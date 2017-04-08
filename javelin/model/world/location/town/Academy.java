package javelin.model.world.location.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.labor.Build;
import javelin.model.world.location.town.labor.BuildingUpgrade;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

/**
 * A place where units can go to learn about a general topic - be it physical
 * feats or intellectual or magical prowess.
 *
 * @author alex
 */
public class Academy extends Fortification {
	private static final Comparator<Upgrade> ALPHABETICALSORT = new Comparator<Upgrade>() {
		@Override
		public int compare(Upgrade o1, Upgrade o2) {
			return o1.name.compareTo(o2.name);
		}
	};

	/**
	 * Builds one academy of this type. Since cannot have only 1 instance,
	 * {@link #getacademy()} needs to be defined by subclasses.
	 *
	 * @see BuildAcademies
	 * @author alex
	 */
	public abstract static class BuildAcademy extends Build {
		public Academy goal;

		public BuildAcademy(Rank minimumrank) {
			super("", 0, null, minimumrank);
		}

		@Override
		protected void define() {
			goal = getacademy();
			super.define();
			if (goal.upgrades.isEmpty()) {
				goal.setrealm(town.originalrealm);
			}
			cost = goal.upgrades.size();
			name = "Build " + goal.descriptionknown.toLowerCase();
		}

		protected abstract Academy getacademy();

		@Override
		public Location getgoal() {
			return goal;
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d)
					&& validatecount(d.getlocationtype(goal.getClass()), d);
		}

