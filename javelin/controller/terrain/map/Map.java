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
	public Square[][] map;

	public Image floor = Images.getImage("terraindirt");
	public Image wall = Images.getImage("terrainwall");
	public Image wallfloor = null;
	public Image obstacle = Images.getImage("terrainbush");
	protected Image rock = Images.getImage("terrainrock");

	public int maxflooding = Weather.STORM;
	public Image flooded = Images.getImage("terrainflooded");

	public Map(int width, int height) {
		map = new Square[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				map[x][y] = new Square(false, false, false);
			}
		}
	}

	abstract public void generate();

	public void putwater(int x, int y) {
		map[x][y].flooded = true;
	}

	final public void putobstacle(int x, int y) {
		map[x][y].obstructed = true;
	}

	public Image getobstacle() {
		return obstacle;
	}

	final public void putwall(int x, int y) {
		map[x][y].blocked = true;
	}

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
