package javelin.view.screen.world;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import javelin.controller.walker.Walker;
import javelin.model.BattleMap;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;
import javelin.model.world.Haxor;
import javelin.model.world.Incursion;
import javelin.model.world.Lair;
import javelin.model.world.Portal;
import javelin.model.world.Squad;
import javelin.model.world.Squad.Transport;
import javelin.model.world.WorldActor;
import javelin.model.world.WorldMap;
import javelin.model.world.WorldMap.Region;
import javelin.model.world.WorldPlace;
import javelin.model.world.town.Order;
import javelin.model.world.town.Town;
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

	private static final ArrayList<Realm> TOWNINFO = new ArrayList<Realm>();
	static final HashMap<Class<? extends WorldActor>, Float> FEATURECHANCE =
			new HashMap<Class<? extends WorldActor>, Float>();

	static {
		TOWNINFO.add(Realm.FIRE);
		TOWNINFO.add(Realm.WATER);
		TOWNINFO.add(Realm.WIND);
		TOWNINFO.add(Realm.EARTH);
		TOWNINFO.add(Realm.MAGICAL);
		TOWNINFO.add(Realm.GOOD);
		TOWNINFO.add(Realm.EVIL);

		FEATURECHANCE.put(Dungeon.class, .5f);
		FEATURECHANCE.put(Lair.class, .25f);
		FEATURECHANCE.put(Portal.class, .25f);
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
			Realm r = RPG.pick(TOWNINFO);
			TOWNINFO.remove(r);
			placetown(town, r);
		}
		int more = RPG.r(5, 7);
		for (int i = 0; i < more; i++) {
			int x = RPG.r(0, WorldMap.MAPDIMENSION - 1);
			int y = RPG.r(0, WorldMap.MAPDIMENSION - 1);
			if (getactor(x, y) != null) {
				i -= 1;
				continue;
			}
			Point p = new Point(x, y);
			placetown(p, determinecolor(p).realm);
		}
		Point easya = seed.towns.get(Region.EASYA);
		Point easyb = seed.towns.get(Region.EASYB);
		new Portal(WorldScreen.getactor(easya.x, easya.y, Town.towns),
				WorldScreen.getactor(easyb.x, easyb.y, Town.towns), false,
				false, true, true, null, false).place();
		Haxor.spawn(easya);
		int features = (WorldMap.MAPDIMENSION * WorldMap.MAPDIMENSION) / 9;
		while (Dungeon.dungeons.size() < features
				* FEATURECHANCE.get(Dungeon.class)) {
			Dungeon.spawn(1);
		}
		while (Lair.lairs.size() < features * FEATURECHANCE.get(Lair.class)) {
			Lair.spawn(1);
		}
		while (Portal.portals.size() < features
				* FEATURECHANCE.get(Portal.class)) {
			Portal.open(1);
		}
		final Point start = easya;
		Squad.active.x = start.x;
		Squad.active.y = start.y;
		Squad.active.displace();
		Squad.active.place();
		Squad.active.equipment.fill(Squad.active);
	}

	private static Town determinecolor(Point p) {
		Town closest = Town.towns.get(0);
		for (int i = 1; i < Town.towns.size(); i++) {
			Town t = Town.towns.get(i);
			if (Walker.distance(t.x, t.y, p.x, p.y) < Walker.distance(closest.x,
					closest.y, p.x, p.y)) {
				closest = t;
			}
		}
		return closest;
	}

	protected static void placetown(final Point town, Realm color) {
		final Town town2 = new Town(town.x, town.y, color);
		Town.towns.add(town2);
		town2.place();
	}

	public static BattleMap makemap(final WorldMap seed) {
		WorldScreen.worldmap =
				new BattleMap(WorldMap.MAPDIMENSION, WorldMap.MAPDIMENSION);
		for (int i = 0; i < WorldScreen.worldmap.width; i++) {
			for (int j = 0; j < WorldScreen.worldmap.height; j++) {
				WorldScreen.worldmap.setTile(i, j, seed.getTile(i, j));
			}
		}
		WorldScreen.worldmap.makeAllInvisible();
		Point t = seed.towns.get(Region.EASYA);
		for (int x = t.x - 2; x <= t.x + 2; x++) {
			for (int y = t.y - 2; y <= t.y + 2; y++) {
				if (x < 0 || x >= WorldMap.MAPDIMENSION || y < 0
						|| y >= WorldMap.MAPDIMENSION) {
					continue;
				}
				setVisible(x, y);
			}
		}
		return worldmap;
	}

	@Override
	protected void updatescreen(final Thing h) {
		centerscreen(h.x, h.y);
		view(h);
		Game.redraw();
	}

	@Override
	public void view(Thing h) {
		for (int x = h.x - 1; x <= h.x + 1; x++) {
			for (int y = h.y - 1; y <= h.y + 1; y++) {
				setVisible(x, y);
			}
		}
		for (Point p : discovered) {
			setVisible(p.x, p.y);
		}
	}

	public static HashSet<Point> discovered = new HashSet<Point>();

	static private void setVisible(int x, int y) {
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
			final float onefeatureperweek = 1 / 7f;
			Lair.spawn(onefeatureperweek * FEATURECHANCE.get(Lair.class));
			Dungeon.spawn(onefeatureperweek * FEATURECHANCE.get(Dungeon.class));
			Portal.open(onefeatureperweek * FEATURECHANCE.get(Portal.class));
			Weather.weather();
			Town tournament = null;
			for (Town t : Town.towns) {
				for (Order item : t.training.reclaim(time)) {
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
			Town.work();
			WorldScreen.lastday += 1;
			if (Incursion.invade(this) && !Squad.squads.isEmpty()) {
				break;
			}
		}
		StateManager.save();
	}

	static int countfeatures() {
		return Lair.lairs.size() + Dungeon.dungeons.size()
				+ Portal.portals.size();
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

	@Override
	protected void initmap() {
		return;
	}
}
