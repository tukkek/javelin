package javelin.controller.generator.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.VirtualMap.Room;
import javelin.controller.generator.dungeon.tables.ConnectionTable;
import javelin.controller.generator.dungeon.template.Direction;
import javelin.controller.generator.dungeon.template.StaticTemplate;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.generator.dungeon.template.generated.Corridor;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

public class DungeonGenerator {
	static final boolean DEBUG = true;
	static final boolean DEBUGROOMS = true;
	static final int DEBUGSIZE = 1;

	public String ascii;
	public char[][] grid;

	LinkedList<Template> pool = new LinkedList<Template>();
	ConnectionTable connections = new ConnectionTable();
	VirtualMap map = new VirtualMap();

	/**
	 * @param sizehint
	 *            TOOD would be cool to have this handled built-in, not on
	 *            {@link #generate(int, int)}.
	 */
	private DungeonGenerator(int sizehint) {
		generatepool(sizehint);
		draw(pool.pop(), new Point(0, 0));
		/* TODO make this a Table 5±10 */
		int corridorattempts = map.rooms.size() * RPG.r(0, 10);
		for (int i = 0; i < corridorattempts; i++) {
			createcorridor();
		}
		finish();
	}

	void createcorridor() {
		Room r = RPG.pick(map.rooms);
		Direction d = Direction.getrandom();
		// d = Direction.SOUTH;
		Point exit = RPG.pick(d.getborder(r));
		if (countadjacent(exit, Template.FLOOR) == 0) {
			return;
		}
		ArrayList<Point> corridor = new ArrayList<Point>();
		int length = RPG.r(1, 4) + RPG.r(1, 4) + 1;
		boolean connected = false;
		for (int i = 0; i < length; i++) {
			Point step = new Point(exit);
			step.x -= d.reverse.x * i;
			step.y -= d.reverse.y * i;
			if (countadjacent(step, Template.DOOR) > 0) {
				return;
			}
			corridor.add(step);
			Character tile = map.get(step);
			// map.set(Integer.toString(i).charAt(0), step);
			// TODO check door
			if (corridor.size() > 1
					&& countadjacent(step, Template.FLOOR) == 1) {
				// corridor.remove(step);
				connected = true;
				break;
			}
			if (Template.WALL.equals(tile) || tile == null) {
				continue;
			}
			return;
		}
		if (connected && corridor.size() > 2) {
			// if (countadjacent(door, Template.DOOR) > 0) {
			// return;
			// }
			for (Point step : corridor) {
				map.set(Template.FLOOR, step);
			}
			Point door = corridor.get(corridor.size() - 1);
			map.set(Template.DOOR, door);

		}
	}

	public int countadjacent(Point p, Character tile) {
		int found = 0;
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				if (tile.equals(map.get(new Point(x, y)))) {
					found += 1;
				}
			}
		}
		return found;
	}

	public void finish() {
		ascii = map.rasterize(true).replaceAll(" ",
				Character.toString(Template.WALL));
		String[] grid = ascii.split("\n");
		this.grid = new char[grid.length][];
		for (int i = 0; i < grid.length; i++) {
			this.grid[i] = grid[i].toCharArray();
		}
	}

	boolean draw(Template t, Point cursor) {
		if (!map.draw(t, cursor.x, cursor.y)) {
			return false;
		}
		for (Point door : t.getdoors()) {
			if (pool.isEmpty()) {
				if (Javelin.DEBUG) {
					System.out.println("#dungeongenerator empty pool");
				}
				map.set(Template.WALL, cursor, door);
				continue;
			}
			Template next = pool.pop();
			Direction going = t.inborder(door.x, door.y);
			Direction coming = Direction.opposite(going);
			Point doorb = next.rotate(coming);
			Point cursorb = new Point(cursor);
			cursorb = going.connect(cursorb, t, next, door, doorb);
			Corridor.clear(t, cursor, door, next, doorb, map);
			if (draw(next, cursorb)) {
				map.set(Template.FLOOR, cursorb, doorb);
			} else {
				map.set(Template.WALL, cursor, door);
			}
		}
		return true;
	}

	void generatepool(int sizehint) {
		List<Template> templates = new LinkedList<Template>(
				Arrays.asList(Template.GENERATED));
		Collections.shuffle(templates);
		templates = templates.subList(0,
				Math.min(RPG.r(1, 4), templates.size()));
		int permutations = 10 * sizehint / templates.size();
		if (RPG.chancein(2)) {
			templates.add(Template.CORRIDOR);
		}
		for (Template t : templates) {
			for (int i = 0; i < permutations; i++) {
				pool.add(t.create());
			}
		}
		if (RPG.chancein(2)) {
			Collections.shuffle(StaticTemplate.STATIC);
			int target = pool.size() / templates.size();
			pool.addAll(StaticTemplate.STATIC.subList(0,
					Math.min(target, StaticTemplate.STATIC.size())));
		}
		Collections.shuffle(pool);
	}

	void print() {
		String[] lines = ascii.split("\n");
		char[][] map = new char[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			map[i] = lines[i].toCharArray();
		}
		if (DEBUGROOMS) {
			ArrayList<Room> rooms = this.map.rooms;
			for (int i = 0; i < rooms.size(); i++) {
				Room r = rooms.get(i);
				for (int x = r.x; x < r.x + r.width; x++) {
					for (int y = r.y; y < r.y + r.height; y++) {
						if (map[x][y] == Template.FLOOR) {
							map[x][y] = SelectScreen.getkey(i);
						}
					}
				}
			}
		}
		StringBuilder builder = new StringBuilder();
		for (char[] line : map) {
			builder.append(line);
			builder.append('\n');
		}
		System.out.println(ascii);
	}

	public static DungeonGenerator generate(int minrooms, int maxrooms) {
		minrooms *= DEBUGSIZE;
		maxrooms *= DEBUGSIZE;
		DungeonGenerator dungeon = null;
		while (dungeon == null) {
			dungeon = new DungeonGenerator((minrooms + maxrooms) / 2);
			int size = dungeon.map.rooms.size();
			if (!(minrooms <= size && size <= maxrooms)) {
				dungeon = null;
			}
		}
		return dungeon;
	}

	public static void main(String[] args) {
		generate().print();
	}

	public static DungeonGenerator generate() {
		return generate(13, 13 * 2);
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
