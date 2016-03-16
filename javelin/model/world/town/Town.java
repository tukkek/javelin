package javelin.model.world.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.action.world.CastSpells;
import javelin.controller.action.world.UseItems;
import javelin.controller.db.StateManager;
import javelin.controller.tournament.Exhibition;
import javelin.controller.tournament.Match;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
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
 * @author alex
 */
public class Town implements WorldActor {

	public int x;
	public int y;
	public List<RecruitOption> lairs =
			new ArrayList<RecruitOption>(RecruitScreen.RECRUITSPERTOWN);
	public List<Option> upgrades = new ArrayList<Option>();
	public ItemSelection items = new ItemSelection();
	public static ArrayList<Town> towns = new ArrayList<Town>();
	transient private Thing visual;
	public TownQueue crafting = new TownQueue();
	public TownQueue training = new TownQueue();
	/** gold in bank when all members of a squad are training */
	public int stash = 0;
	public ArrayList<Exhibition> events = new ArrayList<Exhibition>();
	public String name;
	public Realm realm;
	/**
	 * Represent a list of units positioned inside a town.
	 */
	public List<Combatant> garrison = new ArrayList<Combatant>();

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
		// Collections.sort(recruits, new Comparator<Monster>() {
		// @Override
		// public int compare(Monster o1, Monster o2) {
		// return new Float(o1.challengeRating)
		// .compareTo(o2.challengeRating);
		// }
		// });
		return recruits;
	}

	public void enter(final Squad s) {
		if (name == null) {
			name = RecruitScreen
					.namingscreen(realm.toString().toLowerCase() + " town");
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

	public void reclaim() {
		for (QueueItem member : training.reclaim(Squad.active.hourselapsed)) {
			completetraining(member);
		}
		for (QueueItem item : crafting.reclaim(Squad.active.hourselapsed)) {
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

	public void completetraining(QueueItem memberp) {
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

	@Override
	public String describe() {
		return name;
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
	 * Possibily starts a torunament in this town.
	 */
	public void host() {
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
}
