package javelin.controller.generator.dungeon.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.GaveUpException;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.Roomlike;
import javelin.controller.generator.dungeon.tables.RoomSizeTable;
import javelin.controller.generator.dungeon.template.Iterator.TemplateTile;
import javelin.controller.generator.dungeon.template.corridor.LinearCorridor;
import javelin.controller.generator.dungeon.template.corridor.WindingCorridor;
import javelin.controller.generator.dungeon.template.generated.Irregular;
import javelin.controller.generator.dungeon.template.generated.Rectangle;
import tyrant.mikera.engine.RPG;

/**
 * TODO most templates should be read from file, not generated
 *
 * @author alex
 */
public abstract class Template implements Cloneable, Roomlike {

	public static final Character FLOOR = '.';
	public static final Character WALL = '█';
	public static final Character DECORATION = '!';
	public static final Character DOOR = '□';

	/** Procedurally generated templates only. */
	public static final Template[] GENERATED = new Template[] { new Irregular(),
			new Rectangle() };
	public static final LinearCorridor[] CORRIDORS = new LinearCorridor[] {
			new LinearCorridor(), new WindingCorridor() };
	public static final ArrayList<Template> STATIC = new ArrayList<Template>();

	public char[][] tiles = null;
	public int width = 0;
	public int height = 0;
	public boolean corridor = false;
	protected Character fill = FLOOR;

	protected void init(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new char[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = fill;
			}
		}
	}

	protected void initrandom() {
		Point dimensions = DungeonGenerator.instance.tables
				.get(RoomSizeTable.class).rolldimensions();
		init(dimensions.x, dimensions.x);
	}

	public abstract void generate();

	public void modify() {
		if (RPG.chancein(2)) {
			rotate();
		}
		if (RPG.chancein(2)) {
			mirrorhorizontally();
		}
		if (RPG.chancein(2)) {
			mirrorvertically();
		}
	}

	private void mirrorvertically() {
		for (int x = 0; x < width; x++) {
			char[] original = Arrays.copyOf(tiles[x], height);
			for (int y = 0; y < height; y++) {
				tiles[x][height - 1 - y] = original[y];
			}
		}
	}

	void mirrorhorizontally() {
		char[][] original = Arrays.copyOf(tiles, width);
		for (int x = 0; x < width; x++) {
			tiles[width - x - 1] = original[x];
		}
	}

	void rotate() {
		char[][] rotated = new char[height][width];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				rotated[y][x] = tiles[x][y];
			}
		}
		tiles = rotated;
		Point dimensions = new Point(width, height);
		width = dimensions.y;
		height = dimensions.x;
	}

	@Override
	public String toString() {
		String s = "";
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				s += tiles[x][y];
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
			do {
				tiles = null;
				width = 0;
				height = 0;
				generate();
				modify();
				close();
				makedoors();
			} while (!validate());
			return clone();
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

	void makedoors() {
		int doors = RPG.r(corridor ? 2 : 1, 4);
		int attempts = 10000;
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
		return find(c).size();
	}

	Point findentry(Direction d) {
		ArrayList<Point> doors = d.getborder(this);
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
		width += 2;
		height += 2;
		char[][] closed = new char[width + 2][height + 2];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (isborder(x, y)) {
					closed[x][y] = WALL;
				} else {
					closed[x][y] = tiles[x - 1][y - 1];
				}
			}
		}
		tiles = closed;
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
				modify();
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
}