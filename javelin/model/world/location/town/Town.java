package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.fight.Siege;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.fight.tournament.Match;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.governor.Governor;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Growth;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.Images;
import javelin.view.screen.NamingScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.TownScreen;
import tyrant.mikera.engine.RPG;

/**
 * A hub for upgrading units, resting, participating in tournament, renting
 * transportation, buying items...
 * 
 * Each town has it's own profile which is predetermined.
 * 
 * @author alex
 */
public class Town extends Location {
	/**
	 * How much {@link #labor} a single work produces in one day (
	 * {@value #DAILYLABOR}). The goal here is to have a {@link Town} controlled
	 * by a {@link HumanGovernor} be around {@link #population} 20 by the end of
	 * 1 year.
	 */
	public static final float DAILYLABOR = .2f;

	static final ArrayList<String> NAMES = new ArrayList<String>();
	static final String[] RANKS = new String[] { "hamlet", "village", "town",
			"city" };
	/** TODO */
	public static final boolean DEBUGPROJECTS = false;

	/**
	 * TODO could probably use only this instead of #RANKS and #getrank()
	 * 
	 * @author alex
	 */
	public enum Rank {
		HAMLET, VILLAGE, TOWN, CITY
	}

	static {
		initnames();
	}

	public ArrayList<Exhibition> events = new ArrayList<Exhibition>();
	/**
	 * Represent 10 working citizens that will produce 1 {@link #labor} every 10
	 * days.
	 * 
	 * An arbitrary decision is to try to fit the game-span of a normal game
	 * into a 1-year period, which puts a town max size roughly at 10 if it does
	 * nothing but {@link Growth}.
	 */
	public int population = 1;
	/** See {@link Governor}. */
	public Governor governor = new HumanGovernor(this);
	/**
	 * Alphabetically ordered set of urban traits.
	 * 
	 * @see Deck
	 */
	public TreeSet<String> traits = new TreeSet<String>();

	/**
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @param r
	 *            Type of town.
	 */
	public Town(Point location, Realm r) {
		super(NAMES.isEmpty() ? null : NAMES.get(0));
		if (!NAMES.isEmpty()) {
			NAMES.remove(0);
		}
		allowentry = false;
		this.x = location.x;
		this.y = location.y;
		// if (!World.seed.done) {
		// checktooclose();
		// }
		realm = r;
		gossip = true;
		discard = false;
		vision = getdistrict().getradius();
	}

	public Town(List<Point> list, Realm r) {
		this(getvalidlocation(list), r);
	}

	private static Point getvalidlocation(List<Point> list) {
		Collections.shuffle(list);
		for (Point p : list) {
			if (Terrain.get(p.x, p.y) != Terrain.WATER && !checktooclose(p)) {
				return p;
			}
		}
		throw new RestartWorldGeneration();
	}

