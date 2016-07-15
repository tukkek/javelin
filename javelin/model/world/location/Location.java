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
import javelin.model.world.location.town.Town;
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
	protected boolean discard = true;
	/**
	 * Used for {@link #toString()}.
	 */
	public String description;
	/**
	 * Allows use of {@link Skills#gatherinformation}.
	 */
	public boolean gossip = false;

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
		generate(this);
	}

	/**
	 * Default implementation of {@link #generate()}, will try random
	 * positioning until it lands on a free space.
	 * 
	 * @param p
	 */
	static public void generate(WorldActor p) {
		p.x = -1;
		ArrayList<WorldActor> actors = WorldActor.getall();
		actors.remove(p);
		while (p.x == -1 || World.seed.map[p.x][p.y].equals(Terrain.WATER)
				|| WorldActor.get(p.x, p.y, actors) != null || neartown(p)) {
			p.x = RPG.r(0, World.MAPDIMENSION - 1);
			p.y = RPG.r(0, World.MAPDIMENSION - 1);
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
						if (closest == null || Walker.distance(p.x, p.y, a.x,
								a.y) < Walker.distance(p.x, p.y, closest.x,
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
	public static boolean headsup(List<Combatant> opponents,
			String description) {
		opponents.sort(new Comparator<Combatant>() {
			@Override
			public int compare(Combatant o1, Combatant o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		Game.messagepanel.clear();
		Game.message(
				description + ". Forces: ("
						+ ChallengeRatingCalculator.describedifficulty(
								ChallengeRatingCalculator.calculateel(opponents)
										- ChallengeRatingCalculator.calculateel(
												Squad.active.members))
						+ " fight)\n\n" + Squad.active.spot(opponents)
						+ "\n\nPress s to storm or any other key to retreat.",
				null, Delay.NONE);
		if (InfoScreen.feedback() == 's') {
			return true;
		}
		throw new RepeatTurn();
	}

	@Override
	public String toString() {
		return description;
	}

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
			generate(this);
		}
	}

	public boolean iscloseto(Class<? extends Location> targets) {
		for (WorldActor p : getall(targets)) {
			if (p != this && Walker.distance(x, y, p.x, p.y) <= CLOSE) {
				return true;
			}
		}
		return false;
	}

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
}