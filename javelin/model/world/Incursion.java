package javelin.model.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.place.Portal;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.town.Town;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.InfoScreen;

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
public class Incursion implements WorldActor {
	public static List<Incursion> squads = new ArrayList<Incursion>();
	public int x;
	public int y;
	transient Thing visual;
	public Integer el = null;
	public List<Combatant> squad = null;
	public final Realm realm;
	public static Integer currentel = 1;

	public Incursion(final int x, final int y, ArrayList<Combatant> squad,
			Realm r) {
		super();
		this.x = x;
		this.y = y;
		Incursion.squads.add(this);
		if (squad == null) {
			el = Incursion.currentel;
			Incursion.currentel += 1;
		} else {
			this.squad = squad;
		}
		realm = r;
		StateManager.save();
	}

	@Override
	public void place() {
		visual = Lib.create("lesser demon");
		updateavatar();
		WorldScreen.worldmap.addThing(visual, x, y);
	}

	public void move(final WorldScreen s) {
		updateavatar();
		WorldActor target = findtarget(s);
		final int targetx = target.getx();
		final int targety = target.gety();
		int newx = x + decideaxismove(x, targetx);
		int newy = y + decideaxismove(y, targety);
		target = WorldScreen.getactor(newx, newy);
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
		final List<WorldActor> targets = WorldScreen.getallmapactors();
		for (final WorldActor a : new ArrayList<WorldActor>(targets)) {
			boolean invasionportal =
					a instanceof Portal && ((Portal) a).invasion;
			boolean friendlytown = a instanceof Town
					&& ((Town) a).realm == realm && ((Town) a).ishostile();
			if (a.ignore(this)) {
				targets.remove(a);
			}
		}
		WorldActor target = null;
		for (final WorldActor a : targets) {
			if (target == null || WorldMap.triangledistance(new Point(x, y),
					new Point(a.getx(), a.gety())) < WorldMap.triangledistance(
							new Point(x, y),
							new Point(target.getx(), target.gety()))) {
				target = a;
			}
		}
		return target;
	}

	public static void waitforenter() {
		Game.message(" Press ENTER to continue...", null, Delay.NONE);
		Character feedback = ' ';
		while (feedback != '\n') {
			feedback = InfoScreen.feedback();
		}
		Game.messagepanel.clear();
	}

	private int decideaxismove(final int me, final int target) {
		if (target > me) {
			return +1;
		}
		if (target == me) {
			return 0;
		}
		return -1;
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
		Incursion.squads.remove(this);
	}

	public Fight getfight() {
		return new IncursionFight(this);
	}

	public static boolean invade(WorldScreen world) {
		if (Javelin.DEBUGDISABLECOMBAT) {
			return false;
		}
		for (final Incursion i : new ArrayList<Incursion>(Incursion.squads)) {
			i.move(world);
		}
		return spawn();
	}

	/**
	 * A rate of 1 incursion every 18 days means that it will take a year for a
	 * level 20 incursion to appear.
	 */
	public static boolean spawn() {
		if (RPG.r(1, 18) != 1 && !Javelin.DEBUG_SPAWNINCURSION) {
			return false;
		}
		ArrayList<WorldPlace> portals =
				new ArrayList<WorldPlace>(Portal.portals);
		Collections.shuffle(portals);
		for (WorldPlace p : portals) {
			if (((Portal) p).invasion) {
				Realm[] realms = Realm.values();
				Realm r = realms[RPG.r(0, realms.length - 1)];
				int x = p.x;
				int y = p.y;
				place(r, x, y, null);
				return true;
			}
		}
		return false;
	}

	public static void place(Realm r, int x, int y,
			ArrayList<Combatant> squadp) {
		while (WorldScreen.getactor(x, y) != null) {
			int delta = RPG.pick(new int[] { -1, 0, +1 });
			if (RPG.r(1, 2) == 1) {
				x += delta;
			} else {
				y += delta;
			}
		}
		create(x, y, squadp, r);
	}

	// static void placeatedge() {
	// while (true) {
	// int x = RPG.r(0, WorldMap.MAPDIMENSION - 1);
	// int y = RPG.r(0, WorldMap.MAPDIMENSION - 1);
	// switch (RPG.pick(new int[] { 1, 2, 3, 4 })) {
	// case 1:
	// /* top */
	// y = WorldMap.MAPDIMENSION - 1;
	// break;
	// case 2:
	// /* right */
	// x = WorldMap.MAPDIMENSION - 1;
	// break;
	// case 3:
	// /* bottom */
	// y = 0;
	// break;
	// case 4:
	// /* left */
	// x = 0;
	// break;
	// }
	// if (WorldScreen.getmapactor(x, y) == null) {
	// create(x, y, null);
	// return;
	// }
	// }
	// }

	public static Incursion create(int x, int y, ArrayList<Combatant> squadp,
			Realm r) {
		Incursion incursion = new Incursion(x, y, squadp, r);
		incursion.place();
		return incursion;
	}

	@Override
	public void move(int tox, int toy) {
		x = tox;
		y = toy;
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
		/* don't inline, null bug */
		if (attacker.realm == realm) {
			return ignoreincursion(attacker);
		} else {
			return fight(attacker.determineel(), determineel());
		}
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}
	 * 
	 * @return <code>null</code>.
	 */
	static public Boolean ignoreincursion(Incursion attacker) {
		WorldScreen.displace(attacker);
		return null;
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}. Uses a percentage to
	 * decide which combatant wins (adapted from CCR).
	 * 
	 * @param attacker
	 *            Encounter level.
	 * @param defender
	 *            Encounter level.
	 * @return <code>true</code> if attacker wins.
	 */
	public static boolean fight(int attacker, int defender) {
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

	public int determineel() {
		return squad == null ? el
				: ChallengeRatingCalculator.calculateElSafe(squad);
	}

	@Override
	public boolean ignore(Incursion attacker) {
		return realm == attacker.realm;
	}
}
