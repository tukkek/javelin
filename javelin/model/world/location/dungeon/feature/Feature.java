package javelin.model.world.location.dungeon.feature;

import java.io.Serializable;

import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.Images;
import javelin.view.mappanel.dungeon.DungeonTile;
import javelin.view.mappanel.dungeon.DungeonWalker;

/**
 * An interactive dungeon tile.
 *
 * @author alex
 */
public abstract class Feature implements Serializable {
	/** X coordinate. */
	public int x;
	/** Y coordinate. */
	public int y;
	public String avatarfile;
	/**
	 * If <code>true</code> will {@link #remove()} this if {@link #activate()}
	 * return <code>true</code>.
	 */
	public boolean remove = true;
	/** <code>false</code> if this should be hidden from the player. */
	public boolean draw = true;
	/**
	 * If <code>true</code> lets the {@link Squad} stay in the same
	 * {@link DungeonTile} as a feature.
	 */
	public boolean enter = true;
	/**
	 * If <code>true</code>, once {@link #activate()} is called will not allow a
	 * movement sequence to carry through.
	 *
	 * @see DungeonWalker
	 */
	public boolean stop = false;

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
	public Feature(int xp, int yp, String avatarfilep) {
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
	public void generate() {
	}

	/**
	 * TODO the if here is probably due to some wacky interface bug which should
	 * be solved by 2.0
	 */
	public void remove() {
		if (Dungeon.active != null) {
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