	static boolean checktooclose(Point p) {
		for (WorldActor town : WorldActor.getall(Town.class)) {
			if (town.distance(p.x, p.y) <= District.RADIUSMAX) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void generate() {
		// location is given in the constructor
	}

	/**
	 * Populates {@link #NAMES}. This may be needed if restarting {@link World}
	 * generation.
	 * 
	 * @see World#retry()
	 */
	public static void initnames() {
		NAMES.clear();
		NAMES.add("Alexandria"); // my name :)
		NAMES.add("Lindblum"); // final fantasy 9
		NAMES.add("Sigil"); // planescape: torment
		NAMES.add("Reno");// fallout 2
		NAMES.add("Marrymore");// super mario rpg
		NAMES.add("Kakariko"); // zelda
		NAMES.add("The Citadel"); // mass effect
		NAMES.add("Tristam");// diablo
		NAMES.add("Midgar"); // final fantasy 7
		NAMES.add("Medina");// chrono trigger
		NAMES.add("Figaro"); // final fantasy 6
		NAMES.add("Balamb"); // final fantasy 8
		NAMES.add("Zanarkand"); // final fantasy 10
		NAMES.add("Cornelia"); // final fantasy 1
		NAMES.add("Vivec");// morrowind
		NAMES.add("Termina");// chrono cross
		NAMES.add("Tarant");// arcanum
		Collections.shuffle(NAMES);
	}

	public ArrayList<Monster> getpossiblerecruits() {
		ArrayList<Monster> recruits = new ArrayList<Monster>();
		String terrain = Terrain.get(x, y).toString();
		for (Monster m : Javelin.ALLMONSTERS) {
			if (m.getterrains().contains(terrain)) {
				recruits.add(m);
			}
		}
		Collections.shuffle(recruits);
		return recruits;
	}

	/**
	 * Receives a {@link #description} from the user for this town.
	 */
	public void rename() {
		description = NamingScreen.getname(toString());
	}

	// public void reclaim() {
	// for (Order o : training.reclaim(Squad.active.hourselapsed)) {
	// TrainingOrder t = (TrainingOrder) o;
	// pickstash(TownUpgradingScreen.completetraining(t, this, t.trained));
	// }
	// for (Order item : crafting.reclaim(Squad.active.hourselapsed)) {
	// CraftingOrder o = (CraftingOrder) item;
	// Item i = o.item;
	// i.grab();
	// }
	// }

	@Override
	public District getdistrict() {
		return new District(this);
	}

	/**
	 * @return <code>true</code> if a flag icon is to be displayed.
	 * @see #events
	 */
	public boolean ishosting() {
		return !events.isEmpty();
	}

	/**
	 * Possibly starts a tournament in this town.
	 */
	public void host() {
		if (ishostile()) {
			return;
		}
		int nevents = RPG.r(3, 7);
		for (int i = 0; i < nevents; i++) {
			events.add(RPG.r(1, 2) == 1 ? RPG.pick(Exhibition.SPECIALEVENTS)
					: new Match());
		}
	}

	@Override
	public void turn(long time, WorldScreen screen) {
		/** Produces {@link #labor} and {@link #automanage}s spending. */
		// for (Order item : training.reclaim(time)) {
		// TrainingOrder to = (TrainingOrder) item;
		// pickstash(
		// TownUpgradingScreen.completetraining(to, this, to.trained));
		// }
		// labor += ;
		// if (governor.automanage) {
		// governor.manage();
		// }
		governor.work(population * DAILYLABOR);
		if (Preferences.DEBUGLABOR != null && !ishostile()) {
			governor.work(Preferences.DEBUGLABOR);
		}
		if (DEBUGPROJECTS) {
			for (Labor l : governor.getprojects()) {
				System.out.println(
						toString() + " (day " + WorldScreen.currentday() + "): "
								+ l.toString() + " " + l.getprogress() + "%");
			}
		}
		// if (!research.queue.isEmpty() && Math
		// .ceil(research.queue.get(0).price) <= Math.floor(labor)) {
		// research.queue.get(0).finish(this, null);
		// research.queue.remove(0);
		// } else if (automanage) {
		// TownManager m =
		// ishostile() ? new MonsterManager() : new HumanManager();
		// m.manage(this);
		// }
	}

	// void pickstash(Squad s) {
	// s.gold += stash;
	// stash = 0;
	// }

	/**
	 * When a player captures a hostile town.
	 * 
	 * @param showsurroundings
	 *            if <code>true</code> will show this town's surrounding
	 *            squares.
	 * @see #ishostile()
	 */
	public void captureforhuman(boolean showsurroundings) {
		// labor = 0;
		// nexttask = null;
		// research.queue.clear();
		// crafting.clear();
		// training.clear();
		// automanage = true;
		garrison.clear();
		governor = new HumanGovernor(this);
		// governor.redraw();
		// redraw();
		if (showsurroundings) {
			Outpost.discover(x, y, Outpost.VISIONRANGE);
		}
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		if (attacker.realm == realm) {
			return Incursion.ignoreincursion(attacker);
		}
		if (!garrison.isEmpty()) {
			return attacker.fight(garrison);
		}
		// look for sleeping defense Squad
		for (int x = this.x - 1; x <= this.x + 1; x++) {
			for (int y = this.y - 1; y <= this.y + 1; y++) {
				Squad s = (Squad) WorldActor.get(x, y, Squad.class);
				if (s != null) {
					s.destroy(attacker);
					throw new RuntimeException(
							"destroy is supposed to throw exception #town");
				}
			}
		}
		captureforcomputer(attacker);
		return false;// remove attacking squad, now garrison
	}

	public void captureforcomputer(Incursion attacker) {
		garrison.clear();
		garrison.addAll(attacker.squad);
		attacker.remove();
		// captureforhuman(false);
		realm = attacker.realm;
		governor = new MonsterGovernor(this);
		// governor.redraw();
	}

	@Override
	protected Integer getel(int attackerel) {
		return null;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		Squad.active.lasttown = this;
		// reclaim();
		new TownScreen(this);
		// if (!Squad.getall(Squad.class).isEmpty()) {
		for (final Combatant m : Squad.active.members) {
			if (m.source.fasthealing > 0) {
				m.hp = m.maxhp;
			}
		}
		// reclaim();
		// if (!Squad.active.members.isEmpty()) {
		// Squad.active.updateavatar();
		// }
		// }
		// Javelin.app.switchScreen(BattleScreen.active);
		return true;
	}

	// /**
	// * Note that this method doesn't actually change {@link #transport}.
	// *
	// * @return The next step on the {@link #transport} tree, up to
	// * {@link Transport#AIRSHIP}.
	// */
	// public Transport upgradetransport() {
	// if (transport == null) {
	// return Transport.CARRIAGE;
	// }
	// return transport.equals(Transport.CARRIAGE) ? Transport.SHIP
	// : Transport.AIRSHIP;
	// }

	@Override
	protected Siege fight() {
		Siege f = new Siege(this);
		f.bribe = false;
		return f;
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	/**
	 * @return <code>true</code> if it's probably a good idea for a player to
	 *         return here and manager this town.
	 * @see #automanage
	 * @see #labor
	 * @see TownManager
	 */
	@Override
	public boolean haslabor() {
		return !ishostile() && governor.getprojectssize() > 1;
	}

	@Override
	public Realm getrealmoverlay() {
		return ishostile() ? super.getrealmoverlay() : null;
	}

	@Override
	public Image getimage() {
		String image = "locationtown" + getranktitle();
		if (!ishostile() && ishosting()) {
			image += "festival";
		}
		return Images.getImage(image);
	}

	public String getranktitle() {
		return RANKS[getrank() - 1];
	}

	/**
	 * @return A rank between [1,4] based on current {@link #population}.
	 * @see #RANKS
	 */
	public int getrank() {
		if (population <= 5) {
			return 1;
		}
		if (population <= 10) {
			return 2;
		}
		if (population <= 15) {
			return 3;
		}
		return 4;
	}

	@Override
	public void capture() {
		super.capture();
		captureforhuman(true);
	}

	/**
	 * Creates the initial {@link Location#garrison} for computer-controlled
	 * towns.
	 */
	public void populategarisson() {
		Dwelling d = (Dwelling) getnearest(Dwelling.class);
		garrison.add(new Combatant(d.dweller.source.clone(), true));
		governor = new MonsterGovernor(this);
	}

	public static ArrayList<Town> gettowns() {
		ArrayList<WorldActor> actors = getall(Town.class);
		ArrayList<Town> towns = new ArrayList<Town>(actors.size());
		for (WorldActor a : actors) {
			towns.add((Town) a);
		}
		return towns;
	}

	public int distanceinsteps(Point p) {
		return distanceinsteps(p.x, p.y);
	}

	public static HashSet<Point> getdistricts() {
		HashSet<Point> districts = new HashSet<Point>();
		for (Town t : Town.gettowns()) {
			districts.addAll(t.getdistrict().getarea());
		}
		return districts;
	}

	public static Set<Hazard> gethazards(boolean special) {
		return new HashSet<Hazard>();
	}
}
