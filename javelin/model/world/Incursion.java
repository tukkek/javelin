package javelin.model.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.model.unit.Combatant;
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
	public final int el;
	public List<Combatant> squad = null;
	public static Integer currentel = 1;

	public Incursion(final int x, final int y) {
		super();
		this.x = x;
		this.y = y;
		Incursion.squads.add(this);
		el = Incursion.currentel;
		Incursion.currentel += 1;
		double alarm = WorldScreen.lastday * 24;
		for (Squad s : Squad.squads) {
			if (s.hourselapsed > alarm) {
				s.hourselapsed = (long) alarm;
			}
		}
		Game.message("An enemy incursion arrives!", null, Delay.NONE);
		StateManager.save();
		waitforenter();
	}

	@Override
	public void place() {
		visual = Lib.create("lesser demon");
		WorldScreen.worldmap.addThing(visual, x, y);
	}

	public void move(final WorldScreen s) {
		final WorldActor target = findtarget(s);
		final int targetx = target.getx();
		final int targety = target.gety();
		x += decideaxismove(x, targetx);
		y += decideaxismove(y, targety);
		visual.remove();
		place();
		if (x == targetx && y == targety) {
			attack(target);
		}
	}

	public void attack(final WorldActor target) {
		Game.message("An incursion reaches " + target.describe() + "!", null,
				Delay.NONE);
		waitforenter();
		if (target instanceof Squad) {
			throw new StartBattle(new IncursionFight(this));
		}
		target.remove();
		remove();
	}

	public WorldActor findtarget(final WorldScreen s) {
		WorldActor target = null;
		final List<WorldActor> targets = WorldScreen.getallmapactors();
		for (final WorldActor a : new ArrayList<WorldActor>(targets)) {
			if (a instanceof Incursion) {
				targets.remove(a);
			}
		}
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

	public void waitforenter() {
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

	@Override
	public String describe() {
		return "an incursion";
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
		return Incursion.spawn();
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
				int x = p.x;
				int y = p.y;
				while (WorldScreen.getactor(x, y) != null) {
					int delta = RPG.pick(new int[] { -1, 0, +1 });
					if (RPG.r(1, 2) == 1) {
						x += delta;
					} else {
						y += delta;
					}
				}
				create(x, y);
				p.remove();
				return true;
			}
		}
		placeatedge();
		return true;
	}

	static void placeatedge() {
		while (true) {
			int x = RPG.r(0, WorldMap.MAPDIMENSION - 1);
			int y = RPG.r(0, WorldMap.MAPDIMENSION - 1);
			switch (RPG.pick(new int[] { 1, 2, 3, 4 })) {
			case 1:
				/* top */
				y = WorldMap.MAPDIMENSION - 1;
				break;
			case 2:
				/* right */
				x = WorldMap.MAPDIMENSION - 1;
				break;
			case 3:
				/* bottom */
				y = 0;
				break;
			case 4:
				/* left */
				x = 0;
				break;
			}
			if (WorldScreen.getmapactor(x, y) == null) {
				create(x, y);
				return;
			}
		}
	}

	public static Incursion create(int x, int y) {
		Incursion incursion = new Incursion(x, y);
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
}
