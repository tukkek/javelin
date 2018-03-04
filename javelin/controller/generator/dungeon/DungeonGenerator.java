package javelin.controller.generator.dungeon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.VirtualMap.Room;
import javelin.controller.generator.dungeon.tables.LevelTables;
import javelin.controller.generator.dungeon.template.StaticTemplate;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.generator.dungeon.template.mutator.Mutator;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

public class DungeonGenerator {
	public static final boolean DEBUG = false;
	public static final Template DEBUGTEMPLATE = DEBUG ? null : null;
	public static final Template DEBUGCORRIDOR = DEBUG ? null : null;
	public static final Mutator DEBUGMUTATOR = DEBUG ? null : null;
	static final boolean DEBUGROOMS = true;
	static final int DEBUGSIZE = 1;

	/**
	 * TODO temporary: will need to be refactored when more than one level can
	 * be generated (with one set of tables/parameters per level) and/or for
	 * multithreading. Should be as simple as passing an instance of this or of
	 * a new class GeneratorLevel to Templates.
	 */
	public static DungeonGenerator instance;

	static int ncorridors;
	static int ntemplates;

	public LevelTables tables = new LevelTables();
	public char[][] grid;
	public String ascii;

	LinkedList<Segment> segments = new LinkedList<Segment>();
	ArrayList<Template> pool = new ArrayList<Template>();
	VirtualMap map = new VirtualMap();
	String templatesused;
	int attempts = 3000;
	private int minrooms;
	private int maxrooms;

	static {
		setupparameters();
	}

	/**
	 * @param maxrooms
	 * @param minrooms
	 * @param sizehint
	 *            TOOD would be cool to have this handled built-in, not on
	 *            {@link #generate(int, int)}.
	 */
	private DungeonGenerator(int minrooms, int maxrooms) {
		this.minrooms = minrooms;
		this.maxrooms = maxrooms;
		instance = this;
		generatepool();
		draw();
		/* TODO make this a Table 5±10 */
		int connectionattempts = map.rooms.size() * RPG.r(0, 10);
		for (int i = 0; i < connectionattempts; i++) {
			createconnection();
		}
		finish();
	}

	Template generateroom() {
		Template t = null;
		while (t == null) {
			t = RPG.pick(pool).create();
		}
		return t;
	}

	/**
	 * TODO doesn't need necesarily to create only based on rooms
	 */
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

	void draw() {
		Template start = generateroom();
		segments.add(new Segment(start, new Point(0, 0)));
		map.draw(start, 0, 0);
		int nrooms = RPG.r(minrooms, maxrooms);
		while (nrooms > 0 && !segments.isEmpty()) {
			Segment s = RPG.pick(segments);
			segments.remove(s);
			List<Point> doors = s.room.getdoors();
			Collections.shuffle(doors);
			for (Point door : doors) {
				attempts -= 1;
				if (attempts == 0) {
					if (DEBUG) {
						System.err.println("#dungeongenerator empty pool");
					}
					map.set(Template.WALL, s.cursor, door);
					continue;
				}
				Template next = generateroom();
				Direction going = s.room.inborder(door.x, door.y);
				if (going == null) {
					/* static template with internal door */
					map.set(Template.FLOOR, door.x, door.y);
					continue;
				}
				Direction coming = Direction.opposite(going);
				Point doorb = next.rotate(coming);
				Point cursorb = new Point(s.cursor);
				cursorb = going.connect(cursorb, s.room, next, door, doorb);
				StraightCorridor.clear(s.room, s.cursor, door, next, doorb,
						map);
				if (map.draw(next, cursorb.x, cursorb.y)) {
					map.set(Template.FLOOR, cursorb, doorb);
					nrooms -= 1;
					segments.add(new Segment(next, cursorb));
				} else if (map.get(s.cursor, door).equals(Template.DOOR)) {
					map.set(Template.WALL, s.cursor, door);
				}
			}
		}
		for (Segment s : segments) {
			for (Point door : s.room.getdoors()) {
				if (map.get(s.cursor, door).equals(Template.DOOR)) {
					map.set(Template.WALL, s.cursor, door);
				}
			}
		}
	}

	void generatepool() {
		pool.addAll(selectrooms());
		pool.addAll(selectcorridors());
		if (RPG.chancein(2) && DEBUGTEMPLATE == null) {
			pool.add(StaticTemplate.FACTORY);
		}
		for (Template t : pool) {
			templatesused += t.getClass().getSimpleName() + " ";
		}
	}

	List<Template> selectrooms() {
		List<Template> templates;
		if (DEBUGTEMPLATE != null) {
			templates = new ArrayList<Template>(1);
			templates.add(DEBUGTEMPLATE);
			return templates;
		}
		templates = new ArrayList<Template>(Arrays.asList(Template.GENERATED));
		Collections.shuffle(templates);
		return templates.subList(0, Math.min(ntemplates, templates.size()));
	}

	List<Template> selectcorridors() {
		ArrayList<Template> corridors = new ArrayList<Template>();
		if (DEBUGCORRIDOR != null) {
			corridors.add(DEBUGCORRIDOR);
			return corridors;
		}
		if (ncorridors == 0) {
			return corridors;
		}
		corridors.addAll(Arrays.asList(Template.CORRIDORS));
		Collections.shuffle(corridors);
		return corridors.subList(0, Math.min(ncorridors, corridors.size()));
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

	/**
	 * Called to set-up default parameters. You may call this method to "reset"
	 * and then provide your own before calling {@link #generate()}.
	 *
	 * This step is done in advance - otherwise the random generator will just
	 * naturally select "easy" parameters. This way parameters are "set" and the
	 * generator needs to rety as many times as necesar to achieve them.
	 */
	public static void setupparameters() {
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
	public static DungeonGenerator generate(int minrooms, int maxrooms) {
		StaticTemplate.load();
		DungeonGenerator dungeon = null;
		while (dungeon == null) {
			dungeon = new DungeonGenerator(minrooms, maxrooms);
			int size = dungeon.map.rooms.size();
			if (!(minrooms <= size && size <= maxrooms)) {
				dungeon = null;
			}
		}
		return dungeon;
	}

	public static void main(String[] args) throws IOException {
		int minrooms = 5 + RPG.randomize(2);
		int maxrooms = 10 + RPG.randomize(3);
		minrooms = 13;
		maxrooms = 13 * 2;
		DungeonGenerator dungeon = generate(minrooms, maxrooms);
		dungeon.print();
		System.out.println(dungeon.templatesused);
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
