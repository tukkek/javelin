package javelin.controller.generator.dungeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.controller.generator.dungeon.VirtualMap.Room;
import javelin.controller.generator.dungeon.tables.ConnectionTable;
import javelin.controller.generator.dungeon.template.Direction;
import javelin.controller.generator.dungeon.template.Irregular;
import javelin.controller.generator.dungeon.template.Template;
import javelin.view.screen.town.SelectScreen;

public class DungeonGenerator {
	static final boolean DEBUGROOMS = true;
	/** Procedurally generated templates only. */
	static final Template[] TEMPLATES = new Template[] { new Irregular() };
	/**
	 * How many times to generate each procedurally-generated {@link Template}.
	 */
	static final int PERMUTATIONS = 100;

	public String ascii;

	LinkedList<Template> pool = new LinkedList<Template>();
	ConnectionTable connections = new ConnectionTable();
	VirtualMap map = new VirtualMap();

	private DungeonGenerator() {
		generatepool();
		draw(pool.pop(), new Point(0, 0));
		ascii = map.rasterize(true);
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
			Point cursorb = from.connect(new Point(cursor), t, next, door,
					doorb);
			if (draw(next, cursorb)) {
				map.set(Template.FLOOR, cursorb.x + doorb.x,
						cursorb.y + doorb.y);
			} else {
				map.set(Template.WALL, cursor.x + door.x, cursor.y + door.y);
			}
		}
		return true;
	}

	void generatepool() {
		for (Template t : TEMPLATES) {
			for (int i = 0; i < PERMUTATIONS; i++) {
				pool.add(t.create());
			}
		}
		Collections.shuffle(pool);
	}

	public static void main(String[] args) {
		DungeonGenerator dungeon = generate(13, 13 * 2);
		dungeon.print();
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
		write(builder.toString());
	}

	private static DungeonGenerator generate(int minrooms, int maxrooms) {
		DungeonGenerator dungeon = null;
		while (dungeon == null) {
			dungeon = new DungeonGenerator();
			int size = dungeon.map.rooms.size();
			if (!(minrooms <= size && size <= maxrooms)) {
				dungeon = null;
			}
		}
		return dungeon;
	}

	static void write(String log) { // debug
		try {
			Preferences.write(log, "/tmp/dungeon.txt");
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
}
