package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;

/**
 * Makes a movement on the overworld or {@link Dungeon}.
 * 
 * TODO {@link WorldScreen} hierarchy should be refactored into proper Battle /
 * Dungeon / World screens.
 * 
 * TODO {@link #perform(WorldScreen)} needs refactoring after 2.0
 * 
 * @see Javelin#getDayPeriod()
 * @author alex
 */
public class WorldMove extends WorldAction {
	/**
	 * Represents time spent on resting, eating, sleeping, etc.
	 * 
	 * TODO forced march
	 */
	public static final float NORMALMARCH = 4f / 5f;
	/**
	 * Ideally a move should always be 6 hours in a worst-case scenario (the
	 * time of a period), so as to avoid a move taking longer than a period,
	 * which could confuse {@link Hazard}s.
	 * 
	 * @see #TIMECOST
	 */
	public static final float MOVETARGET = 6f;
	/**
	 * How much time it takes to walk a single square with speed 30 (~30mph,
	 * normal human speed).
	 * 
	 * Calculation of worst case scenario (which should take 6 hours): 6 *
	 * 15/30ft (slow races) * .5 (bad terrain)
	 * 
	 * @see Monster#gettopspeed()
	 * @see #MOVETARGET
	 * @see #NORMALMARCH
	 */
	public static final float TIMECOST = NORMALMARCH * (MOVETARGET * 15f / 30f)
			/ 2f;
	/** TODO remove, hack */
	public static boolean isleavingplace = false;//
	private final int deltax;
	private final int deltay;

	/**
	 * Constructor
	 * 
	 * @param keycodes
	 *            Integer/char keys.
	 * @param deltax
	 *            Direction of x movement.
	 * @param deltay
	 *            Direction of y movement.
	 * @param keys
	 *            Text keys.
	 */
	public WorldMove(final int[] keycodes, final int deltax, final int deltay,
			final String[] keys) {
		super("Move (" + keys[1].charAt(0) + ")", keycodes, keys);
		this.deltax = deltax;
		this.deltay = deltay;
	}

	@Override
	public void perform(final WorldScreen s) {
		final Point t = JavelinApp.context.getherolocation();
		move(t.x + deltax, t.y + deltay, true);
	}

	/**
	 * TODO needs to be refactored
	 * 
	 * @see BattleScreen
	 */
	public static boolean move(int tox, int toy, boolean encounter) {
		final WorldScreen s = (WorldScreen) BattleScreen.active;
		Squad.active.lastterrain = Terrain.current();
		if (!World.validatecoordinate(tox, toy) || (Dungeon.active == null
				&& !World.seed.map[tox][toy].enter(tox, toy))) {
			throw new RepeatTurn();
		}
		float hours = Dungeon.active == null
				? Squad.active.move(false, Terrain.current(), tox, toy) : 0;
		try {
			Actor actor = Dungeon.active == null ? World.get(tox, toy) : null;
			Location l = actor instanceof Location ? (Location) actor : null;
			try {
				if (JavelinApp.context.react(actor, tox, toy)) {
					if (Dungeon.active != null) {
						if (DungeonScreen.dontenter) {
							DungeonScreen.dontenter = false;
						}
						if (DungeonScreen.updatelocation) {
							place(tox, toy);
						} else {
							DungeonScreen.updatelocation = true;
						}
						return !DungeonScreen.stopmovesequence;
					} else if (l != null) {
						if (l.allowentry && l.garrison.isEmpty()) {
							place(tox, toy);
						}
					}
					return true;
				}
				if (l != null && !l.allowentry) {
					return false;
				}
			} catch (StartBattle e) {
				if (l != null && l.allowentry) {
					place(tox, toy);
				}
				throw e;
			}
			if (s instanceof DungeonScreen && (DungeonScreen.dontenter)) {
				DungeonScreen.dontenter = false;
				return false;// TODO hack
			}
			if (!place(tox, toy)) {
				return false;
			}
			boolean stop = false;
			if (WorldMove.walk(JavelinApp.context.getherolocation())) {
				stop = JavelinApp.context.explore(hours, encounter);
			}
			heal();
			return stop;
		} finally {
			if (Squad.active != null) {
				Squad.active.ellapse(Math.round(hours));
			}
		}
	}

	/**
	 * @return <code>true</code> if moved current actor to the given location.
	 */
	public static boolean place(final int tox, final int toy) {
		if (!JavelinApp.context.allowmove(tox, toy)) {
			return false;
		}
		if (!JavelinApp.context.validatepoint(tox, toy)) {
			return false;
		}
		JavelinApp.context.updatelocation(tox, toy);
		JavelinApp.context.view(tox, toy);
		return true;
	}

	static void heal() {
		for (final Combatant m : Squad.active.members) {
			if (m.source.fasthealing != 0) {
				m.hp = m.maxhp;
			}
		}
	}

	static boolean walk(final Point t) {
		if (Dungeon.active != null) {
			return true;
		}
		final List<Squad> here = new ArrayList<Squad>();
		for (final Actor p : World.getall(Squad.class)) {
			Squad s = (Squad) p;
			if (s.x == t.x && s.y == t.y) {
				here.add(s);
			}
		}
		if (here.size() <= 1) {
			return true;
		}
		here.get(0).join(here.get(1));
		return false;
	}
}
