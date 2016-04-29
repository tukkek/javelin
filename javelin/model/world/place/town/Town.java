package javelin.model.world.place.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.CastSpells;
import javelin.controller.action.world.UseItems;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.tournament.Exhibition;
import javelin.controller.tournament.Match;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.place.Outpost;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.manager.HumanManager;
import javelin.model.world.place.town.manager.MonsterManager;
import javelin.model.world.place.town.manager.TownManager;
import javelin.model.world.place.town.research.Grow;
import javelin.model.world.place.town.research.Research;
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
public class Town extends WorldPlace {
	private static final ArrayList<String> NAMES = new ArrayList<String>();
	private static final int STARTINGLAIRS = 3;
	static boolean startingtown = true;

	static {
		NAMES.add("Alexandria"); // my name :)
		NAMES.add("Lindblum"); // final fantasy 9
		NAMES.add("Sigil"); // planescape: torment
		NAMES.add("Reno");// fallout 2
		NAMES.add("Marrymore");// super mario rpg
		NAMES.add("Kakariko"); // zelda
		NAMES.add("the Citadel"); // mass effect
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

	public List<RecruitOption> lairs =
			new ArrayList<RecruitOption>(STARTINGLAIRS);
	public List<Upgrade> upgrades = new ArrayList<Upgrade>();
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
	 * nothing but grow TODO link
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
	public Transport transport = Transport.NONE;

	/**
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @param r
	 *            Type of town.
	 */
	public Town(final int x, final int y, Realm r) {
		super(NAMES.get(0));
		NAMES.remove(0);
		allowentry = false;
		this.x = x;
		this.y = y;
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
		} else {
			garrison.add(new Combatant(null, RPG.pick(lairs).m.clone(), true));
		}
		research.hand[0] = new Grow(this);
		draw();
	}

	public ArrayList<Monster> possiblerecruits(final int x, final int y) {
		ArrayList<Monster> recruits = new ArrayList<Monster>();
		String[] terrains = Javelin.terrains(Javelin.terrain(x, y));
		for (Monster m : Javelin.ALLMONSTERS) {
			for (String terrain : terrains) {
				if (m.terrains.contains(terrain)) {
					recruits.add(m);
					break;
				}
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
			TownUpgradingScreen.completetraining(t, this, t.trained).gold +=
					stash;
			stash = 0;
		}
		for (Order item : crafting.reclaim(Squad.active.hourselapsed)) {
			CraftingOrder o = (CraftingOrder) item;
			Item i = o.item;
			Town.grab(i);
		}
	}

	public static void grab(Item item) {
		Squad.active.equipment.get(Squad.active.members.get(CastSpells.choose(
				UseItems.listitems(null, false) + "\nWho will take the "
						+ item.toString().toLowerCase() + "?",
				Squad.active.members, true, true)).id).add(item);
	}

	@Override
	public boolean isupgrading() {
		return !training.done();
	}

	@Override
	public boolean iscrafting() {
		return !crafting.done();
	}

	/**
	 * @param restperiods
	 *            Normally 1 rest period equals to 8 hours of rest in normal
	 *            conditions.
	 * @param hours
	 *            Number of hours ellapsed.
	 */
	public static void rest(int restperiods, long hours) {
		for (final Combatant m : Squad.active.members) {
			int heal = m.source.hd.count() * restperiods;
			if (heal < 1) {
				heal = 1;
			}
			m.hp += heal;
			if (m.hp > m.maxhp) {
				m.hp = m.maxhp;
			}
			for (Spell p : m.spells) {
				p.used = 0;
			}
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
	 * @see #host()
	 */
	@Override
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
			TownUpgradingScreen.completetraining(to, this, to.trained);
		}
		labor += size / 10f;
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
		for (int i = 0; i < research.hand.length; i++) {
			research.hand[i] = null;
		}
		labor = 0;
		research.queue.clear();
		crafting.clear();
		training.clear();
		automanage = true;
		realm = null;// null in this case means human
		draw();
		if (showsurroundings) {
			Outpost.discover(x, y, Outpost.VISIONRANGE);
		}
	}

	void draw() {
		Research.draw(this);
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		if (attacker.realm == realm) {
			return Incursion.ignoreincursion(attacker);
		}
		if (!garrison.isEmpty()) {
			return Incursion.fight(attacker.getel(),
					ChallengeRatingCalculator.calculateElSafe(garrison));
		}
		// look for sleeping defense Squad
		for (int x = this.x - 1; x <= this.x + 1; x++) {
			for (int y = this.y - 1; y <= this.y + 1; y++) {
				Squad s = (Squad) WorldScreen.getactor(x, y, Squad.class);
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
		StateManager.save();
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
		return transport.equals(Transport.NONE) ? Transport.CARRIAGE
				: Transport.AIRSHIP;
	}
}
