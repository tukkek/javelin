package javelin.controller.map;

import java.util.HashSet;

import javelin.controller.Point;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Tile;

/**
 * Generates a map based on the d20 SRD suggestions for terrain.
 * 
 * @author alex
 */
public abstract class DndMap extends Map {
	public static final int SIZE = 35;
	double walls, obstacles, water;

	public DndMap(double wallsp, double obstaclesp, double waterp) {
		super(SIZE, SIZE);
		walls = wallsp;
		obstacles = obstaclesp;
		water = waterp;
	}

	@Override
	public void generate() {
		map.fillArea(0, 0, map.width - 1, map.height - 1, floor);
		final HashSet<Point> occupied = new HashSet<Point>();
		int area = map.width * map.height;
		int walls = (int) (this.walls * area);
		int obstacles = (int) (this.obstacles * area);
		int water = (int) (this.water * area);
		while (walls > 0 || obstacles > 0 || water > 0) {
			Point p = new Point(RPG.r(0, map.width - 1),
					RPG.r(0, map.height - 1));
			if (occupied.contains(p)) {
				continue;
			}
			occupied.add(p);
			if (walls > 0 && walls >= obstacles && walls >= water) {
				putwall(p.x, p.y);
				walls -= 1;
			} else if (obstacles > 0 && obstacles >= water) {
				putobstacle(p.x, p.y);
				obstacles -= 1;
			} else {
				putwater(p.x, p.y);
				water -= 1;
			}
		}
	}

	public void putwater(int x, int y) {
		map.setTile(x, y, Tile.POOL);
	}

	public void putobstacle(int x, int y) {
		map.addThing(Lib.create("bush"), x, y);
	}

	public void putwall(int x, int y) {
		map.setTile(x, y, Tile.WALL);
	}
}
