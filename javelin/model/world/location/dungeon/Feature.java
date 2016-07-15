package javelin.model.world.location.dungeon;

import java.io.Serializable;

import javelin.model.BattleMap;
import javelin.model.unit.Squad;
import javelin.view.Images;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

/**
 * An interactive dungeon tile.
 * 
 * @author alex
 */
public abstract class Feature implements Serializable {
	/** X coordinate. */
	final public int x;
	/** Y coordinate. */
	final public int y;
	/** TODO remove from 2.0 */
	transient public Thing visual = null;
	private final String thing;
	private String avatarfile;
	/**
	 * If <code>true</code> will {@link #remove()} this if {@link #activate()}
	 * return <code>true</code>.
	 */
	public boolean remove = true;

	/**
	 * @param thing
	 *            TODO remove
	 * @param xp
	 *            {@link #x}
	 * @param yp
	 *            {@link #y}
	 * @param avatarfilep
	 *            File name for {@link Images#getImage(String)}.
	 */
	public Feature(String thing, int xp, int yp, String avatarfilep) {
		this.thing = thing;
		x = xp;
		y = yp;
		avatarfile = avatarfilep;
	}

	/**
	 * TODO evolve on 2.0+
	 * 
	 * @param map
	 *            Adds itself to this map.
	 */
	public void generate(BattleMap map) {
		visual = Lib.create(thing);
		visual.javelinimage = Images.getImage(avatarfile);
		addvisual(map);
	}

	/**
	 * @param map
	 *            See {@link BattleMap#addThing(Thing, int, int)}.
	 */
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

	/**
	 * Called when a {@link Squad} reaches this location.
	 * 
	 * @return <code>true</code> if the Squad activates this feature or
	 *         <code>false</code> if it is ignored.
	 */
	abstract public boolean activate();
}
