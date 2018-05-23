package javelin.view.screen;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.world.WorldAction;
import javelin.controller.action.world.WorldMove;
import javelin.controller.db.StateManager;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.transport.Transport;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.old.Game;
import javelin.old.Game.Delay;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldPanel;

/**
 * Shows and helps manage the overworld view.
 *
 * @see World
 * @see JavelinApp#overviewmap
 * @author alex
 */
public class WorldScreen extends BattleScreen {

	/**
	 * Every {@link WorldMove} should be carefully considered - both to provide
	 * interesting strategic situation and also to help the {@link World} fit in
	 * a small amount of screen-space. If the World is small enough to fit a
	 * screen we can't have the player walking around too freely or he will be
	 * able to reach anywhere without difficulty.
	 *
	 * One encounter every 2 steps is way too restricting though - 3 feels like
	 * "most" steps will be safe, but just barely enough.
	 *
	 * Per D&D rules a well-equipped party should be able to withstand 4
	 * encounters in a row before needing to rest. This means that the player
	 * can take (statistically speaking) 6 steps away from a Town and still come
	 * back to rest properly. This sounds quite fine, even quite liberal,
	 * considering how small the map is and that the player can build roads,
	 * rent {@link Transport} vehicles, etc - which will further reduce the
	 * encounter rate by increasing player speed.
	 *
	 * The current value (1.85) has been selected to make sure starting units
	 * are close to the 3-step per encounter mark but actually a {@link Squad}
	 * with 30ft-moving units is getting closer to 5 steps even on bad
	 * {@link Terrain}. Unfortunately the fixed {@link WorldMove#MOVETARGET} and
	 * the fact that current {@link Monster} selection allows a novice player to
	 * select a 15-feet moving unit makes this hard to circumvent.
	 */
	public static final float HOURSPERENCOUNTER = WorldMove.MOVETARGET * 1.85f;
	private static final int STATUSSPACE = 28;
	/** TODO used for tabulation, shouldn't be needed with a more modern UI */
	public static final String SPACER = "                                               ";

	/** Last day that was taken into account by {@link World} computations. */
	public static double lastday = -1;
	/** Current active world screen. */
	public static WorldScreen current;
	static boolean welcome = true;
	public boolean firstdraw = true;

	/**
	 * Constructor.
	 *
	 * @param open
	 */
	public WorldScreen(boolean open) {
		super(false, open);
	}

	@Override
	void open() {
		super.open();
		WorldScreen.current = this;
		Tile[][] tiles = gettiles();
		if (Debug.showmap) {
			for (Tile[] ts : tiles) {
				for (Tile t : ts) {
					t.discovered = true;
				}
			}
		} else {
			showdiscovered(tiles);
		}
	}

	@Override
	public void close() {
		super.close();
		savediscovered();
	}

	Tile[][] gettiles() {
		return mappanel.tiles;
	}

	void move() {
		try {
			redraw();
			Game.userinterface.waiting = true;
			final KeyEvent updatableUserAction = getUserInput();
			if (MapPanel.overlay != null) {
				MapPanel.overlay.clear();
			}
			if (updatableUserAction == null) {
				callback.run();
				callback = null;
			} else {
				perform(updatableUserAction);
			}
		} catch (RepeatTurn e) {
			Game.messagepanel.clear();
			updateplayerinformation();
			move();
		}
	}

	void perform(KeyEvent keyEvent) {
		for (final WorldAction a : WorldAction.ACTIONS) {
			for (final String s : a.morekeys) {
				if (s.equals(Character.toString(keyEvent.getKeyChar()))) {
					Game.messagepanel.clear();
					a.perform(this);
					return;
				}
			}
		}
		for (final WorldAction a : WorldAction.ACTIONS) {
			for (final int s : a.keys) {
				if (s == keyEvent.getKeyCode()) {
					Game.messagepanel.clear();
					a.perform(this);
					return;
				}
			}
		}
	}

	@Override
	public void turn() {
		if (WorldScreen.welcome) {
			saywelcome();
		} else if (World.scenario.win()) {
			StateManager.clear();
			System.exit(0);
		}
		StateManager.save(false, StateManager.SAVEFILE);
		endturn();
		if (World.getall(Squad.class).isEmpty()) {
			return;
		}
		updateplayerinformation();
		move();
		messagepanel.clear();
	}

	/** TODO remove on 2.0+ */
	public Point getherolocation() {
		return Squad.active == null ? null
				: new Point(Squad.active.x, Squad.active.y);
	}

