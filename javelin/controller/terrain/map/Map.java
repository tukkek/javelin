package javelin.controller.terrain.map;

import java.awt.Image;

import javelin.controller.Weather;
import javelin.model.BattleMap;
import javelin.model.state.Square;
import javelin.view.Images;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Tile;

/**
 * Representation of a battle area.
 * 
 * @author alex
 */
public abstract class Map {
	public BattleMap battlemap;
	public Square[][] map;

	public Image floor = Images.getImage("terraindirt");
	public Image wall = Images.getImage("terrainwall");
	public Image wallfloor = null;
	public Image obstacle = Images.getImage("terrainbush");
	protected Image rock = Images.getImage("terrainrock");

	public int maxflooding = Weather.STORM;
	public Image flooded = Images.getImage("terrainflooded");

	public Map(BattleMap mapp) {
		this.battlemap = mapp;
		if (mapp != null) {
			map = new Square[mapp.width][mapp.height];
			for (int x = 0; x < mapp.width; x++) {
				for (int y = 0; y < mapp.height; y++) {
					map[x][y] = new Square(false, false, false);
				}
			}
		}
	}

	public Map(int width, int heigth) {
		this(new BattleMap(width, heigth));
	}

	abstract public void generate();

	public void putwater(int x, int y) {
		// battlemap.setTile(x, y, Tile.POOL);
		map[x][y].flooded = true;
		addthing(x, y, flooded, battlemap);
	}

	final public void putobstacle(int x, int y) {
		addthing(x, y, getobstacle(), battlemap);
		map[x][y].obstructed = true;
	}

	static public void addthing(int x, int y, Image getobstacle, BattleMap m) {
		Thing t = Lib.create("bush");
		t.javelinimage = getobstacle;
		m.addThing(t, x, y);
	}

	public Image getobstacle() {
		return obstacle;
	}

	final public void putwall(int x, int y) {
		map[x][y].blocked = true;
		battlemap.setTile(x, y, Tile.WALL);
		if (wallfloor != null) {
			addthing(x, y, wall, battlemap);
		}
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
