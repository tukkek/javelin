package javelin.controller.generator.dungeon.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.dungeon.Direction;
import javelin.controller.generator.dungeon.DungeonArea;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.Iterator.TemplateTile;
import javelin.controller.generator.dungeon.template.corridor.StraightCorridor;
import javelin.controller.generator.dungeon.template.corridor.WindingCorridor;
import javelin.controller.generator.dungeon.template.generated.Irregular;
import javelin.controller.generator.dungeon.template.generated.Linear;
import javelin.controller.generator.dungeon.template.generated.Rectangle;
import javelin.controller.generator.dungeon.template.mutator.Alcoves;
import javelin.controller.generator.dungeon.template.mutator.Grow;
import javelin.controller.generator.dungeon.template.mutator.Hallway;
import javelin.controller.generator.dungeon.template.mutator.HorizontalMirror;
import javelin.controller.generator.dungeon.template.mutator.Mutator;
import javelin.controller.generator.dungeon.template.mutator.Noise;
import javelin.controller.generator.dungeon.template.mutator.Rotate;
import javelin.controller.generator.dungeon.template.mutator.Symmetry;
import javelin.controller.generator.dungeon.template.mutator.VerticalMirror;
import javelin.controller.generator.dungeon.template.mutator.Wall;
import javelin.controller.table.dungeon.RoomSizeTable;
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
	public static final Template[] CORRIDORS = new Template[] {
			new StraightCorridor(), new WindingCorridor() };

	static final ArrayList<Mutator> MUTATORS = new ArrayList<Mutator>(Arrays
			.asList(new Mutator[] { Rotate.INSTANCE, HorizontalMirror.INSTANCE,
					VerticalMirror.INSTANCE, new Symmetry(), new Noise(),
					new Wall(), new Alcoves(), Grow.INSTANCE, new Hallway() }));
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

	public boolean corridor = false;
	public char[][] tiles = null;
	public double mutate = 0.1;
	public int width = 0;
	public int height = 0;

	protected Character fill = FLOOR;
	public int doors = RPG.r(1, 4);

	protected void init(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new char[width][height];
		for (int x = 0; x < width; x++) {
			Arrays.fill(tiles[x], fill);
		}
	}

	protected void initrandom() {
		RoomSizeTable table = DungeonGenerator.instance.tables
				.get(RoomSizeTable.class);
		init(table.rollnumber(), table.rollnumber());
	}

	public abstract void generate();

	public void modify() {
		if (DungeonGenerator.DEBUGMUTATOR != null) {
			DungeonGenerator.DEBUGMUTATOR.apply(this);
			return;
		}
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
		} catch (GaveUp e) {
			return null;
		}
	}

	/**
	 * @return <code>true</code> if the generated template is good to use in an
	 *         actual map.
	 * @throws GaveUp
	 *             Subclasses may throw, otherwise will continue calling
	 *             {@link #create()} infinitely. Especially useful for
	 *             sanitizing {@link StaticTemplate}s.
	 */
	protected boolean validate() throws GaveUp {
		if (tiles == null) {
			return false;
		}
		List<Point> doors = getdoors();
		final HashSet<Point> free = new HashSet<Point>(width * height);
		walk(doors.get(0), free);
		for (Point door : doors) {
			if (!free.contains(door)) {
				return false;
			}
		}
		iterate(new Iterator() {
			@Override
			public void iterate(TemplateTile t) {
				if (!free.contains(new Point(t.x, t.y))) {
					tiles[t.x][t.y] = WALL;
				}
			}
		});
		return true;
	}

	void walk(Point tile, HashSet<Point> free) {
		if (!free.add(tile)) {
			return;
		}
		for (Point step : Point.getadjacent()) {
			step.x += tile.x;
			step.y += tile.y;
			if (step.validate(0, 0, width, height)
					&& tiles[step.x][step.y] != WALL) {
				walk(step, free);
			}
		}
	}

	void makedoors() throws GaveUp {
		if (corridor && doors == 1) {
			doors = 2;
		}
		int attempts = doors * 4;
		while (attempts > 0 && doors > 0) {
			Direction direction = Direction.getrandom();
			Point door = findentry(direction);
			if (door == null) {
				attempts -= 1;
			} else {
				tiles[door.x][door.y] = DOOR;
				doors -= 1;
			}
		}
		if (attempts == 0) {
			tiles = null;
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