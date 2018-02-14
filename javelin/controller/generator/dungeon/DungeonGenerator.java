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
				map.set(Template.WALL, cursor.x + door.x, cursor.y + door.y);
				continue;
			}
			Template next = pool.pop();
			Direction from = t.inborder(door.x, door.y);
			Direction to = Direction.opposite(from);
			Point doorb = next.rotate(to);
			Point cursorb = new Point(cursor);
			cursorb = from.connect(cursorb, t, next, door, doorb);
			Corridor.clear(t, cursor, door, next, doorb, map);
			if (draw(next, cursorb)) {
				map.set(Template.FLOOR, cursorb.x + doorb.x,
						cursorb.y + doorb.y);
			} else {
				map.set(Template.WALL, cursor.x + door.x, cursor.y + door.y);
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
		// write(builder.toString());
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
		return generate(13 / 2, 13 * 2);
	}
}