	void redraw() {
		Javelin.app.switchScreen(this);
		Point h = JavelinApp.context.getherolocation();
		center(h.x, h.y);
		view(h.x, h.y);
		Game.redraw();
	}

	@Override
	public void view(int x, int y) {
		Squad.active.seesurroudings();
	}

	/**
	 * Marks coordinate as permanently visible.
	 */
	static public void setVisible(int x, int y) {
		if (!World.validatecoordinate(x, y)) {
			return;
		}
		// StateManager.DISCOVERED.add(new Point(x, y));
		WorldScreen s = getcurrentscreen();
		if (s != null) {
			s.gettiles()[x][y].discovered = true;
		}
	}

	/**
	 * Player acts and ends turn, allowing time to pass.
	 *
	 * @see Javelin#act()
	 * @see Squad#hourselapsed
	 */
	void endturn() {
		if (Dungeon.active != null) {
			return;
		}
		Squad act = Javelin.act();
		long time = act.hourselapsed;
		final int day = new Double(Math.ceil(time / 24.0)).intValue();
		List<Actor> squads = World.getall(Squad.class);
		while (day > WorldScreen.lastday || squads.isEmpty()) {
			WorldScreen.lastday += 1;
			Season.change(day);
			Weather.weather();
			World.seed.featuregenerator.spawn(1 / 14f, false);
			World.scenario.endday();
			ArrayList<Actor> actors = World.getactors();
			ArrayList<Incursion> incursions = Incursion.getincursions();
			actors.removeAll(incursions);
			Collections.shuffle(actors);
			for (Actor a : actors) {
				a.turn(time, this);
				Location l = a instanceof Location ? (Location) a : null;
				if (l != null && World.scenario.spawn) {
					l.spawn();
				}
			}
			Collections.shuffle(incursions);
			for (Incursion i : incursions) {
				/* may throw StartBattle */
				i.turn(time, this);
			}
		}
	}

