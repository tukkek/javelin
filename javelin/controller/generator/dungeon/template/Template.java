package javelin.controller.generator.dungeon.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.GaveUpException;
import javelin.controller.generator.dungeon.Direction;
import javelin.controller.generator.dungeon.DungeonArea;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.tables.RoomSizeTable;
import javelin.controller.generator.dungeon.template.Iterator.TemplateTile;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.generator.dungeon.template.corridor.WindingCorridor;
import javelin.controller.generator.dungeon.template.generated.Irregular;
import javelin.controller.generator.dungeon.template.generated.Linear;
import javelin.controller.generator.dungeon.template.generated.Rectangle;
import javelin.controller.generator.dungeon.template.mutator.Alcoves;
import javelin.controller.generator.dungeon.template.mutator.Grow;
import javelin.controller.generator.dungeon.template.mutator.HorizontalMirror;
import javelin.controller.generator.dungeon.template.mutator.Mutator;
import javelin.controller.generator.dungeon.template.mutator.Noise;
import javelin.controller.generator.dungeon.template.mutator.Rotate;
import javelin.controller.generator.dungeon.template.mutator.Symmetry;
import javelin.controller.generator.dungeon.template.mutator.VerticalMirror;
import javelin.controller.generator.dungeon.template.mutator.Wall;
import tyrant.mikera.engine.RPG;

/**
 * TODO most templates should be read from file, not generated
 *
 * @author alex
 */
public abstract class Template implements Cloneable, DungeonArea {

	public static final Character FLOOR = '.';
	public static final Character WALL = '█';
	public static final Character DECORATION = '!';
	public static final Character DOOR = '□';

	/** Procedurally generated templates only. */
	public static final Template[] GENERATED = new Template[] { new Irregular(),
			new Rectangle(), new Linear() };
	public static final StraightCorridor[] CORRIDORS = new StraightCorridor[] {
			new StraightCorridor(), new WindingCorridor() };
	public static final ArrayList<StaticTemplate> STATIC = new ArrayList<StaticTemplate>();

	static final ArrayList<Mutator> MUTATORS = new ArrayList<Mutator>(Arrays
			.asList(new Mutator[] { Rotate.INSTANCE, HorizontalMirror.INSTANCE,
					VerticalMirror.INSTANCE, new Symmetry(), new Noise(),
					new Wall(), new Alcoves(), Grow.INSTANCE }));
	static final ArrayList<Mutator> ROTATORS = new ArrayList<Mutator>(
			Arrays.asList(new Mutator[] { Rotate.INSTANCE,
					HorizontalMirror.INSTANCE, VerticalMirror.INSTANCE }));
	static final int FREEMUTATORS;

	static {
		int freemutators = 0;
		for (Mutator m : MUTATORS) {
			if (m.chance == null) {
				freemutators += 1;
			}
		}
		FREEMUTATORS = freemutators;
	}

	public char[][] tiles = null;
	public int width = 0;
	public int height = 0;
	public boolean corridor = false;
	protected Character fill = FLOOR;
	public double mutate = 0.1;

	protected void init(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new char[width][height];
		for (int x = 0; x < width; x++) {
			Arrays.fill(tiles[x], fill);
		}
	}

	protected void initrandom() {
		Point dimensions = DungeonGenerator.instance.tables
				.get(RoomSizeTable.class).rolldimensions();
		init(dimensions.x, dimensions.y);
	}

	public abstract void generate();

