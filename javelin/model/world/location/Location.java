package javelin.model.world.location;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Siege;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.Images;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * A {@link WorldActor} that is actually a place that represent a location to be
 * entered or explored.
 * 
 * @see WorldActor#getall()
 * @author alex
 */
public abstract class Location extends WorldActor {
	/**
	 * Note that this value is used in a triagonal calculation, not in
	 * square-steps.
	 */
	static final int CLOSE = 4;

	/**
	 * If <code>false</code> will make sure no {@link Squad} occupies the same
	 * {@link World} space as this.
	 */
	public boolean allowentry = true;

	/**
	 * Represent a list of units positioned inside a world feature.
	 */
	public List<Combatant> garrison = new ArrayList<Combatant>();
	/**
	 * If <code>true</code>, the computer will destroy this instead of
	 * positioning a {@link #garrison} here.
	 * 
	 * @see #destroy(javelin.model.world.Incursion)
	 */
	public boolean sacrificeable = false;
	/**
	 * If this feature should be {@link #remove()}d after {@link #interact()}.
	 */
	public boolean discard = true;
	/**
	 * Used for {@link #toString()}.
	 */
	public String description;
	/**
	 * Allows use of {@link Skills#gatherinformation}.
	 */
	public boolean gossip = false;

	/**
	 * Used to calculate fog of war. Usually rangess from 1 (most
	 * {@link Fortification}s, to 3 ({@link Outpost}s), with {@link Town}s being
	 * the benchmark for 2. {@link UniqueLocation}s usually have none since
	 * they're minding their own business.
	 */
	public int vision = 0;

	/**
	 * @param descriptionknown
	 *            What to show a player on a succesfull {@link Skills#knowledge}
	 *            check.
	 * @param descriptionunknown
	 *            What to show otherwise.
	 */
	public Location(String description) {
		this.description = description;
	}

	/**
	 * Determines {@link World} location for this.
	 */
	protected void generate() {
		generate(this, false);
	}

	/**
	 * Default implementation of {@link #generate()}, will try random
	 * positioning a {@link WorldActor} until it lands on a free space.
	 * 
	 * @param allowwater
	 *            <code>true</code> if it is allowed to place the actor on
	 *            {@link Terrain#WATER}.
	 */
	static public void generate(WorldActor p, boolean allowwater) {
		p.x = -1;
		ArrayList<WorldActor> actors = WorldActor.getall();
		actors.remove(p);
		while (p.x == -1
				|| (!allowwater && World.seed.map[p.x][p.y]
						.equals(Terrain.WATER))
				|| WorldActor.get(p.x, p.y, actors) != null || neartown(p)) {
			p.x = RPG.r(0, World.SIZE - 1);
			p.y = RPG.r(0, World.SIZE - 1);
			World.retry();
		}
	}