		protected boolean validatecount(ArrayList<Location> count, District d) {
			return count.isEmpty();
		}
	}

	/**
	 * Like {@link BuildAcademy} except allows for 1 academy of the given type
	 * per town rank.
	 *
	 * @author alex
	 */
	public abstract static class BuildAcademies extends BuildAcademy {
		public BuildAcademies(Rank minimumrank) {
			super(minimumrank);
		}

		@Override
		protected boolean validatecount(ArrayList<Location> count, District d) {
			if (count.size() >= d.town.getrank().rank) {
				return false;
			}
			for (Location l : count) {
				Academy a = (Academy) l;
				if (a.descriptionknown.equals(goal.descriptionknown)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Builds a common, base-class {@link Academy}.
	 *
	 * @author alex
	 */
	public static class BuildCommonAcademy extends BuildAcademy {
		public BuildCommonAcademy() {
			super(Rank.HAMLET);
		}

		@Override
		protected Academy getacademy() {
			return new Academy(null);
		}
	}

	class UpgradeCommonAcademy extends BuildingUpgrade {
		public UpgradeCommonAcademy(int cost, Academy previous) {
			super("", cost, +cost, previous, Rank.HAMLET);
			name = "Upgrade academy";
		}

		@Override
		public Location getgoal() {
			return previous;
		}

		@Override
		public boolean validate(District d) {
			return super.validate(d) && cost > 0;
		}

		@Override
		public void done() {
			super.done();
			((Academy) previous).level += cost;
			refill();
		}
	}

	/** Currently training unit. */
	public OrderQueue training = new OrderQueue();
	/** Money {@link #training} unit had before entering here (if alone). */
	public int stash;
	/** Upgrades that can be learned here. */
	public HashSet<Upgrade> upgrades;
	/** If <code>true</code> will allow the academy to be pillaged for money. */
	public boolean pillage = true;
	/** If a single unit parks with a vehicle here it is stored. */
	public Transport parking = null;
	public int level = 0;
	protected boolean allowupgrade = false;
	Realm upgradetype;

	/**
	 * Builds a basic, upgradeable academy.
	 *
	 * @param r
	 *            Type of upgrade to offer. If you choose to set this as
	 *            <code>null</code>, you need to manually call
	 *            {@link #setrealm(Realm)} later on.
	 *
	 * @see BuildAcademy
	 * @see UpgradeHandler
	 */
	public Academy(Realm r) {
		this("An academy", "An academy", 0, 0, new HashSet<Upgrade>(), null,
				null);
		allowupgrade = true;
		// level = 10;
		if (r != null) {
			setrealm(r);
		}
	}

	/**
	 * See {@link Fortification#Fortification(String, String, int, int)}.
	 *
	 * @param upgradesp
	 * @param classadvancement
	 * @param raiseability
	 */
	public Academy(String descriptionknown, String descriptionunknown,
			int minlevel, int maxlevel, HashSet<Upgrade> upgradesp,
			RaiseAbility raiseability, ClassAdvancement classadvancement) {
		super(descriptionknown, descriptionunknown, minlevel, maxlevel);
		upgrades = new HashSet<Upgrade>(upgradesp);
		if (raiseability != null) {
			upgrades.add(raiseability);
		}
		if (classadvancement != null) {
			upgrades.add(classadvancement);
		}
		// sort(upgrades);
		sacrificeable = false;
		level = upgrades.size();
	}

	public void setrealm(Realm r) {
		upgradetype = r;
		level = minlevel = maxlevel = Math.min(10, getupgrades(r).size());
		if (minlevel > 1) {
			minlevel -= 1;
		}
		maxlevel += 1;
		refill();
	}

	/**
	 * @param upgrades
	 *            {@link #upgrades}, to be sorted.
	 */
	public void sort(ArrayList<Upgrade> upgrades) {
		upgrades.sort(ALPHABETICALSORT);
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		for (Order o : training.reclaim(Squad.active.hourselapsed)) {
			completetraining((TrainingOrder) o);
		}
		getscreen().show();
		return true;
	}

	protected AcademyScreen getscreen() {
		return new AcademyScreen(this, null);// TODO shows progress
	}

	Squad completetraining(TrainingOrder next) {
		Squad s = moveout(next, next.trained);
		s.gold += stash;
		stash = 0;
		if (parking != null) {
			if (s.transport == null) {
				s.transport = parking;
				parking = null;
				s.updateavatar();
			} else if (parking.price > s.transport.price) {
				Transport swap = parking;
				parking = s.transport;
				s.transport = swap;
				s.updateavatar();
			}
		}
		return s;
	}

	@Override
	public boolean hasupgraded() {
		return training.reportanydone();
	}

	@Override
	public List<Combatant> getcombatants() {
		ArrayList<Combatant> combatants = new ArrayList<Combatant>(garrison);
		for (Order o : training.queue) {
			TrainingOrder next = (TrainingOrder) o;
			combatants.add(next.untrained);
		}
		return combatants;
	}

	/**
	 * @return <code>true</code> if already has the maximum number of upgrades.
	 */
	public boolean full() {
		return upgrades.size() >= 9;
	}

	/**
	 * Applies the upgrade and adjustments. Currently never creates a new squad
	 * because this isn't being called from {@link WorldActor#turn(long,
	 * javelin.view.screen.WorldScreen).}
	 *
	 * @param o
	 *            Training information.
	 * @param member
	 *            Joins a nearby {@link Squad} or becomes a new one.
	 * @param p
	 *            Place the training was realized.
	 * @param member
	 *            Member to be returned (upgraded or not, in case of cancel).
	 * @return The Squad the trainee is now into.
	 */
	public Squad moveout(TrainingOrder o, Combatant member) {
		ArrayList<Point> free = new ArrayList<Point>();
		for (int deltax = -1; deltax <= +1; deltax++) {
			for (int deltay = -1; deltay <= +1; deltay++) {
				if (deltax == 0 && deltay == 0) {
					continue;
				}
				int x = this.x + deltax;
				int y = this.y + deltay;
				if (!World.validatecoordinate(x, y)
						|| Terrain.get(x, y).equals(Terrain.WATER)) {
					continue;
				}
				Squad stationed = (Squad) World.get(x, y, Squad.class);
				if (stationed == null) {
					if (World.get(x, y) == null) {
						free.add(new Point(x, y));
					}
				} else {
					stationed.add(member, o.equipment);
					return stationed;
				}
			}
		}
		Point destination = free.isEmpty() ? getlocation() : RPG.pick(free);
		Squad s = new Squad(destination.x, destination.y,
				Math.round(Math.ceil(o.completionat / 24f) * 24), null);
		s.add(member, o.equipment);
		s.place();
		if (free.isEmpty()) {
			s.displace();
		}
		return s;
	}

	@Override
	protected Integer getel(int attackerel) {
		return ChallengeRatingCalculator.calculateel(getcombatants());
	}

	@Override
	protected void captureforai(Incursion attacker) {
		super.captureforai(attacker);
		training.clear();
		stash = 0;
		parking = null;
	}

	/**
	 * Normally {@link #training} units don't get out of the academy by
	 * themselves since this would mean being alone in the wild but if the game
	 * is about to be lost due to the absence of {@link Squad}s then the unit
	 * gets out to prevent the game from ending.
	 *
	 * @return <code>false</code> if there was no unit in {@link #training}.
	 */
	public static boolean train() {
		boolean trained = false;
		for (Actor actor : World.getall()) {
			if (actor instanceof Academy) {
				Academy a = (Academy) actor;
				/* don't inline */
				for (Order order : a.training.queue) {
					TrainingOrder o = (TrainingOrder) order;
					a.completetraining(o).hourselapsed = Math
							.max(o.completionat, Squad.active.hourselapsed);
					trained = true;
				}
				a.training.clear();
			}
		}
		return trained;
	}

	void refill() {
		ArrayList<Upgrade> upgrades = new ArrayList<Upgrade>(
				getupgrades(upgradetype));
		if (this.upgrades.isEmpty()) {
			for (Upgrade u : upgrades) {
				if (u instanceof ClassAdvancement) {
					this.upgrades.add(u);
					break;
				}
			}
		}
		Collections.shuffle(upgrades);
		for (Upgrade u : upgrades) {
			if (this.upgrades.size() >= level) {
				break;
			}
			this.upgrades.add(u);
		}

	}

	@Override
	public ArrayList<Labor> getupgrades(District d) {
		ArrayList<Labor> getupgrades = super.getupgrades(d);
		if (allowupgrade && upgrades.size() <= d.town.getrank().maxpopulation) {
			getupgrades.add(new UpgradeCommonAcademy(
					getupgrades(upgradetype).size() - upgrades.size(), this));
		}
		return getupgrades;
	}

	static HashSet<Upgrade> getupgrades(Realm r) {
		return UpgradeHandler.singleton.getupgrades(r);
	}

	@Override
	public boolean isworking() {
		return !training.queue.isEmpty() && !training.reportalldone();
	}
}