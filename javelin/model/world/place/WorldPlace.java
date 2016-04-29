package javelin.model.world.place;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Siege;
import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.guarded.Academy;
import javelin.model.world.place.guarded.Dwelling;
import javelin.model.world.place.guarded.Guardian;
import javelin.model.world.place.guarded.Inn;
import javelin.model.world.place.guarded.Shrine;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.unique.Artificer;
import javelin.model.world.place.unique.Haxor;
import javelin.model.world.place.unique.MercenariesGuild;
import javelin.view.Images;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * A {@link WorldActor} that is actually a place that represent a location to be
 * entered or explored.
 * 
 * @see WorldScreen#getactors()
 * @author alex
 */
public abstract class WorldPlace extends WorldActor {
	/**
	 * Note that this value is used in a triagonal calculation, not in
	 * square-steps.
	 */
	private static final int CLOSE = 4;
	static final HashMap<Class<? extends WorldPlace>, Image> IMAGES =
			new HashMap<Class<? extends WorldPlace>, Image>();

	static {
		IMAGES.put(Town.class, Images.town);
		IMAGES.put(Dungeon.class, Images.dungeon);
		IMAGES.put(Haxor.class, Images.haxor);
		IMAGES.put(Lair.class, Images.lair);
		IMAGES.put(Outpost.class, Images.outpost);
		IMAGES.put(Portal.class, Images.portal);
		IMAGES.put(Inn.class, Images.inn);
		IMAGES.put(Shrine.class, Images.shrine);
		IMAGES.put(Guardian.class, Images.guardian);
		IMAGES.put(Artificer.class, Images.university);
		IMAGES.put(Dwelling.class, Images.dwelling);
		IMAGES.put(Academy.class, Images.academy);
		IMAGES.put(MercenariesGuild.class, Images.arena);
	}

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
	protected String description;
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
	public WorldPlace(String description) {
		this.description = description;
		generate();
	}

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
		ArrayList<WorldActor> actors = WorldScreen.getactors();
		actors.remove(p);
		while (p.x == -1 || WorldScreen.getactor(p.x, p.y, actors) != null
				|| neartown(p)) {
			p.x = RPG.r(0, World.MAPDIMENSION - 1);
			p.y = RPG.r(0, World.MAPDIMENSION - 1);
		}
	}

	private static boolean neartown(WorldActor p) {
		if (p instanceof Town) {
			return false;
		}
		for (WorldActor place : WorldPlace.getall(Town.class)) {
			Town t = (Town) place;
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
	public static boolean interact(WorldPlace p) {
		if (p.defend()) {
			return false;
		}
		if (p.gossip) {
			WorldActor closest = null;
			for (int x = p.x - 5; x <= p.x + 5; x++) {
				for (int y = p.y - 5; y <= p.y + 5; y++) {
					if (!WorldScreen.discovered.contains(new Point(x, y))) {
						WorldActor a = WorldScreen.getactor(x, y);
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
					WorldScreen.discovered.add(new Point(closest.x, closest.y));
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
					ChallengeRatingCalculator.calculateElSafe(garrison));
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
	 * @throws RepeatTurnException
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
				description + ". Forces: (" + ChallengeRatingCalculator
						.describedifficulty(ChallengeRatingCalculator
								.calculateElSafe(opponents)
								- ChallengeRatingCalculator
										.calculateElSafe(Squad.active.members))
						+ " fight)\n\n" + Squad.active.spot(opponents)
						+ "\n\nPress s to storm or any other key to retreat.",
				null, Delay.NONE);
		if (InfoScreen.feedback() == 's') {
			return true;
		}
		throw new RepeatTurnException();
	}

	@Override
	public String toString() {
		return description;
	}

	public Image getimage() {
		return IMAGES.get(getClass());
	}

	/**
	 * Convenience method for subclasses to use as {@link #generate()}. Warning:
	 * when using this do not override {@link #generate(WorldPlace)}!
	 */
	protected void generateawayfromtown() {
		x = -1;
		while (x == -1 || iscloseto(Town.class)) {
			generate(this);
		}
	}

	public boolean iscloseto(Class<? extends WorldPlace> targets) {
		for (WorldActor p : getall(targets)) {
			if (p != this && Walker.distance(x, y, p.x, p.y) <= CLOSE) {
				return true;
			}
		}
		return false;
	}

	public double distance(int xp, int yp) {
		return Walker.distance(xp, yp, x, y);
	}

	public static int count() {
		int sum = 0;
		for (ArrayList<WorldActor> places : INSTANCES.values()) {
			sum += places.size();
		}
		return sum;
	}

	/**
	 * @return <code>true</code> if a flag icon is to be displayed.
	 */
	public boolean ishosting() {
		return false;
	}

	/**
	 * @return <code>true</code> if a item icon is to be displayed.
	 */
	public boolean iscrafting() {
		return false;
	}

	/**
	 * @return <code>true</code> if a arrow icon is to be displayed.
	 */
	public boolean isupgrading() {
		return false;
	}
}