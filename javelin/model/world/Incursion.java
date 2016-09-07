package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.Portal;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * An attacking {@link Squad}, trying to destroy a {@link Town} or other
 * {@link WorldActor}. Each one that appears grows stronger, which should
 * eventually end the game.
 * 
 * An {@link Incursion} is similar to an invading {@link Squad} while an
 * Invasion refers to the ongoing process of trying to destroy the player's
 * region.
 * 
 * @author alex
 */
public class Incursion extends WorldActor {
	/** Move even if {@link Javelin#DEBUGDISABLECOMBAT} is enabled. */
	private static final boolean FORCEMOVEMENT = false;
	private static final boolean DONTSPAWN = false;

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
	 *            See {@link WorldActor#realm}.
	 */
	public Incursion(final int x, final int y, ArrayList<Combatant> squadp,
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
		WorldActor target = findtarget(s);
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
		target = WorldActor.get(newx, newy);
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

	WorldActor findtarget(final WorldScreen s) {
		final List<WorldActor> targets = WorldActor.getall();
		for (final WorldActor a : new ArrayList<WorldActor>(targets)) {
			if (a.impermeable || a.realm == realm
					|| crosseswater(this, a.x, a.y)) {
				targets.remove(a);
			}
		}
		if (targets.isEmpty()) {
			return null;
		}
		WorldActor target = null;
		for (final WorldActor a : targets) {
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
	public static boolean crosseswater(WorldActor from, int tox, int toy) {
		int x = from.x;
		int y = from.y;
		while (x != tox && y != toy) {
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
		if (Preferences.DEBUGDISABLECOMBAT && !FORCEMOVEMENT) {
			return;
		}
		move(world);
	}

	/**
	 * A rate of 1 incursion every 18 days means that it will take a year for a
	 * level 20 incursion to appear.
	 */
	public static boolean spawn() {
		if (DONTSPAWN) {
			return false;
		}
		if (RPG.r(1, 18) != 1) {
			return false;
		}
		ArrayList<WorldActor> portals = Location.getall(Portal.class);
		Collections.shuffle(portals);
		for (WorldActor p : portals) {
			if (((Portal) p).invasion) {
				place(p.realm, p.x, p.y, null);
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates and places a new incursion. Finds an empty spot close to the
	 * given coordinates.
	 * 
	 * @param r
	 *            See {@link WorldActor#realm}.
	 * @param x
	 *            Starting {@link World} coordinate.
	 * @param y
	 *            Starting {@link World} coordinate.
	 * @param squadp
	 *            See {@link Incursion#squad}.
	 * @see WorldActor#place()
	 */
	public static void place(Realm r, int x, int y,
			ArrayList<Combatant> squadp) {
		while (WorldActor.get(x, y) != null) {
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
			} else if (x >= World.SIZE) {
				x = World.SIZE - 1;
			} else if (y >= World.SIZE) {
				y = World.SIZE - 1;
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
			return Incursion.fight(attacker.getel(), getel());
		}
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}
	 * 
	 * @return <code>null</code>.
	 */
	static public Boolean ignoreincursion(WorldActor attacker) {
		attacker.displace();
		return null;
	}

	/**
	 * @return Encounter level for {@link #squad}.
	 * @see ChallengeRatingCalculator#calculateel(List)
	 */
	public int getel() {
		return ChallengeRatingCalculator.calculateel(squad);
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
		if (defender == Integer.MIN_VALUE) {
			return true;
		}
		int gap = defender - attacker;
		final int chance;
		if (gap <= -4) {
			chance = 9;
		} else if (gap == -3) {
			chance = 8;
		} else if (gap == -2) {
			chance = 7;
		} else if (gap == -1) {
			chance = 6;
		} else if (gap == 0) {
			chance = 5;
		} else if (gap == 1) {
			chance = 4;
		} else if (gap == 2) {
			chance = 3;
		} else if (gap == 3) {
			chance = 2;
		} else {
			chance = 1;
		}
		return RPG.r(1, 10) <= chance;
	}

	@Override
	public String toString() {
		return "An incursion";
	}

	@Override
	public boolean interact() {
		if (!Location.headsup(squad, toString())) {
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
					|| c.source.challengeRating > leader.source.challengeRating) {
				leader = c;
			}
		}
		return Images.getImage(leader.source.avatarfile);
	}

	@Override
	public String describe() {
		return "Enemy incursion ("
				+ ChallengeRatingCalculator.describedifficulty(squad)
				+ " fight):\n\n" + Squad.active.spot(squad);
	}
}
