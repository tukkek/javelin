package javelin.controller.terrain.map;

import java.awt.Image;

import javelin.controller.Weather;
import javelin.model.state.Square;
import javelin.view.Images;

/**
 * Representation of a battle area.
 * 
 * @author alex
 */
public abstract class Map {
	/**
	 * Two-dimensional array of squares. map[point.x][point.y]
	 */
	public Square[][] map;

	/** Background image. */
	public Image floor = Images.getImage("terraindirt");
	/** Represents {@link Square#blocked}. */
	public Image wall = Images.getImage("terrainwall");
	/**
	 * If given, instead of using {@link #wall} on top of a {@link #floor}
	 * bakcground will use this instead.
	 */
	public Image wallfloor = null;
	/** Represents an {@link Square#obstructed} item. */
	public Image obstacle = Images.getImage("terrainbush");
	/** Used often as an {@link #obstacle}. */
	protected Image rock = Images.getImage("terrainrock");
	/** Represents {@link Square#flooded}. */
	public Image flooded = Images.getImage("terrainflooded");
	/** See {@link Weather}. By default allows any extent of flooding. */
	public int maxflooding = Weather.STORM;

	/**
	 * Construcor based on map size. By default all {@link Square}s are
	 * completely free.
	 */
	public Map(int width, int height) {
		map = new Square[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				map[x][y] = new Square(false, false, false);
			}
		}
	}

	/**
	 * Creates the {@link #map}. Called after construction to allow for fast
	 * instantiation of a {@link Maps} list and lazy generation operation.
	 */
	abstract public void generate();

	/** Marks {@link Square#flooded}. */
	public void putwater(int x, int y) {
		map[x][y].flooded = true;
	}

	/** Marks {@link Square#obstructed}. */
	final public void putobstacle(int x, int y) {
		map[x][y].obstructed = true;
	}

	/**
	 * @return {@link #obstacle} representation for this map. May be overridden
	 *         for dynamic purposes.
	 */
	public Image getobstacle() {
		return obstacle;
	}

	/** Marks {@link Square#blocked}. */
	final public void putwall(int x, int y) {
		map[x][y].blocked = true;
	}

	/**
	 * @return Image that represents {@link Square#blocked}.
	 * @see #wall
	 * @see #wallfloor
	 */
	public Image getblockedtile(int x, int y) {
		return wallfloor == null ? wall : wallfloor;
	}

	/**
	 * @return <code>false</code> if this map can't be used now due to any
	 *         circumstances.
	 */
	public boolean validate() {
		return true;
	}
}
