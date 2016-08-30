package javelin.view.screen;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.world.WorldAction;
import javelin.controller.action.world.WorldMove;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.model.world.Incursion;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldPanel;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.QuestApp;

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
	public static final String SPACER =
			"                                               ";

	/** Last day that was taken into account by {@link World} computations. */
	public static double lastday = -1;
	/** Current active world screen. */
	public static WorldScreen current;
	static boolean welcome = true;

	/** Constructor. */
	public WorldScreen() {
		super(false);
		WorldScreen.current = this;
		Javelin.settexture(QuestApp.DEFAULTTEXTURE);
		Tile[][] tiles = gettiles();
		if (Preferences.DEBUGESHOWMAP) {
			for (Tile[] ts : tiles) {
				for (Tile t : ts) {
					t.discovered = true;
				}
			}
		} else if (getClass().equals(WorldScreen.class)) {
			for (Point p : StateManager.DISCOVERED) {
				tiles[p.x][p.y].discovered = true;
			}
		}
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
					a.perform(this);
					return;
				}
			}
		}
		for (final WorldAction a : WorldAction.ACTIONS) {
			for (final int s : a.keys) {
				if (s == keyEvent.getKeyCode()) {
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
		}
		StateManager.save(false, StateManager.SAVEFILE);
		endturn();
		if (Squad.getall(Squad.class).isEmpty()) {
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
		centerscreen(h.x, h.y);
		view(h.x, h.y);
		Game.redraw();
	}

	@Override
	public void view(int x, int y) {
		int vision = Squad.active.perceive(true)
				+ (Squad.active.transport == Transport.AIRSHIP ? +4
						: Terrain.current().visionbonus);
		Squad.active.view(vision);
	}

	/**
	 * Marks coordinate as permanently visible.
	 */
	static public void setVisible(int x, int y) {
		if (!World.validatecoordinate(x, y)) {
			return;
		}
		StateManager.DISCOVERED.add(new Point(x, y));
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
	public void endturn() {
		Squad act = Javelin.act();
		if (act != null && act.work != null) {
			act.build();
		}
		long time = act == null ? Town.train() : act.hourselapsed;
		final int day = new Double(Math.ceil(time / 24.0)).intValue();
		List<WorldActor> squads = Squad.getall(Squad.class);
		while (day > WorldScreen.lastday || squads.isEmpty()) {
			Season.change(day);
			FeatureGenerator.SINGLETON.spawn(1 / 7f, false);
			for (WorldActor p : WorldActor.getall()) {
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
	}

	/** Show party/world status. */
	public void updateplayerinformation() {
		final ArrayList<String> infos = new ArrayList<String>();
		String period = Javelin.getDayPeriod();
		String date = "Day " + currentday() + ", " + period.toLowerCase();
		String weather = Terrain.current().getweather();
		if (!weather.isEmpty()) {
			date += " (" + weather + ")";
		}
		infos.add(date);
		infos.add(Season.current.toString());
		infos.add("");
		if (Dungeon.active == null) {
			infos.add(Squad.active.speed(Terrain.current()) + " mph"
					+ (Squad.active.transport == null ? ""
							: Squad.active.transport
									.load(Squad.active.members)));
		}
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
				hp = WorldScreen.SPACER;
			}
			panel += hp + info + "\n";
		}
		Game.message(panel, Delay.NONE);
	}

	/**
	 * @return One line of text containing unit name and status information
	 *         (health, poison, etc).
	 */
	static public ArrayList<String> showstatusinformation() {
		final ArrayList<String> hps = new ArrayList<String>();
		for (final Combatant c : Squad.active.members) {
			String status = c.getStatus() + ", ";
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
			hps.add(vital + " Level "
					+ Math.round(Math.floor(
							ChallengeRatingCalculator.calculateCr(c.source)))
					+ " " + c.gethumanxp());
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
		Game.message(Javelin.sayWelcome(), Delay.NONE);
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
	 * @return
	 */
	public boolean explore(float hoursellapsed, boolean encounter) {
		if (encounter && //
				(Squad.active.transport == null
						|| Squad.active.transport.battle())) {
			RandomEncounter.encounter(hoursellapsed / HOURSPERENCOUNTER);
		}
		Set<Hazard> hazards = Terrain.current()
				.gethazards(RPG.r(1, Terrain.HAZARDCHANCE) == 1);
		for (Hazard h : new ArrayList<Hazard>(hazards)) {
			if (!h.validate()) {
				hazards.remove(h);
			}
		}
		if (hazards.isEmpty()) {
			return true;
		}
		RPG.pick(new ArrayList<Hazard>(hazards))
				.hazard(Math.round(hoursellapsed));
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
	public boolean react(WorldActor actor, int x, int y) {
		if (actor == null) {
			return false;
		}
		if (actor instanceof Location) {
			return actor.interact();
		}
		if (!WorldMove.isleavingplace) {
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
	public void centerscreen(int x, int y) {
		mappanel.viewPosition(x, y);
	}

	@Override
	public Image gettile(int x, int y) {
		return Images.getImage("terrain" + Terrain.get(x, y).toString());
	}

	/**
	 * @return <code>true</code> if this {@link World} coordinate can be seen.
	 */
	public static boolean see(Point p) {
		if (!World.validatecoordinate(p.x, p.y)) {
			return false;
		}
		WorldScreen s = getcurrentscreen();
		return s == null ? StateManager.DISCOVERED.contains(p)
				: s.gettiles()[p.x][p.y].discovered;
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
}
