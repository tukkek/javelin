package javelin.model.world.location.town;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.fight.Siege;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.fight.tournament.Match;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.town.manager.HumanManager;
import javelin.model.world.location.town.manager.MonsterManager;
import javelin.model.world.location.town.manager.TownManager;
import javelin.model.world.location.town.research.Grow;
import javelin.model.world.location.town.research.Research;
import javelin.view.Images;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.RecruitScreen;
import javelin.view.screen.town.TownScreen;
import javelin.view.screen.town.option.RecruitOption;
import javelin.view.screen.upgrading.TownUpgradingScreen;
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
	 * {@value #DAILYLABOR}).
	 */
	public static final float DAILYLABOR = .1f;
	private static final ArrayList<String> NAMES = new ArrayList<String>();
	private static final int STARTINGLAIRS = 3;
	static boolean startingtown = true;

	static {
		initnames();
	}

	public List<RecruitOption> lairs =
			new ArrayList<RecruitOption>(STARTINGLAIRS);
	public HashSet<Upgrade> upgrades = new HashSet<Upgrade>();
	public ItemSelection items = new ItemSelection();
	public OrderQueue crafting = new OrderQueue();
	public OrderQueue training = new OrderQueue();
	/** gold in bank when all members of a squad are training */
	public int stash = 0;
	public ArrayList<Exhibition> events = new ArrayList<Exhibition>();
	/**
	 * Represent 10 working citizens that will produce 1 {@link #labor} every 10
	 * days.
	 * 
	 * An arbitrary decision is to try to fit the game-span of a normal game
	 * into a 1-year period, which puts a town max size roughly at 10 if it does
	 * nothing but {@link Grow}.
	 */
	public int size = 1;
	/**
	 * Can be used to improve a town. 1 unit represents 10 days of work by a
	 * 10-men crew.
	 */
	public float labor = 0.1f;
	public boolean automanage = true;

	/**
	 * Contains {@link Research} data structures.
	 */
	public ResearchData research = new ResearchData();
	/**
	 * Next task for the {@link #automanage}r.
	 */
	public Research nexttask;
	/** Higher levels allow better rest. */
	public Accommodations lodging = Accommodations.LODGE;
	/** Higher levels allow better travel methods. */
	public Transport transport = null;

	/**
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @param r
	 *            Type of town.
	 */
	public Town(final int x, final int y, Realm r) {
		super(NAMES.isEmpty() ? null : NAMES.get(0));
		if (!NAMES.isEmpty()) {
			NAMES.remove(0);
		}
		allowentry = false;
		this.x = x;
		this.y = y;
		if (!World.seed.done) {
			checktooclose();
		}
		realm = r;
		gossip = true;
		discard = false;
		ArrayList<Monster> recruits = possiblerecruits(x, y);
		for (int i = 0; i < STARTINGLAIRS; i++) {
			Monster recruit = recruits.get(i);
			lairs.add(new RecruitOption(recruit.name,
					100 * recruit.challengeRating, recruit));
		}
		if (startingtown) {
			startingtown = false;
		} else if (!Preferences.DEBUGCLEARGARRISON) {
			garrison.add(new Combatant(RPG.pick(lairs).m.clone(), true));
		}
		draw();
	}

	void checktooclose() {
		for (WorldActor town : WorldActor.getall(Town.class)) {
			if (town != this && distance(town.x, town.y) <= 2) {
				throw new RestartWorldGeneration();
			}
		}
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

	ArrayList<Monster> possiblerecruits(final int x, final int y) {
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
		description = RecruitScreen.namingscreen(toString());
	}

	public void reclaim() {
		for (Order o : training.reclaim(Squad.active.hourselapsed)) {
			TrainingOrder t = (TrainingOrder) o;
			pickstash(TownUpgradingScreen.completetraining(t, this, t.trained));
		}
		for (Order item : crafting.reclaim(Squad.active.hourselapsed)) {
			CraftingOrder o = (CraftingOrder) item;
			Item i = o.item;
			i.grab();
		}
	}

	@Override
	public boolean hasupgraded() {
		return training.ready();
	}

	@Override
	public boolean hascrafted() {
		return crafting.ready();
	}

	/**
	 * @param restperiods
	 *            Normally 1 rest period equals to 8 hours of rest in normal
	 *            conditions.
	 * @param hours
	 *            Number of hours elapsed.
	 * @param accomodation
	 *            Level of the resting environment.
	 */
	public static void rest(int restperiods, long hours, Accommodations a) {
		for (final Combatant c : Squad.active.members) {
			int heal = c.source.hd.count() * restperiods;
			if (a != Accommodations.HOSPITAL && c.heal() >= 15) {
				heal *= 2;
			}
			if (heal < 1) {
				heal = 1;
			}
			c.hp += heal;
			if (c.hp > c.maxhp) {
				c.hp = c.maxhp;
			}
			for (Spell p : c.spells) {
				p.used = 0;
			}
			if (c.source.poison > 0) {
				int detox = restperiods == 1 ? RPG.r(0, 1) : restperiods / 2;
				c.detox(Math.min(c.source.poison, detox));
			}
			c.terminateconditions((int) hours);
		}
		Squad.active.hourselapsed += hours;
	}

	/**
	 * Finish the next training that is in-progress.
	 * 
	 * @return Time of completion.
	 */
	public static Long train() {
		Long next = Long.MAX_VALUE;
		for (WorldActor p : getall(Town.class)) {
			Town t = (Town) p;
			if (!t.training.queue.isEmpty()) {
				Long candidate = t.training.next();
				if (candidate < next) {
					next = candidate;
				}
			}
		}
		return next == Long.MAX_VALUE ? null : next;
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
		for (Order item : training.reclaim(time)) {
			TrainingOrder to = (TrainingOrder) item;
			pickstash(
					TownUpgradingScreen.completetraining(to, this, to.trained));
		}
		labor += size * DAILYLABOR;
		if (!research.queue.isEmpty() && Math
				.ceil(research.queue.get(0).price) <= Math.floor(labor)) {
			research.queue.get(0).finish(this, null);
			research.queue.remove(0);
		} else if (automanage) {
			TownManager m =
					ishostile() ? new MonsterManager() : new HumanManager();
			m.manage(this);
		}
	}

	void pickstash(Squad s) {
		s.gold += stash;
		stash = 0;
	}

	/**
	 * When a player captures a hostile town.
	 * 
	 * @param showsurroundings
	 *            if <code>true</code> will show this town's surrounding
	 *            squares.
	 * @see #ishostile()
	 */
	public void capture(boolean showsurroundings) {
		nexttask = null;
		labor = 0;
		research.queue.clear();
		crafting.clear();
		training.clear();
		automanage = true;
		// realm = null;// null in this case means human
		redraw();
		if (showsurroundings) {
			Outpost.discover(x, y, Outpost.VISIONRANGE);
		}
	}

	/**
	 * Draws empty {@link Research} cards.
	 */
	public void draw() {
		research.hand[0] = new Grow(this);
		Research.draw(this);
	}

	/** Discard all {@link Research} cards and {@link #draw()}. */
	public void redraw() {
		for (int i = 0; i < research.hand.length; i++) {
			research.hand[i] = null;
		}
		draw();
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		if (attacker.realm == realm) {
			return Incursion.ignoreincursion(attacker);
		}
		if (!garrison.isEmpty()) {
			return Incursion.fight(attacker.getel(),
					ChallengeRatingCalculator.calculateel(garrison));
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
		capture(false);
		realm = attacker.realm;
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
		reclaim();
		new TownScreen(this);
		if (!Squad.getall(Squad.class).isEmpty()) {
			for (final Combatant m : Squad.active.members) {
				if (m.source.fasthealing > 0) {
					m.hp = m.maxhp;
				}
			}
			reclaim();
			if (!Squad.active.members.isEmpty()) {
				Squad.active.updateavatar();
			}
		}
		Javelin.app.switchScreen(BattleScreen.active);
		return true;
	}

	/**
	 * Note that this method doesn't actually change {@link #lodging}.
	 * 
	 * @return The next step on the {@link #lodging} tree, up to
	 *         {@link Accommodations#HOSPITAL}.
	 */
	public Accommodations upgradeinn() {
		return lodging.equals(Accommodations.LODGE) ? Accommodations.HOTEL
				: Accommodations.HOSPITAL;
	}

	/**
	 * Note that this method doesn't actually change {@link #transport}.
	 * 
	 * @return The next step on the {@link #transport} tree, up to
	 *         {@link Transport#AIRSHIP}.
	 */
	public Transport upgradetransport() {
		if (transport == null) {
			return Transport.CARRIAGE;
		}
		return transport.equals(Transport.CARRIAGE) ? Transport.SHIP
				: Transport.AIRSHIP;
	}

	@Override
	protected Siege fight() {
		Siege f = new Siege(this);
		f.bribe = false;
		return f;
	}

	/**
	 * Adds a Worker units to the active {@link Squad}.
	 * 
	 * @param t
	 *            If not <code>null</code> will reduce town {@link #size} by 1.
	 *            If already at 1 (minimum) returns without any effect.
	 */
	static public void getworker(Town t) {
		if (t != null) {
			if (t.size == 1) {
				return;
			}
			t.size -= 1;
		}
		Squad.active.members
				.add(new Combatant(Javelin.getmonster("Worker"), false));
	}

	@Override
	public List<Combatant> getcombatants() {
		if (ishostile()) {
			return garrison;
		}
		ArrayList<Combatant> combatants =
				new ArrayList<Combatant>(training.queue.size());
		for (Order o : training.queue) {
			combatants.add(((TrainingOrder) o).untrained);
		}
		return combatants;
	}

	/**
	 * @return <code>true</code> if it's probably a good idea for a player to
	 *         return here and manager this town.
	 * @see #automanage
	 * @see #labor
	 * @see TownManager
	 */
	public boolean haslabor() {
		return !ishostile() && !automanage && labor >= size;
	}

	@Override
	public Realm getrealmoverlay() {
		return ishostile() ? super.getrealmoverlay() : null;
	}

	@Override
	public Image getimage() {
		String image = "locationtown";
		if (size <= 5) {
			image += "hamlet";
		} else if (size <= 10) {
			image += "village";
		} else if (size <= 15) {
			image += "town";
		} else {
			image += "city";
		}
		if (!ishostile() && ishosting()) {
			image += "festival";
		}
		return Images.getImage(image);
	}
}
