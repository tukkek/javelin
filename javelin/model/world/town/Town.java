package javelin.model.world.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.action.world.CastSpells;
import javelin.controller.action.world.UseItems;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Siege;
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
import javelin.model.world.town.research.Grow;
import javelin.model.world.town.research.Research;
import javelin.view.screen.town.RecruitScreen;
import javelin.view.screen.town.TownScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitOption;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * A hub for upgrading units, resting, participating in tournament, renting
 * transportation, buying items...
 * 
 * Each town has it's own profile which is predetermined.
 * 
 * 
 * @author alex
 */
public class Town implements WorldActor {
	private static final ArrayList<String> NAMES = new ArrayList<String>();
	public static ArrayList<Town> towns = new ArrayList<Town>();
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

	public int x;
	public int y;
	public List<RecruitOption> lairs =
			new ArrayList<RecruitOption>(RecruitScreen.RECRUITSPERTOWN);
	public List<Option> upgrades = new ArrayList<Option>();
	public ItemSelection items = new ItemSelection();
	transient private Thing visual;
	public OrderQueue crafting = new OrderQueue();
	public OrderQueue training = new OrderQueue();
	/** gold in bank when all members of a squad are training */
	public int stash = 0;
	public ArrayList<Exhibition> events = new ArrayList<Exhibition>();
	public String name;
	public Realm realm;
	/**
	 * Represent a list of units positioned inside a town.
	 */
	public List<Combatant> garrison = new ArrayList<Combatant>();
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
	 * Represents a few {@link Research} options that the player or
	 * {@link #automanage}r can use to advance a town. Grow should always be
	 * first option. TODO link
	 * 
	 * A value of <code>null</code> means that the hand is initially empty, that
	 * it's card has been spent or that there are no more valid cards of that
	 * type.
	 * 
	 * These are the card types by index:
	 * 
	 * 0 - {@link Grow}
	 * 
	 * 1 - Native (from {@link #realm}) {@link Upgrade}.
	 * 
	 * 2 - Foreign {@link Upgrade}
	 * 
	 * 3 - Native {@link Item}.
	 * 
	 * 4 - Foreign {@link Item}
	 * 
	 * 5 - {@link Monster} lair, see {@link #lairs}
	 * 
	 * 6 - Special card
	 */
	public Research[] researchhand = new Research[7];
	public ResearchQueue researching = new ResearchQueue();
	/**
	 * Next task for the {@link #automanage}r.
	 */
	public Research nexttask;

	public Town(final int x, final int y, Realm colorp) {
		this.x = x;
		this.y = y;
		realm = colorp == null ? javelin.model.Realm.WIND : colorp;
		int nrecruits = RPG.r(3, 5);
		ArrayList<Monster> recruits = possiblerecruits(x, y);
		for (int i = 0; i < nrecruits; i++) {
			Monster recruit = recruits.get(i);
			lairs.add(new RecruitOption(recruit.name,
					100 * recruit.challengeRating, recruit));
		}
		if (startingtown) {
			startingtown = false;
		} else {
			garrison.add(new Combatant(null, RPG.pick(lairs).m.clone(), true));
		}
		researchhand[0] = new Grow(this);
		draw();
		towns.add(this);
		place();
		name = NAMES.get(0);
		NAMES.remove(0);
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

	public void enter(final Squad s) {
		if (ishostile()) {
			throw new StartBattle(new Siege(this));
		}
		reclaim();
		new TownScreen(this);
		if (!Squad.squads.isEmpty()) {
			for (final Combatant m : Squad.active.members) {
				if (m.source.fasthealing > 0) {
					m.hp = m.maxhp;
				}
			}
			reclaim();
			if (!s.members.isEmpty()) {
				s.updateavatar();
			}
		}
		StateManager.save();
	}

	/**
	 * Receives a {@link #name} from the user for this town.
	 */
	public void rename() {
		name = RecruitScreen
				.namingscreen(realm.toString().toLowerCase() + " town");
	}

	public void reclaim() {
		for (Order member : training.reclaim(Squad.active.hourselapsed)) {
			completetraining(member);
		}
		for (Order item : crafting.reclaim(Squad.active.hourselapsed)) {
			Item i = (Item) item.payload[0];
			i.produce(this);
			Town.grab(i);
		}
	}

	public static void grab(Item item) {
		Squad.active.equipment.get(Squad.active.members.get(CastSpells.choose(
				UseItems.listitems(null, false) + "\nWho will take the "
						+ item.toString().toLowerCase() + "?",
				Squad.active.members, true, true)).id).add(item);
	}

	public void completetraining(Order memberp) {
		Combatant member = (Combatant) memberp.payload[0];
		ArrayList<Item> equipment = (ArrayList<Item>) memberp.payload[1];
		ArrayList<Point> empty = new ArrayList<Point>();
		for (int deltax = -1; deltax <= +1; deltax++) {
			for (int deltay = -1; deltay <= +1; deltay++) {
				if (deltax == 0 && deltay == 0) {
					continue;
				}
				int x = this.x + deltax;
				int y = this.y + deltay;
				WorldActor stationed = WorldScreen.getactor(x, y, Squad.squads);
				if (stationed != null) {
					Squad.active.add(member, equipment);
					return;
				} else if (WorldScreen.getactor(x, y) == null) {
					empty.add(new Point(x, y));
				}
			}
		}
		/* TODO */
		assert !empty.isEmpty();
		Point destination = RPG.pick(empty);
		Squad s = new Squad(destination.x, destination.y,
				Math.round(Math.ceil(memberp.completionat / 24f) * 24));
		s.gold = stash;
		stash = 0;
		s.add(member, equipment);
		s.place();
	}

	@Override
	public void place() {
		visual = Lib.create("town");
		JavelinApp.overviewmap.addThing(visual, x, y);
	}

	@Override
	public int getx() {
		return x;
	}

	@Override
	public int gety() {
		return y;
	}

	@Override
	public void remove() {
		visual.remove();
		Town.towns.remove(this);
	}

	public boolean isupgrading() {
		return !training.done();
	}

	public boolean iscrafting() {
		return !crafting.done();
	}

	public static void rest(int restperioeds, long hours) {
		for (final Combatant m : Squad.active.members) {
			int heal = m.source.hd.count() * restperioeds;
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
		for (Town t : Town.towns) {
			if (!t.training.queue.isEmpty()) {
				Long candidate = t.training.next();
				if (candidate < next) {
					next = candidate;
				}
			}
		}
		return next == Long.MAX_VALUE ? null : next;
	}

	@Override
	public void move(int tox, int toy) {
		x = tox;
		y = toy;
	}

	/**
	 * @see #host()
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
	public String toString() {
		return name;
	}

	/**
	 * Produces {@link #labor} and {@link #automanage}s spending.
	 */
	public static void work() {
		for (Town t : towns) {
			t.labor += t.size / 10f;
			if (!t.researching.isEmpty() && Math
					.ceil(t.researching.get(0).price) <= Math.floor(t.labor)) {
				t.researching.get(0).finish(t, null);
				t.researching.remove(0);
			} else if (t.automanage) {
				t.manage();
			}
		}
	}

	void manage() {
		if (nexttask == null) {
			if (size < 3) {
				nexttask = researchhand[0];
			} else {
				int totalprice = 0;
				ArrayList<Research> hand = new ArrayList<Research>();
				for (Research r : researchhand) {
					if (r == null || !r.aiable) {
						continue;
					}
					totalprice += r.price;
					hand.add(r);
				}
				Collections.sort(hand, new Comparator<Research>() {
					@Override
					public int compare(Research o1, Research o2) {
						return Math.round(Math.round(o1.price - o2.price));
					}
				});
				double roll = RPG.r(0, totalprice);
				int forward = 0;
				for (int i = hand.size() - 1; i >= 0; i--) {
					roll -= hand.get(i).price;
					if (roll <= 0) {
						nexttask = hand.get(forward);
						break;
					}
					forward += 1;
				}
			}
		}
		if (nexttask.price <= labor) {
			nexttask.finish(this, null);
			nexttask = null;
		}
	}

	public boolean ishostile() {
		return !garrison.isEmpty();
	}

	/**
	 * When a player captures a hostile town.
	 * 
	 * @see #ishostile()
	 */
	public void capture() {
		nexttask = null;
		for (int i = 0; i < researchhand.length; i++) {
			researchhand[i] = null;
		}
		draw();
	}

	void draw() {
		Research.draw(this);
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		if (!ishostile()) {
			return true;
		}
		if (attacker.realm == realm) {
			return Incursion.ignoreincursion(attacker);
		}
		return Incursion.fight(attacker.determineel(),
				ChallengeRatingCalculator.calculateElSafe(garrison));
	}

	@Override
	public boolean ignore(Incursion attacker) {
		return ishostile() && realm == attacker.realm;
	}
}
