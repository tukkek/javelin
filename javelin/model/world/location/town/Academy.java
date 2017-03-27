package javelin.model.world.location.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.transport.Transport;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.order.TrainingOrder;
import javelin.view.screen.upgrading.AcademyScreen;
import tyrant.mikera.engine.RPG;

/**
 * A place where units can go to learn about a general topic - be it physical
 * feats or intellectual or magical prowess.
 * 
 * @author alex
 */
public abstract class Academy extends Fortification {
	private static final Comparator ALPHABETICALSORT = new Comparator<Upgrade>() {
		@Override
		public int compare(Upgrade o1, Upgrade o2) {
			return o1.name.compareTo(o2.name);
		}
	};

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
				Squad stationed = (Squad) WorldActor.get(x, y, Squad.class);
				if (stationed == null) {
					if (WorldActor.get(x, y) == null) {
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
		for (WorldActor actor : WorldActor.getall()) {
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

}