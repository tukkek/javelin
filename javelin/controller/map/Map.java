package javelin.controller.map;

import javelin.controller.Weather;
import javelin.model.BattleMap;
import tyrant.mikera.tyrant.Tile;

/**
 * Representation of a battle area.
 * 
 * @author alex
 */
public abstract class Map {
	public BattleMap map;
	public int floor = Tile.FLOOR;
	public int maxflooding = Weather.STORM;

	public Map(BattleMap map) {
		this.map = map;
	}

	public Map(int width, int heigth) {
		this(new BattleMap(width, heigth));
	}

	abstract public void generate();
}
