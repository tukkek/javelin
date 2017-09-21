package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.Portal;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * An attacking {@link Squad}, trying to destroy a {@link Town} or other
 * {@link Actor}. Each one that appears grows stronger, which should eventually
 * end the game.
 *
 * An {@link Incursion} is similar to an invading {@link Squad} while an
 * Invasion refers to the ongoing process of trying to destroy the player's
 * region.
 *
 * @author alex
 */
public class Incursion extends Actor {
	static final boolean SPAWN = Javelin.DEBUG ? false : true;
	/** Move even if {@link Javelin#DEBUGDISABLECOMBAT} is enabled. */
	static final boolean FORCEMOVEMENT = false;
	static final Comparator<Combatant> SORTBYCR = new Comparator<Combatant>() {
		@Override
		public int compare(Combatant o1, Combatant o2) {
			return new Float(o1.source.challengerating)
					.compareTo(o2.source.challengerating);
		}
	};

	/** Should probably move this to {@link Portal}? */
	public static Integer currentel = 1;

	/** @see #getel() */
	public List<Combatant> squad = new ArrayList<Combatant>();

	/**
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @param squadp
	 *            See {@link #currentel}.
	 * @param r
	 *            See {@link Actor#realm}.
	 */
	public Incursion(final int x, final int y, List<Combatant> squadp,
			Realm r) {
		this.x = x;
		this.y = y;
		if (squadp == null) {
			ArrayList<Terrain> terrains = new ArrayList<Terrain>(1);
			terrains.add(Terrain.get(x, y));
			squad.addAll(Fight.generate(Incursion.currentel, terrains));
			currentel += 1;
		} else {
			squad = squadp;
		}
		realm = r;
	}

	/**
	 * Incursions only move once a day but this is balanced by several implicit
	 * benefits like canonical {@link #squad} HP not decreasing after a battle
	 * that has been won, jumping over certain {@link Location}s, not having to
	 * deal with {@link RandomEncounter}...
	 *
	 * @param s
	 *            TODO use {@link WorldScreen#current}
	 */
	public void move(final WorldScreen s) {
		Actor target = findtarget(s);
		if (target == null) {
			displace();
			return;
		}
		final int targetx = target.x;
		final int targety = target.y;
		int newx = x + decideaxismove(x, targetx);
		int newy = y + decideaxismove(y, targety);
		if (Terrain.get(newx, newy).equals(Terrain.WATER)) {
			displace();
			return;
		}
		target = World.get(newx, newy);
		x = newx;
		y = newy;
		place();
		if (target == null) {
			return;
		}
		Boolean status = target.destroy(this);
		if (status == null) {
			return;
		}
		if (status == true) {
			target.remove();
		} else if (status == false) {
			remove();
		}
	}

	Actor findtarget(final WorldScreen s) {
		ArrayList<Actor> targets = new ArrayList<Actor>();
		for (final Actor a : World.getactors()) {
			if (a.impermeable || a.realm == realm
					|| crosseswater(this, a.x, a.y)) {
				continue;
			}
			Location l = a instanceof Location ? (Location) a : null;
			if (l != null && CrCalculator.calculateel(l.garrison) > getel()) {
				continue;
			}
			targets.add(a);
		}
		if (targets.isEmpty()) {
			return null;
		}
		Actor target = null;
		for (final Actor a : targets) {
			if (target == null || Walker.distance(x, y, a.x, a.y) < Walker
					.distance(x, y, target.x, target.y)) {
				target = a;
			}
		}
		return target;
	}

	/**
	 * @param tox
	 * @param toy
	 * @return Checks if there is any body of water between these two actors.
	 */
	public static boolean crosseswater(Actor from, int tox, int toy) {
		if (Terrain.get(tox, toy).equals(Terrain.WATER)) {
			return true;
		}
		int x = from.x;
		int y = from.y;
		while (x != tox || y != toy) {
			x += decideaxismove(x, tox);
			y += decideaxismove(y, toy);
			if (Terrain.get(x, y).equals(Terrain.WATER)) {
				return true;
			}
		}
		return false;
	}

	static int decideaxismove(final int me, final int target) {
		if (target == me) {
			return 0;
		}
		return target > me ? +1 : -1;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		// if (Preferences.DEBUGDISABLECOMBAT && !FORCEMOVEMENT) {
		// return;
		// }
		move(world);
	}

	/**
	 * A rate of 1 incursion every 18 days means that it will take a year for a
	 * level 20 incursion to appear.
	 */
	public static boolean spawn() {
		if (!SPAWN || RPG.r(1, 18) != 1) {
			return false;
		}
		ArrayList<Actor> portals = World.getall(Portal.class);
		Collections.shuffle(portals);
		for (Actor p : portals) {
			if (((Portal) p).invasion) {
				place(p.realm, p.x, p.y, null);
				return true;
			}
		}
		return false;
	}

	// static int spawned = 0;

