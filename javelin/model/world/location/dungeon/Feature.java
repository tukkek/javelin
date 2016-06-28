package javelin.model.world.location.dungeon;

import java.io.Serializable;

import javelin.model.BattleMap;
import javelin.view.Images;
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
	private String avatarfile;

	public Feature(String thing, int xp, int yp, String avatarfilep) {
		this.thing = thing;
		x = xp;
		y = yp;
		avatarfile = avatarfilep;
	}

	public void generate(BattleMap map) {
		visual = Lib.create(thing);
		visual.javelinimage = Images.getImage(avatarfile);
		addvisual(map);
	}

	protected void addvisual(BattleMap map) {
		map.addThing(visual, x, y);
	}

	/**
	 * TODO the if here is probably due to some wacky interface bug which should
	 * be solved by 2.0
	 */
	public void remove() {
		if (Dungeon.active != null) {
			visual.remove();
			Dungeon.active.features.remove(this);
		}
	}

	abstract public boolean activate();
}
