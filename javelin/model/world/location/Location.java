package javelin.model.world.location;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.WorldGenerator;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Siege;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.Images;
import javelin.view.mappanel.world.WorldMouse;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * A {@link Actor} that is actually a place that represent a location to be
 * entered or explored.
 *
 * Im may be tempted to add a uniform #level here for subclass convenience, but
 * having the same on each subclass that needs it bring more benefits, from code
 * efficiency to ease of searching calls and assignments. The very small
 * convenience added is not worth the trade-off.
 *
 * @see World#getactors()
 * @author alex
 */
public abstract class Location extends Actor {
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

	/** Link with a road if nearby. */
	public boolean link = true;

	/**
	 * If <code>false</code>, will not show individual units.
	 * 
	 * @see WorldMouse
	 * @see Squad#spot(List)
	 */
	protected boolean showgarrison = true;

	/**
	 * @param descriptionknown
	 *            What to show a player on a succesfull {@link Skills#knowledge}
	 *            check.
	 * @param descriptionunknown
	 *            What to show otherwise.
	 */
	public Location(String description) {
		super();
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
	 * positioning a {@link Actor} until it lands on a free space.
	 *
	 * @param allowwater
	 *            <code>true</code> if it is allowed to place the actor on
	 *            {@link Terrain#WATER}.
	 */
	static public void generate(Actor p, boolean allowwater) {
		p.x = -1;
		ArrayList<Actor> actors = World.getactors();
		actors.remove(p);
		final World w = World.getseed();
		int size = World.scenario.size - 1;
		while (p.x == -1
				|| !allowwater
						&& World.getseed().map[p.x][p.y].equals(Terrain.WATER)
				|| World.get(p.x, p.y, actors) != null || neartown(p)
				|| w.roads[p.x][p.y] || w.highways[p.x][p.y]) {
			p.x = RPG.r(0, size);
			p.y = RPG.r(0, size);
			WorldGenerator.retry();
		}
	}

	/**
	 * @return <code>true</code> if given actor is too close to a town.
	 */
	static boolean neartown(Actor p) {
		return p.getdistrict() != null;
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
			Actor closest = null;
			ArrayList<Actor> actors = World.getactors();
			for (int x = p.x - 5; x <= p.x + 5; x++) {
				for (int y = p.y - 5; y <= p.y + 5; y++) {
					if (!WorldScreen.see(new Point(x, y))) {
						Actor a = World.get(x, y, actors);
						if (a == null) {
							continue;
						}
						if (closest == null || Walker.distance(p.x, p.y, a.x,
								a.y) < Walker.distance(p.x, p.y, closest.x,
										closest.y)) {
							closest = a;
						}
					}
				}
			}
			if (closest != null) {
				final double distance = Walker.distance(p.x, p.y, closest.x,
						closest.y);
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
		if (sacrificeable) {
			int el = attacker.getel();
			return Incursion.fight(el, getel(el));
		}
		captureforai(attacker);
		return false;
	}

	/** TODO could probably merge this and {@link #capture()}. */
	protected void captureforai(Incursion attacker) {
		garrison.addAll(attacker.squad);
		realm = attacker.realm;
	}

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
		if (Preferences.DEBUGDISABLECOMBAT) {
			capture();
			return false;
		}
		if (headsup(garrison, toString(), showgarrison, this)) {
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
	static public boolean headsup(List<Combatant> opponents, String description,
			boolean showgarrison, Actor a) {
		opponents.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		Game.messagepanel.clear();
		final String prompt = describe(opponents, description, showgarrison, a)
				+ "\n\nPress s to storm or any other key to retreat.";
		Game.message(prompt, Delay.NONE);
		if (InfoScreen.feedback() == 's') {
			return true;
		}
		throw new RepeatTurn();
	}

	static String describe(List<Combatant> opponents, String name,
			boolean showgarrison, Actor a) {
		String description = name;
		if (!opponents.isEmpty()) {
			description += ". Forces: ("
					+ ChallengeCalculator.describedifficulty(opponents)
					+ " fight)";
			if (showgarrison) {
				description += "\n\n" + Squad.active.spot(opponents, a);
			}
			return description;
		}
		if (!description.endsWith(".")) {
			description += ".";
		}
		return description;
	}

	@Override
	public String describe() {
		return describe(garrison, toString(), showgarrison, this);
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public Image getimage() {
		return Images.getImage(
				"location" + getClass().getSimpleName().toLowerCase());
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
	public boolean isnear(Class<? extends Location> targets, int distance) {
		Actor nearest = findnearest(targets);
		return nearest != null && distance(nearest.x, nearest.y) > distance;
	}

	public boolean isnear(Class<? extends Location> targets) {
		return isnear(targets, CLOSE);
	}

	/**
	 * @return Total number of places in the game {@link World}.
	 */
	public static int count() {
		int sum = 0;
		for (ArrayList<Actor> places : World.getseed().actors.values()) {
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

	public boolean view() {
		return !ishostile() && vision > 0;
	}

	/**
	 * Clear {@link #garrison} and {@link Realm}.
	 */
	public void capture() {
		garrison.clear();
		realm = null;
	}

	/**
	 * Should only be called from a valid {@link District}.
	 *
	 * @param d
	 *
	 * @return Any upgrades this location may be improved with.
	 */
	public ArrayList<Labor> getupgrades(District d) {
		return new ArrayList<Labor>(0);
	}

	public void rename(String name) {
		description = name;
	}

	public boolean isworking() {
		return false;
	}

	public boolean canupgrade() {
		return !ishostile();
	}

	public void spawn() {
		if (realm == null || !ishostile() || garrison.isEmpty()) {
			return;
		}
		Combatant spawn = RPG.pick(garrison);
		Float cr = spawn.source.cr;
		if (!RPG.chancein(Math.round(400 * cr / 20))) {
			return;
		}
		Location reinforce = this;
		for (Town t : Town.gettowns()) {
			if (t != this && t.realm == realm && t.population >= cr
					&& t.getel(0) < ChallengeCalculator
							.calculateel(reinforce.garrison)) {
				reinforce = t;
			}
		}
		if (Javelin.DEBUG) {
			System.out.println("Spawning a " + spawn + " (cr " + cr + ") from "
					+ this + " (el " + ChallengeCalculator.calculateel(garrison)
					+ ") to " + reinforce + " (cr "
					+ ChallengeCalculator.calculateel(reinforce.garrison)
					+ ")");
		}
		reinforce.garrison.add(new Combatant(spawn.source, true));
		Incursion.raid(reinforce);
	}
}