	/**
	 * @return <code>true</code> if given actor is too close to a town.
	 */
	static boolean neartown(WorldActor p) {
		if (p instanceof Town) {
			return false;
		}
		for (WorldActor a : Location.getall(Town.class)) {
			Town t = (Town) a;
			if (t.x - 1 <= p.x && p.x <= t.x + 1 && t.y - 1 <= p.y
					&& p.y <= t.y + 1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean interact() {
		return interact(this);
	}

	/**
	 * Called when this place is reached with the active Squad.
	 * 
	 * @throws StartBattle
	 * @return <code>true</code> if the {@link Squad} is able to interact with
	 *         this place, <code>false</code> if a {@link #garrison} defends it.
	 */
	public static boolean interact(Location p) {
		if (p.defend()) {
			return false;
		}
		if (p.gossip) {
			WorldActor closest = null;
			for (int x = p.x - 5; x <= p.x + 5; x++) {
				for (int y = p.y - 5; y <= p.y + 5; y++) {
					if (!WorldScreen.see(new Point(x, y))) {
						WorldActor a = WorldActor.get(x, y);
						if (a == null) {
							continue;
						}
						if (closest == null
								|| Walker.distance(p.x, p.y, a.x, a.y) < Walker
										.distance(p.x, p.y, closest.x,
												closest.y)) {
							closest = a;
						}
					}
				}
			}
			if (closest != null) {
				final double distance =
						Walker.distance(p.x, p.y, closest.x, closest.y);
				if (Squad.active.gossip() >= 10 + distance) {
					WorldScreen.setVisible(closest.x, closest.y);
				}
			}
		}
		if (p.discard) {
			p.remove();
		}
		return true;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		if (impermeable || attacker.realm == realm) {
			return Incursion.ignoreincursion(attacker);
		}
		int el = attacker.getel();
		if (!garrison.isEmpty()) {
			return Incursion.fight(el,
					ChallengeRatingCalculator.calculateel(garrison));
		}
		if (sacrificeable) {
			return Incursion.fight(el, getel(el));
		}
		garrison.addAll(attacker.squad);
		realm = attacker.realm;
		return false;
	}

	/**
	 * Only called for {@link #sacrificeable} places when being reached by an
	 * {@link Incursion}.
	 * 
	 * @param attackerel
	 *            Given an attacking encounter level...
	 * @return the defending encounter level. A <code>null</code> value is
	 *         allowed because some places should not be conquered.
	 *         {@link Integer#MIN_VALUE} means an automatic victory for the
	 *         attacker.
	 * @see WorldActor#impermeable
	 * @see Incursion#fight(int, int)
	 */
	abstract protected Integer getel(int attackerel);

	/**
	 * @return <code>true</code> if this place needs to be conquered.
	 */
	public boolean ishostile() {
		return !garrison.isEmpty();
	}

	/**
	 * @throws StartBattle
	 *             If the player chooses to attack a defended location.
	 * @return <code>false</code> if this is unguarded, <code>true</code> if it
	 *         is guarded but the player aborted combat.
	 */
	protected boolean defend() {
		if (!ishostile()) {
			return false;
		}
		if (headsup(garrison, toString())) {
			throw new StartBattle(fight());
		}
		throw new RuntimeException("headsup sould throw #wplace");
	}

	/**
	 * @return Battle controller.
	 * @throws RepeatTurn
	 */
	protected Siege fight() {
		return new Siege(this);
	}

	/**
	 * Offers information and a chance to back out of the fight.
	 * 
	 * @param opponents
	 *            Will be {@link Squad#spot}ted.
	 * @param description
	 *            What to describe this place as.
	 * @return <code>true</code> if player confirms engaging in battle.
	 * @throws RepeatTurn
	 */
	public static boolean
			headsup(List<Combatant> opponents, String description) {
		opponents.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		Game.messagepanel.clear();
		Game.message(describe(opponents, description) + "\n\n"
				+ "Press s to storm or any other key to retreat.", Delay.NONE);
		if (InfoScreen.feedback() == 's') {
			return true;
		}
		throw new RepeatTurn();
	}

	static String describe(List<Combatant> opponents, String description) {
		return description + ". Forces: ("
				+ ChallengeRatingCalculator.describedifficulty(opponents)
				+ " fight)\n\n" + Squad.active.spot(opponents);
	}

	@Override
	public String describe() {
		String description = toString();
		if (!garrison.isEmpty()) {
			return describe(garrison, description);
		}
		if (!description.endsWith(".")) {
			description += ".";
		}
		return description;
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public Image getimage() {
		return Images.getImage("location"
				+ getClass().getSimpleName().toLowerCase());
	}

	/**
	 * Convenience method for subclasses to use as {@link #generate()}. Warning:
	 * when using this do not override {@link #generate(Location)}!
	 */
	protected void generateawayfromtown() {
		x = -1;
		while (x == -1 || neartown(this)) {
			generate(this, false);
		}
	}

	/**
	 * @param targets
	 *            Class of other places to verify if nearby.
	 * @return <code>true</code> if there is one of the given places under
	 *         {@value #CLOSE} distance from here.
	 * @see Walker#distance(int, int, int, int)
	 */
	public boolean isnear(Class<? extends Location> targets) {
		for (WorldActor p : getall(targets)) {
			if (p != this && Walker.distance(x, y, p.x, p.y) <= CLOSE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Total number of places in the game {@link World}.
	 */
	public static int count() {
		int sum = 0;
		for (ArrayList<WorldActor> places : INSTANCES.values()) {
			sum += places.size();
		}
		return sum;
	}

	/**
	 * @return <code>true</code> if a item icon is to be displayed.
	 */
	public boolean hascrafted() {
		return false;
	}

	/**
	 * @return <code>true</code> if a arrow icon is to be displayed.
	 */
	public boolean hasupgraded() {
		return false;
	}

	/**
	 * @return <code>true</code> if should render the {@link Images#HOSTILE}
	 *         overlay.
	 */
	public boolean drawgarisson() {
		return ishostile();
	}

	@Override
	public void place() {
		if (x == -1) {
			generate();
		}
		super.place();
	}

	public int distanceinsteps(int xp, int yp) {
		return Math.max(Math.abs(xp - x), Math.abs(yp - y));
	}

	public boolean view() {
		return !ishostile() && vision > 0;
	}

	public void capture() {
		garrison.clear();
		realm = null; // TODO really?
	}
}