	public void modify() {
		double chance = mutate / FREEMUTATORS;
		Collections.shuffle(MUTATORS);
		for (Mutator m : MUTATORS) {
			if (corridor && !m.allowcorridor) {
				continue;
			}
			if (RPG.random() < (m.chance == null ? chance : m.chance)) {
				m.apply(this);
			}
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				Character c = tiles[x][y];
				s += c == null ? ' ' : c;
			}
			s += "\n";
		}
		return s;
	}

	public void iterate(Iterator i) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				i.iterate(new TemplateTile(x, y, tiles[x][y]));
			}
		}
	}

	protected double getarea() {
		return width * height;
	}

	public ArrayList<Point> find(char tile) {
		ArrayList<Point> found = new ArrayList<Point>();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (tiles[x][y] == tile) {
					found.add(new Point(x, y));
				}
			}
		}
		return found;
	}

	public Template create() {
		try {
			Template c = null;
			while (c == null || !c.validate()) {
				c = clone();
				c.tiles = null;
				c.width = 0;
				c.height = 0;
				c.generate();
				c.modify();
				c.close();
				c.makedoors();
			}
			return c;
		} catch (GaveUpException e) {
			return null;
		}
	}

	static HashSet<Point> walkcache = new HashSet<Point>();

	/**
	 * @return <code>true</code> if the generated template is good to use in an
	 *         actual map.
	 * @throws GaveUpException
	 *             Subclasses may throw, otherwise will continue calling
	 *             {@link #create()} infinitely. Especially useful for
	 *             sanitizing {@link StaticTemplate}s.
	 */
	protected boolean validate() throws GaveUpException {
		if (tiles == null) {
			return false;
		}
		List<Point> doors = getdoors();
		for (int a = 0; a < doors.size(); a++) {
			Point doora = doors.get(a);
			for (int b = a + 1; b < doors.size(); b++) {
				Point doorb = doors.get(b);
				walkcache.clear();
				if (!walk(new Point(doora.x, doora.y),
						new Point(doorb.x, doorb.y))) {
					return false;
				}
			}
		}
		return true;
	}

	boolean walk(Point a, Point b) {
		if (!walkcache.add(a)) {
			return false;
		}
		for (int x = a.x - 1; x <= a.x + 1; x++) {
			if (!(0 <= x && x < width)) {
				continue;
			}
			for (int y = a.y - 1; y <= a.y + 1; y++) {
				if (!(0 <= y && y < height)) {
					continue;
				}
				Point step = new Point(x, y);
				if (step.equals(b)) {
					return true;
				}
				if (step.equals(a) || tiles[x][y] != FLOOR) {
					continue;
				}
				if (walk(step, b)) {
					return true;
				}
			}
		}
		return false;
	}

	void makedoors() throws GaveUpException {
		int doors = RPG.r(corridor ? 2 : 1, 4);
		int attempts = 4 * 4;
		for (int i = 0; i < doors; i++) {
			Direction direction = Direction.getrandom();
			Point door = findentry(direction);
			if (door != null) {
				tiles[door.x][door.y] = DOOR;
				continue;
			}
			if (count(DOOR) != 0) {
				return;
			}
			i -= 1;
			attempts -= 1;
			if (attempts == 0) {
				tiles = null;
				return;
			}
		}
	}

	public int count(char c) {
		int i = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (tiles[x][y] == c) {
					i += 1;
				}
			}
		}
		return i;
	}

	Point findentry(Direction d) {
		ArrayList<Point> doors = d.getborder(this);
		Collections.shuffle(doors);
		for (Point door : doors) {
			Point p = new Point(door.x + d.reverse.x, door.y + d.reverse.y);
			if (tiles[p.x][p.y] == FLOOR && !neardoor(p)) {
				return door;
			}
		}
		return null;
	}

	boolean neardoor(Point p) {
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				Point neighbor = new Point(x, y);
				if (p.validate(0, 0, width, height)
						&& tiles[neighbor.x][neighbor.y] == DOOR) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected Template clone() {
		try {
			return (Template) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		if (isclosed()) {
			return;
		}
		width += 2;
		height += 2;
		char[][] closed = new char[width][height];
		Arrays.fill(closed[0], WALL);
		Arrays.fill(closed[width - 1], WALL);
		for (int x = 1; x < width - 1; x++) {
			closed[x][0] = WALL;
			closed[x][height - 1] = WALL;
		}
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				closed[x + 1][y + 1] = tiles[x][y];
			}
		}
		tiles = closed;
	}

	boolean isclosed() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					if (tiles[x][y] != WALL) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected boolean isborder(int x, int y) {
		return x == 0 || y == 0 || x == width - 1 || y == height - 1;
	}

	public List<Point> getdoors() {
		return find(Template.DOOR);
	}

	public Point getdoor(Direction d) {
		for (Point door : getdoors()) {
			if (inborder(door.x, door.y) == d) {
				return door;
			}
		}
		return null;
	}

	public Direction inborder(int x, int y) {
		if (x == 0) {
			return Direction.WEST;
		}
		if (x == width - 1) {
			return Direction.EAST;
		}
		if (y == 0) {
			return Direction.SOUTH;
		}
		if (y == height - 1) {
			return Direction.NORTH;
		}
		return null;
	}

	public Point rotate(Direction to) {
		Point todoor = null;
		while (todoor == null) {
			todoor = getdoor(to);
			if (todoor == null) {
				RPG.pick(ROTATORS).apply(this);
			}
		}
		return todoor;
	}

	@Override
	public int getwidth() {
		return width;
	}

	@Override
	public int getheight() {
		return height;
	}

	@Override
	public int getx() {
		return 0;
	}

	@Override
	public int gety() {
		return 0;
	}

	public int countadjacent(Character tile, Point p) {
		int found = 0;
		for (int x = p.x - 1; x <= p.x + 1; x++) {
			for (int y = p.y - 1; y <= p.y + 1; y++) {
				if (new Point(x, y).validate(0, 0, width, height)
						&& tile.equals(tiles[x][y])) {
					found += 1;
				}
			}
		}
		return found;
	}

	public void settiles(char[][] t) {
		tiles = t;
		width = t.length;
		height = t[0].length;
	}
}