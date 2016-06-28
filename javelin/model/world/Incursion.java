package javelin.model.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.Preferences;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.IncursionFight;
import javelin.controller.terrain.Terrain;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.Portal;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

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

	public static Integer currentel = 1;

	/** @see #getel() */
	public List<Combatant> squad = new ArrayList<Combatant>();

	public Incursion(final int x, final int y, ArrayList<Combatant> squadp,
			Realm r) {
		this.x = x;
		this.y = y;
		if (squadp == null) {
			squad.addAll(JavelinApp.generatefight(Incursion.currentel,
					Terrain.get(x, y)).opponents);
			currentel += 1;
		} else {
			squad = squadp;
		}
		realm = r;
		StateManager.save();
	}

	@Override
	public void place() {
		super.place();
		updateavatar();

	}

	public void move(final WorldScreen s) {
		updateavatar();
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
		visual.remove();
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

	private void updateavatar() {
		if (squad == null) {
			return;
		}
		Combatant leader = null;
		for (Combatant c : squad) {
			if (leader == null
					|| c.source.challengeRating > leader.source.challengeRating) {
				leader = c;
			}
		}
		visual.combatant = new Combatant(visual, leader.source, false);
	}

	public WorldActor findtarget(final WorldScreen s) {
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

	public static void waitforenter() {
		Game.message(" Press ENTER to continue...", null, Delay.NONE);
		Character feedback = ' ';
		while (feedback != '\n') {
			feedback = InfoScreen.feedback();
		}
		Game.messagepanel.clear();
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
			} else if (x >= World.MAPDIMENSION) {
				x = World.MAPDIMENSION - 1;
			} else if (y >= World.MAPDIMENSION) {
				y = World.MAPDIMENSION - 1;
			}
		}
		create(x, y, squadp, r);
	}

	public static WorldActor create(int x, int y, ArrayList<Combatant> squadp,
			Realm r) {
		WorldActor incursion = new Incursion(x, y, squadp, r);
		incursion.place();
		return incursion;
	}

	static public ArrayList<Combatant> getsafeincursion(List<Combatant> from) {
		int size = from.size();
		ArrayList<Combatant> to = new ArrayList<Combatant>(size);
		for (int i = 0; i < size; i++) {
			to.add(from.get(i).clonedeeply());
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

	public Collection<? extends Combatant> getsquad() {
		return null;
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
}
