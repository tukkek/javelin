package javelin.model.world.place.dungeon;

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
		visual = Lib.create(thing);
		addvisual(map);
	}

	protected void addvisual(BattleMap map) {
		map.addThing(visual, x, y);
	}

	public void remove() {
		if (Dungeon.active != null) {// TODO needs if??
			visual.remove();
			Dungeon.active.features.remove(this);
		}
	}

	abstract public boolean activate();
}
