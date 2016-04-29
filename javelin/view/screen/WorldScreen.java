package javelin.view.screen;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.Action;
import javelin.controller.action.world.WorldAction;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.tournament.Exhibition;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.place.Outpost;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.Transport;
import javelin.model.world.place.unique.Haxor;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Shows and helps manage the overworld view.
 * 
 * @see World
 * @see JavelinApp#overviewmap
 * @author alex
 */
public class WorldScreen extends BattleScreen {
	public static HashSet<Point> discovered = new HashSet<Point>();
	private static final int STATUSSPACE = 28;
	public static final String SPACER =
			"                                               ";

	public static BattleMap worldmap;
	public static Thing worldhero;
	public static double lastday = -1;
	public static WorldScreen current;
	private static boolean welcome = true;
	public static String period;
	private int savecounter = 0;

	public WorldScreen(BattleMap mapp) {
		super(Javelin.app, mapp, false);
		WorldScreen.current = this;
		Javelin.settexture(QuestApp.DEFAULTTEXTURE);
		mappanel.tilesize = 48;
		if (Javelin.DEBUGEXPLORED) {
			mapp.setAllVisible();
		}
	}

	@Override
	public void checkEndBattle() {
		return;
	}

	@Override
	protected boolean rejectEvent(final KeyEvent keyEvent) {
		return false;
	}

	@Override
	protected void humanTurn() {
		Thing h = JavelinApp.context.updatehero();
		Game.instance().hero = h;
		while (true) {
			try {
				updatescreen(h);
				performAction(h, convertEventToAction(getUserInput()), false);
				break;
			} catch (RepeatTurnException e) {
				Game.messagepanel.clear();
				continue;
			}
		}
	}

	public Thing updatehero() {
		return Squad.active.visual;
	}

