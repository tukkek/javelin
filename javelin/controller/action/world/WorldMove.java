package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
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
		Squad.active.lastterrain = Terrain.current();
		final Thing t = JavelinApp.context.gethero();
		int tox = t.x + deltax;
		int toy = t.y + deltay;
		if (!World.validatecoordinate(tox, toy) || (Dungeon.active == null
				&& !World.seed.map[tox][toy].enter(tox, toy))) {
			throw new RepeatTurn();
		}
		float hours = Dungeon.active == null ? Squad.active.move(false) : 0;
		try {
			WorldActor actor =
					Dungeon.active == null ? WorldActor.get(tox, toy) : null;
			Location place =
					actor instanceof Location ? (Location) actor : null;
			try {
				if (JavelinApp.context.react(actor, tox, toy)) {
					if (Dungeon.active != null) {
						if (DungeonScreen.dontmove) {
							DungeonScreen.dontmove = false;
							return;
						}
						place(t, deltax, deltay);
					} else if (place != null) {
						if (place.allowentry && place.garrison.isEmpty()) {
							place(t, deltax, deltay);
						}
						/* TODO */
						if (place instanceof Dungeon) {
							((Dungeon) place).activate(false);
						} else if (place instanceof Temple) {
							Temple temple = (Temple) place;
							if (temple.open) {
								temple.floors.get(0).activate(false);
							}
						}
					}
					return;
				}
				if (place != null && !place.allowentry) {
					return;
				}
			} catch (StartBattle e) {
				if (place != null && place.allowentry) {
					place(t, deltax, deltay);
				}
				throw e;
			}
			if (s instanceof DungeonScreen && (DungeonScreen.dontmove
					|| t.getMap() != Javelin.app.context.map)) {
				DungeonScreen.dontmove = false;
				return;// TODO hack
			}
			if (!place(t, deltax, deltay)) {
				return;
			}
			if (WorldMove.walk(t)) {
				JavelinApp.context.explore(hours);
			}
			heal();
		} finally {
			if (Squad.active != null) {
				Squad.active.ellapse(Math.round(hours));
			}
		}
	}

	static boolean place(final Thing t, final int deltax, final int deltay) {
		JavelinApp.context.map.removeThing(t);
		t.x += deltax;
		t.y += deltay;
		if (!JavelinApp.context.allowmove(t.x, t.y)) {
			t.x -= deltax;
			t.y -= deltay;
			JavelinApp.context.map.addThing(t, t.x, t.y);
			return false;
		}
		if (t.x < 0 || t.x >= JavelinApp.context.map.width || t.y < 0
				|| t.y >= JavelinApp.context.map.height) {
			t.x -= deltax;
			t.y -= deltay;
		}
		JavelinApp.context.updatelocation(t.x, t.y);
		JavelinApp.context.map.addThing(t, t.x, t.y);
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
