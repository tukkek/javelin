package javelin.controller.generator.dungeon.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Iterator.TemplateTile;
import tyrant.mikera.engine.RPG;

public abstract class Template implements Cloneable {
	public static final char FLOOR = '.';
	public static final char WALL = '#';
	public static final char DECORATION = '!';
	public static final char DOOR = '+';

	public char[][] tiles = null;
	public int width = 0;
	public int height = 0;
	// public boolean hasdecoration;

	void init(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new char[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				tiles[x][y] = FLOOR;
			}
		}
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
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
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

	protected int count(char tile) {
		int count = 0;
		int w = width;
		int h = height;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (tiles[x][y] == tile) {
					count += 1;
				}
			}
		}
		return count;
	}

	public Template create() {
		generate();
		modify();
		close();
		makedoors();
		return clone();
	}

	void makedoors() {
		int doors = RPG.r(1, 4);
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
		}
	}

	Point findentry(Direction d) {
		ArrayList<Point> doors = d.getborder(this);
		Collections.shuffle(doors);
		for (Point door : doors) {
			if (tiles[door.x - d.delta.x][door.y - d.delta.y] == FLOOR) {
				return door;
			}
		}
		return null;
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

}