	@Override
	public Action convertEventToAction(final KeyEvent keyEvent) {
		for (final WorldAction a : WorldAction.ACTIONS) {
			for (final int s : a.keys) {
				if (s == keyEvent.getKeyCode()) {
					a.perform(this);
					return null;
				}
			}
			for (final String s : a.morekeys) {
				if (s.equals(Character.toString(keyEvent.getKeyChar()))) {
					a.perform(this);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public void performAction(final Thing thing, final Action action,
			final boolean isShiftDown) {
	}

	@Override
	protected void updatescreen(final Thing h) {
		centerscreen(h.x, h.y);
		view(h);
		Game.redraw();
	}

	@Override
	public void view(Thing h) {
		int terrain = Javelin.terrain();
		int vision;
		if (Squad.active.transport == Transport.AIRSHIP) {
			vision = Outpost.VISIONRANGE;
		} else {
			vision = terrain == World.HARD || terrain == World.EASY ? 2 : 1;
		}
		Outpost.discover(h.x, h.y, vision);
		for (Point p : discovered) {
			setVisible(p.x, p.y);
		}
	}

	/**
	 * Marks coordinate as permanently visible.
	 */
	static public void setVisible(int x, int y) {
		worldmap.setVisible(x, y);
		discovered.add(new Point(x, y));
	}

	@Override
	protected void removehero() {
		return;
	}

	@Override
	public void setposition() {
	}

	@Override
	public void step() {
		if (WorldScreen.welcome) {
			saywelcome();
		}
		turn();
		if (Squad.getall(Squad.class).isEmpty()) {
			return;
		}
		updateplayerinformation();
		super.step();
		messagepanel.clear();
	}

	/**
	 * Player acts and ends turn, allowing time to pass.
	 * 
	 * @see Javelin#act()
	 * @see Squad#hourselapsed
	 */
	public void turn() {
		Squad act = Javelin.act();
		long time = act == null ? Town.train() : act.hourselapsed;
		final int day = new Double(Math.ceil(time / 24.0)).intValue();
		List<WorldActor> squads = Squad.getall(Squad.class);
		while (day > WorldScreen.lastday || squads.isEmpty()) {
			World.spawnfeatures(1 / 7f, false);
			for (WorldActor p : getactors()) {
				if (!(p instanceof Incursion)) {
					p.turn(time, this);
				}
			}
			Weather.weather();
			Town tournament = null;
			for (WorldActor p : Town.getall(Town.class)) {
				Town t = (Town) p;
				if (t.ishosting()) {
					assert tournament == null;// only one tournament at a time
					tournament = t;
					tournament.events.remove(0);
				}
			}
			if (tournament == null) {
				Exhibition.opentournament();
			}
			for (WorldActor p : new ArrayList<WorldActor>(
					WorldActor.getall(Incursion.class))) {
				p.turn(time, this);// may throw battle exception
			}
			WorldScreen.lastday += 1;
		}
		savecounter += 1;// no need to save every turn
		if (savecounter >= 5) {
			savecounter = 0;
			StateManager.save();
		}
	}

	public void updateplayerinformation() {
		final ArrayList<String> infos = new ArrayList<String>();
		WorldScreen.period = Javelin.getDayPeriod();
		String date =
				"Day " + currentday() + ", " + WorldScreen.period.toLowerCase();
		if (Weather.now == 1) {
			date += " (raining)";
		} else if (Weather.now == 2) {
			date += " (storm)";
		}
		infos.add(date);
		infos.add("$" + SelectScreen.formatcost(Squad.active.gold));
		final ArrayList<String> hps = showstatusinformation();
		while (hps.size() > 6) {
			hps.remove(6);
		}
		String panel = "";
		for (int i = 0; i < Math.max(infos.size(), hps.size()); i++) {
			String hp;
			final String info = infos.size() > i ? "    " + infos.get(i) : "";
			if (hps.size() > i) {
				hp = hps.get(i);
				while (hp.length() < WorldScreen.SPACER.length()) {
					hp += " ";
				}
			} else {
				hp = "    " + WorldScreen.SPACER;
			}
			panel += hp + info + "\n";
		}
		Game.message(panel, null, Delay.NONE);
	}

	static public ArrayList<String> showstatusinformation() {
		final ArrayList<String> hps = new ArrayList<String>();
		for (final Combatant m : Squad.active.members) {
			String status = m.getStatus();
			if (m.spells.size() > 0 && checkexhaustion(m)) {
				status += "*";
			}
			String name = m.toString();
			String vital = name + " (" + status + ")";
			while (vital.length() < WorldScreen.STATUSSPACE) {
				vital += " ";
			}
			hps.add(vital + " Level "
					+ Math.round(Math.floor(
							ChallengeRatingCalculator.calculateCr(m.source)))
					+ " " + m.xp.multiply(new BigDecimal(100)).setScale(0,
							RoundingMode.HALF_UP)
					+ "XP");
		}
		return hps;
	}

	static private boolean checkexhaustion(Combatant m) {
		for (Spell s : m.spells) {
			if (!s.exhausted()) {
				return false;
			}
		}
		return true;
	}

	public long currentday() {
		return Math.round(Math.floor(WorldScreen.lastday));
	}

	private void saywelcome() {
		Game.message(Javelin.sayWelcome(), null, Delay.NONE);
		InfoScreen.feedback();
		messagepanel.clear();
		WorldScreen.welcome = false;
	}

	public static Object getmapactor(final int x, final int y) {
		for (final WorldActor s : WorldScreen.getallmapactors()) {
			if (x == s.x && y == s.y) {
				return s;
			}
		}
		return null;
	}

	public static ArrayList<WorldActor> getallmapactors() {
		// TODO
		return getactors();
	}

	@Override
	public boolean drawbackground() {
		return false;
	}

	/**
	 * @return A new list that is a canonical representation of all existing
	 *         {@link WorldActor}s.
	 */
	public static ArrayList<WorldActor> getactors() {
		ArrayList<WorldActor> actors = new ArrayList<WorldActor>();
		for (ArrayList<WorldActor> instances : WorldActor.INSTANCES.values()) {
			if (instances.isEmpty() || instances.get(0) instanceof Squad) {
				continue;
			}
			actors.addAll(instances);
		}
		if (Haxor.singleton != null) {
			actors.add(Haxor.singleton);
		}
		/* squads should be least priority */
		actors.addAll(Squad.getall(Squad.class));
		return actors;
	}

	public static WorldActor getactor(int x, int y,
			List<? extends WorldActor> actors) {
		for (WorldActor actor : actors) {
			if (actor.x == x && actor.y == y) {
				return actor;
			}
		}
		return null;
	}

	public static WorldActor getactor(int x, int y) {
		return WorldScreen.getactor(x, y, WorldScreen.getactors());
	}

	/**
	 * TODO was .4 which is correct by design but the design was thought of
	 * using wrong math and needs to be rethought.
	 */
	public void encounter() {
		if (Squad.active.transport == Transport.AIRSHIP) {
			return;
		}
		WorldScreen.encounter(
				Squad.active.transport == Transport.CARRIAGE ? 1 / 6f : 1 / 3f);
	}

	static public void encounter(double d) {
		if (RPG.random() < d && !Javelin.DEBUGDISABLECOMBAT) {
			throw new StartBattle(Dungeon.active == null ? new RandomEncounter()
					: new RandomDungeonEncounter());
		}
	}

	public boolean react(WorldActor actor, int x, int y) {
		if (actor == null) {
			return false;
		}
		if (actor instanceof WorldPlace) {
			// WorldMove.isleavingplace = true;
			return actor.interact();
		}
		if (WorldMove.isleavingplace) {
			// WorldMove.isleavingplace = false;
		} else {
			return actor.interact();
		}
		return false;
	}

	public void ellapse(int suggested) {
		Squad.active.hourselapsed += suggested;
	}

	public boolean allowmove(int x, int y) {
		return true;
	}

	public void updatelocation(int x, int y) {
		Squad.active.x = x;
		Squad.active.y = y;
		Squad.active.updateavatar();
	}

	@Override
	protected boolean center(int x, int y) {
		return true;
	}

	@Override
	protected void initmap() {
		return;
	}

	public static WorldActor getactor(int x, int y,
			Class<? extends WorldActor> class1) {
		return getactor(x, y, WorldActor.getall(class1));
	}
}
