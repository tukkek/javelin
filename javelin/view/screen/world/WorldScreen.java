package javelin.view.screen.world;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.action.Action;
import javelin.controller.action.world.WorldAction;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.tournament.Exhibition;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;
import javelin.model.world.Haxor;
import javelin.model.world.Incursion;
import javelin.model.world.Lair;
import javelin.model.world.Portal;
import javelin.model.world.QueueItem;
import javelin.model.world.Squad;
import javelin.model.world.Squad.Transport;
import javelin.model.world.Town;
import javelin.model.world.WorldActor;
import javelin.model.world.WorldMap;
import javelin.model.world.WorldMap.Region;
import javelin.model.world.WorldPlace;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.InfoScreen;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Shows the overworld view.
 * 
 * @author alex
 */
public class WorldScreen extends BattleScreen {
	private static final int STATUSSPACE = 28;
	public static final String SPACER =
			"                                               ";

	private static final TreeMap<String, Color> TOWNINFO =
			new TreeMap<String, Color>();

	static {
		TOWNINFO.put("Mount Fiery", Color.red);
		TOWNINFO.put("Water tribe", Color.blue);
		TOWNINFO.put("Gale Heights", null);
		TOWNINFO.put("Rife Grounds", Color.green);
		TOWNINFO.put("Octarinum", Color.MAGENTA);
		TOWNINFO.put("Benedita", Color.white);
		TOWNINFO.put("Plaga", Color.black);
	}

	public static BattleMap worldmap;
	public static Thing worldhero;
	public static double lastday = -1;
	public static WorldScreen current;
	private static boolean welcome = true;
	public static String period;;

	public WorldScreen(BattleMap mapp) {
		super(Javelin.app, mapp, false);
		WorldScreen.current = this;
		Javelin.settexture(QuestApp.DEFAULTTEXTURE);
		mappanel.tilesize = 48;
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
		updatescreen(h);
		performAction(h, convertEventToAction(getUserInput()), false);
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

	public static void makemap() {
		final WorldMap seed = WorldMap.seed;
		JavelinApp.overviewmap = WorldScreen.makemap(seed);
		for (final Point town : seed.towns.values()) {
			String name = RPG.pick(new ArrayList<String>(TOWNINFO.keySet()));
			Color color = TOWNINFO.get(name);
			TOWNINFO.remove(name);
			final Town town2 = new Town(town.x, town.y, name, color);
			Town.towns.add(town2);
			town2.place();
		}
		Point easya = seed.towns.get(Region.EASYA);
		Point easyb = seed.towns.get(Region.EASYB);
		new Portal(WorldScreen.getactor(easya.x, easya.y, Town.towns),
				WorldScreen.getactor(easyb.x, easyb.y, Town.towns), false,
				false, true, true, null, false).place();
		Haxor.spawn(easya);
		final Point start = easya;
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
		Squad.active.equipment.fill(Squad.active);
	}

	public static BattleMap makemap(final WorldMap seed) {
		WorldScreen.worldmap =
				new BattleMap(WorldMap.MAPDIMENSION, WorldMap.MAPDIMENSION);
		for (int i = 0; i < WorldScreen.worldmap.width; i++) {
			for (int j = 0; j < WorldScreen.worldmap.height; j++) {
				WorldScreen.worldmap.setTile(i, j, seed.getTile(i, j));
			}
		}
		WorldScreen.worldmap.setAllVisible();
		return WorldScreen.worldmap;
	}

	@Override
	protected void updatescreen(final Thing h) {
		centerscreen(h.x, h.y);
		view(h);
		Game.redraw();
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
		if (Squad.squads.isEmpty()) {
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
		while (day > WorldScreen.lastday || Squad.squads.isEmpty()) {
			for (final Squad s : Squad.squads) {
				s.eat();
			}
			float onefeatureperweek = 1 / 7f;
			Lair.spawn(onefeatureperweek / 4f);
			Dungeon.spawn(onefeatureperweek / 2f);
			Portal.open(onefeatureperweek / 4f);
			Weather.weather();
			Town tournament = null;
			for (Town t : Town.towns) {
				for (QueueItem item : t.training.reclaim(time)) {
					t.completetraining(item);
				}
				if (t.ishosting()) {
					assert tournament == null;
					tournament = t;
				}
			}
			if (tournament == null) {
				Exhibition.opentournament();
			} else {
				tournament.events.remove(0);
			}
			WorldScreen.lastday += 1;
			if (Incursion.invade(this) && !Squad.squads.isEmpty()) {
				break;
			}
		}
		StateManager.save();
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
		if (Incursion.squads.size() > 0) {
			infos.add("Invasion!");
		}
		final ArrayList<String> hps = showstatusinformation();
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

	public ArrayList<String> showstatusinformation() {
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

	private boolean checkexhaustion(Combatant m) {
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
			if (x == s.getx() && y == s.gety()) {
				return s;
			}
		}
		return null;
	}

	public static ArrayList<WorldActor> getallmapactors() {
		// final ArrayList<WorldActor> actors =
		// new ArrayList<WorldActor>(Squad.squads);
		// actors.addAll(Town.towns);
		// actors.addAll(Incursion.squads);
		// actors.addAll(Lair.lairs);
		// actors.addAll(Dungeon.dungeons);
		// return actors;
		// TODO
		return getactors();
	}

	@Override
	public boolean drawbackground() {
		return false;
	}

	public static ArrayList<WorldActor> getactors() {
		ArrayList<WorldActor> actors = new ArrayList<WorldActor>();
		actors.addAll(Lair.lairs);
		actors.addAll(Incursion.squads);
		actors.addAll(Town.towns);
		actors.addAll(Dungeon.dungeons);
		actors.addAll(Portal.portals);
		if (Haxor.singleton != null) {
			actors.add(Haxor.singleton);
		}
		/* squads should be least priority */
		actors.addAll(Squad.squads);
		return actors;
	}

	public static WorldActor getactor(int x, int y,
			List<? extends WorldActor> actors) {
		for (WorldActor actor : actors) {
			if (actor.getx() == x && actor.gety() == y) {
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
			throw new StartBattle(new RandomEncounter());
		}
	}

	public boolean react(Thing t, WorldMove worldMove) {
		WorldActor actor = WorldScreen.getactor(t.x, t.y);
		if (actor instanceof WorldPlace) {
			((WorldPlace) actor).enter();
			WorldMove.isleavingplace = true;
			return true;
		}
		if (WorldMove.isleavingplace) {
			WorldMove.isleavingplace = false;
		} else {
			for (final Incursion spot : Incursion.squads) {
				if (spot.x == t.x && spot.y == t.y) {
					final Incursion i = spot;
					throw new StartBattle(i.getfight());
				}
			}
		}
		return false;
	}

	public boolean entertown(Thing t, WorldScreen s, int x, int y) {
		for (final Town town : Town.towns) {
			if (town.x == x && town.y == y) {
				town.enter(Squad.active);
				Javelin.app.switchScreen(BattleScreen.active);
				return true;
			}
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

	static public void displace(WorldActor actor) {
		int deltax = 0, deltay = 0;
		int[] nudges = new int[] { -1, 0, +1 };
		while (deltax == 0 && deltay == 0) {
			deltax = RPG.pick(nudges);
			deltay = RPG.pick(nudges);
		}
		int tox = actor.getx() + deltax;
		int toy = actor.gety() + deltay;
		ArrayList<WorldActor> actors = WorldScreen.getactors();
		actors.remove(actor);
		if (WorldScreen.getactor(tox, toy, actors) == null) {
			actor.move(tox, toy);
		} else {
			WorldScreen.displace(actor);
		}
	}
}
