package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.old.Game;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.Thing;

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
	public static final float TIMECOST =
			NORMALMARCH * (MOVETARGET * 15f / 30f) / 2f;
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
		final Thing t = JavelinApp.context.gethero();
		move(t.x + deltax, t.y + deltay, true);
	}

	public static boolean move(int tox, int toy, boolean encounter) {
		final Thing t = Game.hero();
		final WorldScreen s = (WorldScreen) BattleScreen.active;
		Squad.active.lastterrain = Terrain.current();
		if (!World.validatecoordinate(tox, toy) || (Dungeon.active == null
				&& !World.seed.map[tox][toy].enter(tox, toy))) {
			throw new RepeatTurn();
		}
		float hours = Dungeon.active == null
				? Squad.active.move(false, Terrain.current()) : 0;
		try {
			WorldActor actor =
					Dungeon.active == null ? WorldActor.get(tox, toy) : null;
			Location l = actor instanceof Location ? (Location) actor : null;
			try {
				if (JavelinApp.context.react(actor, tox, toy)) {
					if (BattleScreen.active.map != t.getMap()) {
						return true;
					}
					if (Dungeon.active != null) {
						if (DungeonScreen.dontenter) {
							DungeonScreen.dontenter = false;
						}
						if (DungeonScreen.updatelocation) {
							place(t, tox, toy);
						} else {
							DungeonScreen.updatelocation = true;
						}
						return !DungeonScreen.stopmovesequence;
					} else if (l != null) {
						if (l.allowentry && l.garrison.isEmpty()) {
							place(t, tox, toy);
						}
					}
					return true;
				}
				if (l != null && !l.allowentry) {
					return false;
				}
			} catch (StartBattle e) {
				if (l != null && l.allowentry) {
					place(t, tox, toy);
				}
				throw e;
			}
			if (s instanceof DungeonScreen && (DungeonScreen.dontenter)) {
				DungeonScreen.dontenter = false;
				return false;// TODO hack
			}
			if (!place(t, tox, toy)) {
				return false;
			}
			boolean stop = false;
			if (WorldMove.walk(t)) {
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

	public static boolean place(final Thing t, final int tox, final int toy) {
		if (!JavelinApp.context.allowmove(tox, toy)) {
			// JavelinApp.context.map.addThing(t, t.x, t.y);
			return false;
		}
		if (JavelinApp.context.validatepoint(tox, toy)) {
			t.remove();
			// t.getMap().removeThing(t);
			t.x = tox;
			t.y = toy;
		}
		JavelinApp.context.updatelocation(t.x, t.y);
		// JavelinApp.context.map.addThing(t, t.x, t.y);
		JavelinApp.context.view(t);
		return true;
	}

	static void heal() {
		for (final Combatant m : Squad.active.members) {
			if (m.source.fasthealing != 0) {
				m.hp = m.maxhp;
			}
		}
	}

	static boolean walk(final Thing t) {
		final List<Squad> here = new ArrayList<Squad>();
		for (final WorldActor p : Squad.getall(Squad.class)) {
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
