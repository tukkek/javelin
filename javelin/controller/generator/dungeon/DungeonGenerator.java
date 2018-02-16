package javelin.controller.generator.dungeon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.VirtualMap.Room;
import javelin.controller.generator.dungeon.tables.LevelTables;
import javelin.controller.generator.dungeon.template.Direction;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.generator.dungeon.template.corridor.LinearCorridor;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

public class DungeonGenerator {
	static final boolean DEBUG = true;
	static final Template DEBUGTEMPLATE = null;
	static final Template DEBUGCORRIDOR = null;
	static final boolean DEBUGROOMS = true;
	static final int DEBUGSIZE = 1;
	static final int POOLTARGET = 100;

	public String ascii;
	public char[][] grid;

	LinkedList<Template> pool = new LinkedList<Template>();
	LevelTables tables = new LevelTables();
	VirtualMap map = new VirtualMap();

	static int ntemplates;
	static int ncorridors;
	private static int minrooms;
	private static int maxrooms;

	static {
		setupparameters();
	}

	/**
	 * @param sizehint
	 *            TOOD would be cool to have this handled built-in, not on
	 *            {@link #generate(int, int)}.
	 */
	private DungeonGenerator(int sizehint) {
		generatepool(sizehint);
		draw(pool.pop(), new Point(0, 0));
		/* TODO make this a Table 5±10 */
		int connectionattempts = map.rooms.size() * RPG.r(0, 10);
		for (int i = 0; i < connectionattempts; i++) {
			createconnection();
		}
		finish();
	}

	void createconnection() {
		Room r = RPG.pick(map.rooms);
		Direction d = Direction.getrandom();
		Point exit = RPG.pick(d.getborder(r));
		if (map.countadjacent(Template.FLOOR, exit) == 0) {
			return;
		}
		ArrayList<Point> connection = new ArrayList<Point>();
		int length = RPG.r(1, 4) + RPG.r(1, 4) + 1;
		boolean connected = false;
		for (int i = 0; i < length; i++) {
			Point step = new Point(exit);
			step.x -= d.reverse.x * i;
			step.y -= d.reverse.y * i;
			if (map.countadjacent(Template.DOOR, step) > 0) {
				return;
			}
			connection.add(step);
			Character tile = map.get(step);
			if (connection.size() > 1
					&& map.countadjacent(Template.FLOOR, step) == 1) {
				connected = true;
				break;
			}
			if (Template.WALL.equals(tile) || tile == null) {
				continue;
			}
			return;
		}
		drawconnection(connection, connected);
	}

	void drawconnection(ArrayList<Point> connection, boolean connected) {
		if (connected && connection.size() > 2) {
			for (Point step : connection) {
				map.set(Template.FLOOR, step);
			}
			Point door = connection.get(connection.size() - 1);
			map.set(Template.DOOR, door);
		}
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
				if (DEBUG) {
					System.err.println("#dungeongenerator empty pool");
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
			LinearCorridor.clear(t, cursor, door, next, doorb, map);
			if (draw(next, cursorb)) {
				map.set(Template.FLOOR, cursorb, doorb);
			} else {
				map.set(Template.WALL, cursor, door);
			}
		}
		return true;
	}

	void generatepool(int sizehint) {
		List<Template> templates = selectrooms();
		templates.addAll(selectcorridors());
		int permutations = POOLTARGET * sizehint / templates.size();
		for (Template t : templates) {
			for (int i = 0; i < permutations; i++) {
				pool.add(t.create());
			}
		}
		if (RPG.chancein(2)) {
			Collections.shuffle(Template.STATIC);
			int target = pool.size() / templates.size();
			pool.addAll(Template.STATIC.subList(0,
					Math.min(target, Template.STATIC.size())));
		}
		Collections.shuffle(pool);
	}

	List<Template> selectrooms() {
		List<Template> templates;
		if (DEBUG && DEBUGTEMPLATE != null) {
			templates = new ArrayList<Template>(1);
			templates.add(DEBUGTEMPLATE);
			return templates;
		}
		templates = new ArrayList<Template>(Arrays.asList(Template.GENERATED));
		Collections.shuffle(templates);
		return templates.subList(0, Math.min(ntemplates, templates.size()));
	}

	ArrayList<Template> selectcorridors() {
		ArrayList<Template> corridors = new ArrayList<Template>();
		if (DEBUG && DEBUGCORRIDOR != null) {
			corridors.add(DEBUGCORRIDOR);
			return corridors;
		}
		if (ncorridors == 0) {
			return corridors;
		}
		corridors.addAll(Arrays.asList(Template.CORRIDORS));
		Collections.shuffle(corridors);
		for (int i = 0; i < ncorridors; i++) {
			if (i < corridors.size()) {
				corridors.add(corridors.get(i));
			}
		}
		return corridors;
	}

	void print() {
		String[] lines = ascii.split("\n");
		char[][] map = new char[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			map[i] = lines[i].toCharArray();
		}
		if (DEBUG && DEBUGROOMS) {
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

	/**
	 * Called to set-up default parameters. You may call this method to "reset"
	 * and then provide your own before calling {@link #generate()}.
	 *
	 * This step is done in advance - otherwise the random generator will just
	 * naturally select "easy" parameters. This way parameters are "set" and the
	 * generator needs to rety as many times as necesar to achieve them.
	 */
	public static void setupparameters() {
		minrooms = 13 * DEBUGSIZE;
		maxrooms = 13 * 2 * DEBUGSIZE;
		ntemplates = RPG.r(1, 4);
		ncorridors = 0;
		while (RPG.chancein(2)) {
			ncorridors += 1;
		}
		ncorridors = Math.min(ncorridors, ntemplates);
	}

	/**
	 * @return A dungeon map, ready for drawing.
	 *
	 * @see VirtualMap#rooms
	 * @see #setupparameters()
	 */
	public static DungeonGenerator generate() {
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
		setupparameters();
		generate().print();
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
