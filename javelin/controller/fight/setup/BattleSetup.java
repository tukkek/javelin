package javelin.controller.fight.setup;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.map.Map;
import javelin.controller.map.MapGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * Given a {@link Map}, a {@link Squad} and a {@link Fight} setups an initial
 * battle state.
 * 
 * @author alex
 */
public class BattleSetup {
	private static final int MAXDISTANCE = 9;

	/** Starts the setup steps. */
	public void setup() {
		rollinitiative();
		Fight f = Javelin.app.fight;
		generatemap(f);
		place();
		Weather.flood();
	}

	/** Allows greater control of {@link Map} generation. */
	public void generatemap(Fight f) {
		if (f.map == null) {
			f.map = MapGenerator.generatebattlemap(
					f.terrain == null ? Terrain.current() : f.terrain,
					Dungeon.active != null);
		}
		f.map.generate();
		Fight.state.map = f.map.map;
	}

	/** Sets each combatant in a sensible location. */
	public void place() {
		for (int i = 0; i < 1000; i++) {
			try {
				List<Combatant> queuea = RPG.r(1, 2) == 1 ? Fight.state.blueTeam
						: Fight.state.redTeam;
				List<Combatant> queueb = queuea == Fight.state.blueTeam
						? Fight.state.redTeam : Fight.state.blueTeam;
				queuea = new ArrayList<Combatant>(queuea);
				queueb = new ArrayList<Combatant>(queueb);
				final Combatant seeda = RPG.pick(queuea);
				final Combatant seedb = RPG.pick(queueb);
				add(seeda,
						getrandompoint(Fight.state, 0,
								Fight.state.map.length - 1, 0,
								Fight.state.map[0].length - 1));
				placecombatant(seedb, seeda, Fight.state);
				final ArrayList<Combatant> placeda = new ArrayList<Combatant>();
				final ArrayList<Combatant> placedb = new ArrayList<Combatant>();
				markplaced(seeda, queuea, placeda);
				markplaced(seedb, queueb, placedb);
				while (!queuea.isEmpty() || !queueb.isEmpty()) {
					placeteammate(queuea, placeda, Fight.state);
					placeteammate(queueb, placedb, Fight.state);
				}
				return;
			} catch (GaveUp e) {
				continue;
			}
		}
		throw new RuntimeException("Gave up on trying to place parties.");
	}

	/** Rolls initiative for each {@link Combatant}. */
	public void rollinitiative() {
		for (final Combatant c : Fight.state.getcombatants()) {
			c.ap = 0;
			c.rollinitiative();
		}
	}

	void placeteammate(final List<Combatant> queue,
			final ArrayList<Combatant> placed, final BattleState s)
			throws GaveUp {
		if (!queue.isEmpty()) {
			Combatant c = RPG.pick(queue);
			placecombatant(c, RPG.pick(placed), s);
			markplaced(c, queue, placed);
		}
	}

	void markplaced(final Combatant seeda, final List<Combatant> queue,
			final ArrayList<Combatant> placed) {
		queue.remove(seeda);
		placed.add(seeda);
	}

	void placecombatant(final Combatant placing, final Combatant reference,
			final BattleState s) throws GaveUp {
		int vision = placing.view(s.period);
		if (vision > 8 || vision > MAXDISTANCE) {
			vision = MAXDISTANCE;
		}
		final ArrayList<Point> possibilities = mappossibilities(reference,
				vision, s);
		while (!possibilities.isEmpty()) {
			Point p = RPG.pick(possibilities);
			placing.location[0] = p.x;
			placing.location[1] = p.y;
			Vision path = s.haslineofsight(placing, reference);
			if (path == Vision.CLEAR) {
				add(placing, p);
				break;
			}
			possibilities.remove(p);
		}
		if (possibilities.isEmpty()) {
			throw new GaveUp();
		}
	}

	ArrayList<Point> mappossibilities(final Combatant reference, int vision,
			final BattleState s) {
		final ArrayList<Point> possibilities = new ArrayList<Point>();
		for (int x = reference.location[0] - vision; x <= reference.location[0]
				+ vision; x++) {
			if (isbound(x, s.map)) {
				for (int y = reference.location[1]
						- vision; y <= reference.location[1] + vision; y++) {
					if (isbound(y, s.map[0]) && !s.map[x][y].blocked
							&& s.getcombatant(x, y) == null) {
						possibilities.add(new Point(x, y));
					}
				}
			}
		}
		return possibilities;
	}

	/**
	 * @return <code>true</code> if inside battle map.
	 */
	public static boolean isbound(final int y, final Object[] squares) {
		return 0 < y && y <= squares.length - 1;
	}

	/**
	 * @param c
	 *            Sets location to given {@link Point}.
	 */
	public static void add(final Combatant c, final Point p) {
		c.location[0] = p.x;
		c.location[1] = p.y;
	}

	/**
	 * @return A free spot inside the given coordinates. Will loop infinitely if
	 *         given space is fully occupied.
	 */
	static public Point getrandompoint(final BattleState s, int minx, int maxx,
			int miny, int maxy) {
		minx = Math.max(minx, 0);
		miny = Math.max(miny, 0);
		maxx = Math.min(maxx, s.map.length - 1);
		maxy = Math.min(maxy, s.map[0].length - 1);
		Point p = null;
		while (p == null || s.map[p.x][p.y].blocked
				|| s.getcombatant(p.x, p.y) != null) {
			p = new Point(RPG.r(minx, maxx), RPG.r(miny, maxy));
		}
		return p;
	}

	/**
	 * @return Same as {@link #getrandompoint(BattleState, int, int, int, int)}
	 *         but near to a given point.
	 */
	public static Point getrandompoint(BattleState state, Point p) {
		return getrandompoint(state, p.x - 2, p.x + 2, p.y - 2, p.y + 2);
	}

	/**
	 * @return Same as {@link #getrandompoint(BattleState, int, int, int, int)}
	 *         but receives two Points as parameters, forming a square area.
	 */
	public static Point getrandompoint(BattleState state, Point min,
			Point max) {
		return getrandompoint(state, min.x, max.x, min.y, max.y);
	}

}