	/**
	 * Creates and places a new incursion. Finds an empty spot close to the
	 * given coordinates.
	 *
	 * @param r
	 *            See {@link Actor#realm}.
	 * @param x
	 *            Starting {@link World} coordinate.
	 * @param y
	 *            Starting {@link World} coordinate.
	 * @param squadp
	 *            See {@link Incursion#squad}.
	 * @see Actor#place()
	 */
	public static void place(Realm r, int x, int y, List<Combatant> squadp) {
		// if (Javelin.DEBUG) {
		// spawned += 1;
		// System.out.println("Incursion spawn #" + spawned + " EL"
		// + ChallengeRatingCalculator.calculateel(squadp));
		// }
		if (!SPAWN) {
			return;
		}
		int size = World.scenario.size;
		ArrayList<Actor> actors = World.getactors();
		while (World.get(x, y, actors) != null) {
			int delta = RPG.pick(new int[] { -1, 0, +1 });
			if (RPG.r(1, 2) == 1) {
				x += delta;
			} else {
				y += delta;
			}
			if (x < 0) {
				x = 0;
			} else if (y < 0) {
				y = 0;
			} else if (x >= size) {
				x = size - 1;
			} else if (y >= size) {
				y = size - 1;
			}
		}
		new Incursion(x, y, squadp, r).place();
	}

	/**
	 * @param from
	 *            Clones the {@link Combatant}s here into...
	 * @return a new list.
	 * @see Combatant#clone()
	 * @see Combatant#clonesource()
	 */
	static public ArrayList<Combatant> getsafeincursion(List<Combatant> from) {
		int size = from.size();
		ArrayList<Combatant> to = new ArrayList<Combatant>(size);
		for (int i = 0; i < size; i++) {
			to.add(from.get(i).clone().clonesource());
		}
		return to;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		/* don't inline the return statement, null bug */
		if (attacker.realm == realm) {
			return ignoreincursion(attacker);
		} else {
			return fight(attacker.getel(), getel());
		}
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}
	 *
	 * @return <code>null</code>.
	 */
	static public Boolean ignoreincursion(Actor attacker) {
		attacker.displace();
		return null;
	}

	/**
	 * @return Encounter level for {@link #squad}.
	 * @see CrCalculator#calculateel(List)
	 */
	public int getel() {
		return CrCalculator.calculateel(squad);
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}. Uses a percentage to
	 * decide which combatant wins (adapted from CCR).
	 *
	 * @param attacker
	 *            Encounter level.
	 * @param defender
	 *            Encounter level. {@link Integer#MIN_VALUE} means an automatic
	 *            victory for the attacker.
	 * @return <code>true</code> if attacker wins.
	 */
	public static boolean fight(int attacker, int defender) {
		return defender == Integer.MIN_VALUE
				|| RPG.r(1, 10) <= getchance(attacker, defender);
	}

	/**
	 * @param defender
	 *            Encounter level.
	 * @param attacker
	 *            Encounter level.
	 * @return A chance from 1 to 9 that the attacker will win (meant to be used
	 *         with a d10 roll).
	 */
	static int getchance(int attacker, int defender) {
		int gap = defender - attacker;
		if (gap <= -4) {
			return 9;
		}
		if (gap == -3) {
			return 8;
		}
		if (gap == -2) {
			return 7;
		}
		if (gap == -1) {
			return 6;
		}
		if (gap == 0) {
			return 5;
		}
		if (gap == 1) {
			return 4;
		}
		if (gap == 2) {
			return 3;
		}
		if (gap == 3) {
			return 2;
		}
		return 1;
	}

	@Override
	public String toString() {
		return "An incursion";
	}

	@Override
	public boolean interact() {
		if (!Location.headsup(squad, toString(), true)) {
			return false;
		}
		throw new StartBattle(new IncursionFight(this));
	}

	@Override
	public List<Combatant> getcombatants() {
		return squad;
	}

	@Override
	public Image getimage() {
		if (squad == null) {
			return null;
		}
		Combatant leader = null;
		for (Combatant c : squad) {
			if (leader == null
					|| c.source.challengerating > leader.source.challengerating) {
				leader = c;
			}
		}
		return Images.getImage(leader.source.avatarfile);
	}

	@Override
	public String describe() {
		return "Enemy incursion (" + CrCalculator.describedifficulty(squad)
				+ " fight):\n\n" + Squad.active.spot(squad);
	}

	/**
	 * @return Like {@link #fight(int, int)} but also damage the survivor,
	 *         killing off creatures according to the gravity of battle.
	 */
	public boolean fight(List<Combatant> defenders) {
		int me = getel();
		int them = CrCalculator.calculateel(defenders);
		boolean win = fight(me, them);
		if (win) {
			damage(squad, getchance(me, them));
		} else {
			damage(defenders, getchance(them, me));
		}
		return win;
	}

	/** TODO this hasn't been properly tested yet */
	void damage(List<Combatant> survivors, int chance) {
		int totalcr = 0;
		for (Combatant c : survivors) {
			totalcr += c.source.challengerating;
		}
		float damage = totalcr * (1 - chance / 10f);
		LinkedList<Combatant> wounded = new LinkedList<Combatant>(survivors);
		Collections.sort(wounded, SORTBYCR);
		while (damage > 0 && survivors.size() > 1) {
			Combatant dead = wounded.pop();
			survivors.remove(dead);
			damage -= dead.source.challengerating;
		}
	}

	public static ArrayList<Incursion> getincursions() {
		ArrayList<Incursion> all = new ArrayList<Incursion>();
		for (Actor a : World.getall(Incursion.class)) {
			if (a instanceof Incursion) {
				all.add((Incursion) a);
			}
		}
		return all;
	}
}