	/** Show party/world status. */
	public void updateplayerinformation() {
		Game.messagepanel.clear();
		final ArrayList<String> infos = new ArrayList<>();
		String period = Javelin.getDayPeriod();
		String date = "Day " + currentday() + ", " + period.toLowerCase();
		if (Dungeon.active == null) {
			String weather = Terrain.current().getweather();
			if (!weather.isEmpty()) {
				date += " (" + weather + ")";
			}
		}
		infos.add(date);
		infos.add(Season.current.toString());
		infos.add("");
		if (Dungeon.active == null) {
			final int mph = Squad.active.speed(Terrain.current(),
					Squad.active.x, Squad.active.y);
			infos.add(mph + " mph" + (Squad.active.transport == null ? ""
					: Squad.active.transport.load(Squad.active.members)));
		}
		infos.add(printgold());
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
				hp = WorldScreen.SPACER;
			}
			panel += hp + info + "\n";
		}
		Game.message(panel, Delay.NONE);
	}

	static String printgold() {
		final int upkeep = Squad.active.getupkeep();
		String gold = "$" + Javelin.format(Squad.active.gold);
		if (upkeep > 0) {
			gold += " (upkeep: $" + Javelin.format(upkeep) + "/day)";
		}
		return gold;
	}

	/**
	 * @return One line of text containing unit name and status information
	 *         (health, poison, etc).
	 */
	static public ArrayList<String> showstatusinformation() {
		final ArrayList<String> hps = new ArrayList<>();
		for (final Combatant c : Squad.active.members) {
			String status = c.getstatus() + ", ";
			if (c.source.poison > 0) {
				status += "weak, ";
			}
			if (c.spells.size() > 0 && checkexhaustion(c)) {
				status += "spent, ";
			}
			String vital = c.toString() + " ("
					+ status.substring(0, status.length() - 2) + ")";
			while (vital.length() < WorldScreen.STATUSSPACE) {
				vital += " ";
			}
			long cr = Math.round(Math.floor(c.source.cr));
			hps.add(vital + " Level " + cr + " " + c.gethumanxp());
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

	/**
	 * @return The current day, starting from 1 when the game begins.
	 */
	static public long currentday() {
		return Math.round(Math.floor(WorldScreen.lastday));
	}

	private void saywelcome() {
		Game.message(Javelin.welcome(), Delay.NONE);
		InfoScreen.feedback();
		messagepanel.clear();
		WorldScreen.welcome = false;
	}

	/**
	 * The arbitrary rule is 1 encounter per day in the wild.
	 *
	 * TODO think of a better framework as design guideline for distance between
	 * towns, encounter ratio, etc. The game has changed a lot since 1.0 but the
	 * goal back then was to allow you to reach a new {@link Town} after an
	 * average of 4 fights (100% resource spending). Now {@link Fortification}s
	 * and hostile Towns change everything. The ultimate goal still is to make
	 * every move strategically meaningful (no no-brainers) while still keeping
	 * the world scre en smal enough to fit in just a few screens worth of size.
	 *
	 * @param hoursellapsed
	 * @return <code>true</code> if exploration was uneventful,
	 *         <code>false</code> if something happened.
	 */
	public boolean explore(float hoursellapsed, boolean encounter) {
		if (encounter && World.scenario.worldencounters
				&& (Squad.active.transport == null
						|| Squad.active.transport.battle())) {
			RandomEncounter.encounter(hoursellapsed / HOURSPERENCOUNTER);
		}
		if (!World.scenario.worldhazards) {
			return false;
		}
		boolean special = RPG.r(1, Terrain.HAZARDCHANCE) == 1;
		Set<Hazard> hazards = Squad.active.getdistrict() == null
				? Terrain.current().gethazards(special)
				: Town.gethazards(special);
		for (Hazard h : new ArrayList<>(hazards)) {
			if (!h.validate()) {
				hazards.remove(h);
			}
		}
		if (hazards.isEmpty()) {
			return true;
		}
		RPG.pick(new ArrayList<>(hazards)).hazard(Math.round(hoursellapsed));
		return false;
	}

	/**
	 * TODO it's a conceptual mess
	 *
	 * @param actor
	 *            Used for the {@link WorldScreen}.
	 * @param x
	 *            Coordinate, used for {@link DungeonScreen}.
	 * @param y
	 *            Coordinate, used for {@link DungeonScreen}.
	 * @return <code>true</code> if the active {@link Squad} interacted with
	 *         some map feature, location, etc.
	 */
	public boolean react(Actor actor, int x, int y) {
		if (actor != null
				&& (actor instanceof Location || !WorldMove.isleavingplace)) {
			return actor.interact();
		}
		return false;
	}

	/**
	 * @return <code>false</code> if the given coordinate is impenetrable or
	 *         impassable.
	 */
	public boolean allowmove(int x, int y) {
		return true;
	}

	/** Updates the hero to this new location. */
	public void updatelocation(int x, int y) {
		Squad.active.x = x;
		Squad.active.y = y;
		Squad.active.updateavatar();
	}

	@Override
	public void center(int x, int y) {
		if (firstdraw) {
			mappanel.setposition(x, y);
			firstdraw = false;
		} else {
			mappanel.viewposition(x, y);
		}
	}

	@Override
	public Image gettile(int x, int y) {
		return Images.getImage("terrain" + Terrain.get(x, y).toString());
	}

	/**
	 * @return <code>true</code> if this {@link World} coordinate can be seen.
	 */
	public static boolean see(Point p) {
		return World.validatecoordinate(p.x, p.y)
				&& getcurrentscreen().gettiles()[p.x][p.y].discovered;
	}

	static WorldScreen getcurrentscreen() {
		return BattleScreen.active instanceof WorldScreen
				? (WorldScreen) BattleScreen.active : null;
	}

	/**
	 * @return A random encounter fight.
	 */
	public Fight encounter() {
		return new RandomEncounter();
	}

	@Override
	protected MapPanel getmappanel() {
		return new WorldPanel();
	}

	/**
	 * @return <code>true</code> if this coordinate is valid in this context.
	 */
	public boolean validatepoint(int tox, int toy) {
		return World.validatecoordinate(tox, toy);
	}

	public void adddiscovered(HashSet<Point> discovered) {
		discovered.clear();
		for (Tile[] ts : current.mappanel.tiles) {
			for (Tile t : ts) {
				if (t.discovered) {
					discovered.add(new Point(t.x, t.y));
				}
			}
		}
	}

	void showdiscovered(Tile[][] tiles) {
		for (Point p : getdiscoveredtiles()) {
			tiles[p.x][p.y].discovered = true;
		}
	}

	protected HashSet<Point> getdiscoveredtiles() {
		return World.seed.discovered;
	}

	public void savediscovered() {
		adddiscovered(getdiscoveredtiles());
	}

	@Override
	public void center() {
		Javelin.app.switchScreen(this);
		Point here = getherolocation();
		center(here.x, here.y);
	}
}
