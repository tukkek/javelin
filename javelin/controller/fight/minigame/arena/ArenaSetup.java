package javelin.controller.fight.minigame.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.comparator.SizeComparator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.arena.building.ArenaAcademy;
import javelin.controller.fight.minigame.arena.building.ArenaBuilding;
import javelin.controller.fight.minigame.arena.building.ArenaFountain;
import javelin.controller.fight.minigame.arena.building.ArenaLair;
import javelin.controller.fight.minigame.arena.building.ArenaShop;
import javelin.controller.fight.setup.BattleSetup;
import javelin.model.state.Square;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.old.RPG;

public class ArenaSetup extends BattleSetup {
	static final int MAPSIZE = 28;
	static final int[] ARENASIZE = new int[] { MAPSIZE - 2, MAPSIZE - 2 };
	static final Point[] AREA = new Point[] {
			new Point((MAPSIZE - ARENASIZE[0]) / 2,
					(MAPSIZE - ARENASIZE[1]) / 2),
			new Point((MAPSIZE + ARENASIZE[0]) / 2,
					(MAPSIZE + ARENASIZE[1]) / 2) };

	final ArenaFight fight;

	/**
	 * @param arenaFight
	 */
	ArenaSetup(ArenaFight arenaFight) {
		fight = arenaFight;
	}

	@Override
	public void generatemap(Fight f) {
		super.generatemap(f);
		f.map.flying = false;
		Square[][] map = f.map.map;
		f.map.map = new Square[MAPSIZE][];
		Fight.state.map = f.map.map;
		for (int i = 0; i < MAPSIZE; i++) {
			f.map.map[i] = Arrays.copyOfRange(map[i], 0, MAPSIZE);
		}
		for (int x = 0; x < MAPSIZE; x++) {
			for (int y = 0; y < MAPSIZE; y++) {
				Point p = new Point(x, y);
				if (!p.validate(AREA[0].x, AREA[0].y, AREA[1].x, AREA[1].y)) {
					f.map.map[x][y].blocked = true;
					f.map.map[x][y].flooded = false;
				} else if (x == AREA[0].x || x == AREA[1].x - 1
						|| y == AREA[0].y || y == AREA[1].y - 1) {
					f.map.map[x][y].blocked = false;
				}
			}
		}
	}

	@Override
	public void place() {
		ArrayList<Combatant> gladiators = new ArrayList<>(Fight.state.blueTeam);
		placebuildings();
		Point p = null;
		while (p == null || !validate(p)) {
			p = getcenterpoint();
		}
		fight.enter(gladiators, Fight.state.blueTeam, p);
	}

	Point getcenterpoint() {
		return new Point(RPG.r(AREA[0].x, AREA[1].x),
				RPG.r(AREA[0].y, AREA[1].y));
	}

	void placebuildings() {
		List<List<ArenaBuilding>> quadrants = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			quadrants.add(new ArrayList<ArenaBuilding>());
		}
		generate(2, false, ArenaAcademy.class, quadrants);
		generate(2, true, ArenaLair.class, quadrants);
		generate(2, true, ArenaShop.class, quadrants);
		generate(4, false, ArenaFountain.class, quadrants);
		Collections.shuffle(quadrants);
		for (int i = 0; i < quadrants.size(); i++) {
			for (Building b : quadrants.get(i)) {
				place(b, i);
			}
		}
	}

	void place(Building b, int quadrant) {
		int minx = AREA[0].x + 1;
		int maxx = AREA[1].x - 2;
		int midx = (minx + maxx) / 2;
		int miny = AREA[0].y + 1;
		int maxy = AREA[1].y - 2;
		int midy = (miny + maxy) / 2;
		Point p = null;
		while (p == null) {
			int xa = RPG.r(minx, midx);
			int xb = RPG.r(midx, maxx);
			int ya = RPG.r(miny, midy);
			int yb = RPG.r(midy, maxy);
			if (quadrant == 0) {
				p = new Point(xa, ya);
			} else if (quadrant == 1) {
				p = new Point(xa, yb);
			} else if (quadrant == 2) {
				p = new Point(xb, ya);
			} else if (quadrant == 3) {
				p = new Point(xb, yb);
			}
			if (!validatesurroundings(p)) {
				p = null;
				continue;
			}
		}
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				Fight.state.map[x][y].clear();
			}
		}
		b.setlocation(p);
		Fight.state.blueTeam.add(b);
	}

	boolean validatesurroundings(Point p) {
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				if (Fight.state.getcombatant(p.x, p.y) != null) {
					return false;
				}
			}
		}
		return true;
	}

	void generate(int amount, boolean randomize,
			Class<? extends ArenaBuilding> building,
			List<List<ArenaBuilding>> quadrants) {
		if (randomize) {
			amount += RPG.randomize(amount);
		}
		Collections.shuffle(quadrants);
		quadrants.sort(SizeComparator.INSTANCE);
		for (int i = 0; i < amount; i++) {
			try {
				ArenaBuilding b = building.getDeclaredConstructor()
						.newInstance();
				quadrants.get(i % 4).add(b);
				if (b instanceof ArenaFountain) {
					((ArenaFountain) b).refillchance = 1f / amount;
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	boolean validate(Point p) {
		return p.validate(AREA[0].x, AREA[0].y, AREA[1].x, AREA[1].y)
				&& !Fight.state.map[p.x][p.y].blocked
				&& Fight.state.getcombatant(p.x, p.y) == null;
	}

	static Point getmonsterentry() {
		Point p = new Point(RPG.r(MAPSIZE), RPG.r(MAPSIZE));
		if (RPG.chancein(2)) {
			p.x = RPG.chancein(2) ? AREA[0].x : AREA[1].x - 1;
		} else {
			p.y = RPG.chancein(2) ? AREA[0].y : AREA[1].y - 1;
		}
		return p;
	}
}