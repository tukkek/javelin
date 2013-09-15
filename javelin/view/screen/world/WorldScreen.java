package javelin.view.screen.world;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.action.Action;
import javelin.controller.action.world.Abandon;
import javelin.controller.action.world.CastSpells;
import javelin.controller.action.world.Guide;
import javelin.controller.action.world.Rename;
import javelin.controller.action.world.ResetScore;
import javelin.controller.action.world.ShowStatistics;
import javelin.controller.action.world.Split;
import javelin.controller.action.world.UseItems;
import javelin.controller.action.world.WorldAction;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.upgrade.Spell;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.model.world.WorldActor;
import javelin.model.world.WorldMap;
import javelin.model.world.WorldMap.Region;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;

public class WorldScreen extends BattleScreen {
	private static final int STATUSSPACE = 28;
	public static final String SPACER = "                                               ";
	static final WorldAction[] ACTIONS = new WorldAction[] {
			new UseItems(),
			new CastSpells(),
			new Split(),
			new Rename(),
			new ResetScore(),
			new ShowStatistics(),
			new Abandon(),
			new Guide(KeyEvent.VK_F1, "How to play", "F1"),
			new Guide(KeyEvent.VK_F2, "Combat modifiers", "F2"),
			new Guide(KeyEvent.VK_F3, "Upgrades", "F3"),
			new Guide(KeyEvent.VK_F4, "Items", "F4"),
			new Guide(KeyEvent.VK_F5, "Spells", "F5"),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD7, }, -1, -1,
					new String[] { "↖ or 7 or U", "U" }),
			new WorldMove(new int[] { KeyEvent.VK_UP, KeyEvent.VK_NUMPAD8 }, 0,
					-1, new String[] { "↑ or 8 or I", "I" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD9 }, 1, -1,
					new String[] { "↗ or 9 or O", "O" }),
			new WorldMove(new int[] { KeyEvent.VK_LEFT, KeyEvent.VK_NUMPAD4 },
					-1, 0, new String[] { "← or 4 or J", "J" }),
			new WorldMove(
					new int[] { KeyEvent.VK_RIGHT, KeyEvent.VK_NUMPAD6, }, +1,
					0, new String[] { "→ or 6 or L", "L" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD1 }, -1, 1,
					new String[] { "↙ or 1 or M", "M" }),
			new WorldMove(new int[] { KeyEvent.VK_DOWN, KeyEvent.VK_NUMPAD2, },
					0, 1, new String[] { "↓ or 2 or <", "<" }),
			new WorldMove(new int[] { KeyEvent.VK_NUMPAD3 }, 1, 1,
					new String[] { "↘ or 3 or >", ">" }), new WorldHelp(), };
	public static BattleMap worldmap;
	public static Thing worldhero;
	public static double lastday = -1;
	public static WorldScreen current;
	private static boolean welcome = true;
	public static String period;;

	public WorldScreen(final QuestApp q, final BattleMap m) {
		super(q, m);
		current = this;
		Javelin.settexture(QuestApp.DEFAULTTEXTURE);
	}

	@Override
	protected boolean addsidebar() {
		return false;
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
		updatescreen(Game.hero());
		performAction(Game.hero(), convertEventToAction(getUserInput()), false);
	}

	@Override
	public Action convertEventToAction(final KeyEvent keyEvent) {
		for (final WorldAction a : ACTIONS) {
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
		JavelinApp.overviewmap = makemap(seed);
		for (final Point town : seed.towns.values()) {
			final Town town2 = new Town(town.x, town.y);
			Town.towns.add(town2);
			town2.place();
		}
		final Point start = seed.towns.get(Region.EASYA);
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
	}

	public static BattleMap makemap(final WorldMap seed) {
		worldmap = new BattleMap(WorldMap.MAPDIMENSION, WorldMap.MAPDIMENSION);
		for (int i = 0; i < worldmap.width; i++) {
			for (int j = 0; j < worldmap.height; j++) {
				worldmap.setTile(i, j, seed.getTile(i, j));
			}
		}
		worldmap.setAllVisible();
		return worldmap;
	}

	@Override
	protected void updatescreen(final Thing h) {
		mappanel.viewPosition(worldmap, h.x, h.y);
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
		if (Javelin.namerecruits()) {
			Javelin.app.switchScreen(this);
			StateManager.save();
		}
		saywelcome();
		final Squad nextsquad = Javelin.act();
		final int day = new Double(Math.ceil(nextsquad.hourselapsed / 24.0))
				.intValue();
		while (day != lastday) {
			for (final Squad s : Squad.squads) {
				s.eat();
			}
			heardungeongossip();
			Incursion.invade(this);
			Weather.weather();
			lastday += 1;
		}
		showplayerinformation();
		super.step();
		messagepanel.clear();
	}

	public void showplayerinformation() {
		final ArrayList<String> infos = new ArrayList<String>();
		period = Javelin.getDayPeriod();
		String date = "Day " + currentday() + ", " + period.toLowerCase();
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
				while (hp.length() < SPACER.length()) {
					hp += " ";
				}
			} else {
				hp = "    " + SPACER;
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
			while (vital.length() < STATUSSPACE) {
				vital += " ";
			}
			hps.add(vital
					+ " Level "
					+ Math.round(Math.floor(ChallengeRatingCalculator
							.calculateCr(m.source)))
					+ " "
					+ m.xp.multiply(new BigDecimal(100)).setScale(0,
							RoundingMode.HALF_UP) + "XP");
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
		return Math.round(Math.floor(lastday));
	}

	/**
	 * TODO Would be interesting being able to spend time on the pub and perhaps
	 * hear about the location of dungeons or incursions
	 */
	public void heardungeongossip() {
		if (RPG.r(1, 7) == 1) {
			final Dungeon d = new Dungeon();
			d.place();
			StateManager.save();
		}
	}

	public void saywelcome() {
		if (welcome) {
			Game.message(Javelin.sayWelcome(), null, Delay.NONE);
			IntroScreen.feedback();
			messagepanel.clear();
			welcome = false;
		}
	}

	public static Object getmapactor(final int x, final int y) {
		for (final WorldActor s : getallmapactors()) {
			if (x == s.getx() && y == s.gety()) {
				return s;
			}
		}
		return null;
	}

	public static ArrayList<WorldActor> getallmapactors() {
		final ArrayList<WorldActor> actors = new ArrayList<WorldActor>(
				Squad.squads);
		actors.addAll(Town.towns);
		actors.addAll(Incursion.squads);
		actors.addAll(Dungeon.dungeons);
		return actors;
	}

	@Override
	public boolean drawbackground() {
		return false;
	}

	public static ArrayList<WorldActor> getactors() {
		ArrayList<WorldActor> actors = new ArrayList<WorldActor>();
		actors.addAll(Dungeon.dungeons);
		actors.addAll(Incursion.squads);
		actors.addAll(Town.towns);
		actors.addAll(Squad.squads);
		return actors;
	}

	public static Object getactor(int x, int y, ArrayList<WorldActor> actors) {
		for (WorldActor actor : actors) {
			if (actor.getx() == x && actor.gety() == y) {
				return actor;
			}
		}
		return null;
	}
}
