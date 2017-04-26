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
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.GaveUpException;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.fight.Siege;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.fight.tournament.Match;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.governor.Governor;
import javelin.model.world.location.town.governor.HumanGovernor;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.Deck;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.base.Dwelling;
import javelin.model.world.location.town.labor.base.Growth;
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

	public static final int MINIMUMDISTANCE = Math
			.round(Math.round(District.RADIUSMAX * 1.5));

	/**
	 * How much {@link #labor} a single work produces in one day (
	 * {@value #DAILYLABOR}). The goal here is to have a {@link Town} controlled
	 * by a {@link Governor} be around {@link #population} 20 by the end of 1
	 * year.
	 *
	 * The best of verifying this with some degree of flexibility is to just
	 * verify that at the end of year 1, the town populations are between 16 and
	 * 25.
	 */
	public static final float DAILYLABOR = .12f;

	public ArrayList<Exhibition> events = new ArrayList<Exhibition>();
	/**
	 * Represent 10 working citizens that will produce 1 {@link #labor} every 10
	 * days.
	 *
	 * An arbitrary decision is to try to fit the game-span of a normal game
	 * into a 1-year period, which puts a town max size roughly at 10 if it does
	 * nothing but {@link Growth}.
	 */
	public int population = World.SCENARIO ? 6 : 1;
	/** See {@link Governor}. */
	public Governor governor = new HumanGovernor(this);
	/**
	 * Alphabetically ordered set of urban traits.
	 *
	 * @see Deck
	 */
	public TreeSet<String> traits = new TreeSet<String>();

	/** Remains the same even after capture. */
	public Realm originalrealm;

	/**
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @param r
	 *            Type of town.
	 */
	public Town(Point location, Realm r) {
		super(World.getseed().townnames.isEmpty() ? null
				: World.getseed().townnames.remove(0));
		allowentry = false;
		x = location.x;
		y = location.y;
		realm = r;
		originalrealm = r;
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
		for (Actor town : World.getall(Town.class)) {
			if (town.distance(p.x, p.y) < MINIMUMDISTANCE) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void generate() {
		// location is given in the constructor
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
		governor.work(population * DAILYLABOR, getdistrict());
	}

	/**
	 * When a player captures a hostile town.
	 *
	 * @param showsurroundings
	 *            if <code>true</code> will show this town's surrounding
	 *            squares.
	 * @see #ishostile()
	 */
	public void captureforhuman(boolean showsurroundings) {
		garrison.clear();
		replacegovernor(new HumanGovernor(this));
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
				Squad s = (Squad) World.get(x, y, Squad.class);
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
		if (realm != null) {
			realm = attacker.realm;
		}
		replacegovernor(new MonsterGovernor(this));
	}

	public void replacegovernor(Governor g) {
		for (Labor l : governor.getprojects()) {
			l.cancel();
		}
		governor = g;
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
		new TownScreen(this);
		for (final Combatant m : Squad.active.members) {
			if (m.source.fasthealing > 0) {
				m.hp = m.maxhp;
			}
		}
		return true;
	}

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
	public boolean isworking() {
		return !ishostile() && governor.getprojectssize() > 1;
	}

	@Override
	public Realm getrealmoverlay() {
		return ishostile() ? super.getrealmoverlay() : null;
	}

	@Override
	public Image getimage() {
		String image = "locationtown" + getrank().title;
		if (!ishostile() && ishosting()) {
			image += "festival";
		}
		return Images.getImage(image);
	}

	/**
	 * @return A rank between [1,4] based on current {@link #population}.
	 * @see Rank#RANKS
	 */
	public Rank getrank() {
		for (int i = 0; i < Rank.RANKS.length - 1; i++) {
			final Rank r = Rank.RANKS[i];
			if (population <= r.maxpopulation) {
				return r;
			}
		}
		return Rank.CITY;
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
		if (World.SCENARIO) {
			population = World.getscenariochallenge();
			int el = ChallengeRatingCalculator.leveltoel(population);
			Terrain t = Terrain.get(x, y);
			while (garrison.isEmpty()) {
				try {
					garrison.addAll(EncounterGenerator.generate(el, t));
				} catch (GaveUpException e) {
					el += 1;
				}
			}
			return;
		}
		Dwelling d = (Dwelling) findnearest(Dwelling.class);
		garrison.add(new Combatant(d.dweller.source.clone(), true));
		replacegovernor(new MonsterGovernor(this));
	}

	public static ArrayList<Town> gettowns() {
		ArrayList<Actor> actors = World.getall(Town.class);
		ArrayList<Town> towns = new ArrayList<Town>(actors.size());
		for (Actor a : actors) {
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
