package javelin.model.dungeon;

import java.io.Serializable;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

/**
 * An interactive dungeon tile.
 * 
 * @author alex
 */
public abstract class Feature implements Serializable {
	final public int x, y;
	transient public Thing visual = null;
	private final String thing;

	public Feature(String thing, int xp, int yp) {
		this.thing = thing;
		x = xp;
		y = yp;
	}

	public void generate(BattleMap map) {
		visual = map.addThing(Lib.create(thing), x, y);

	}

	abstract public void activate();
}
