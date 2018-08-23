package javelin.model.world.location.dungeon.feature;

import java.io.Serializable;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Perception;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.Images;
import javelin.view.mappanel.dungeon.DungeonTile;

/**
 * An interactive dungeon tile.
 *
 * @author alex
 */
public abstract class Feature implements Serializable {
    /** X coordinate. */
    public int x = -1;
    /** Y coordinate. */
    public int y = -1;
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
     * @param xp          {@link #x}
     * @param yp          {@link #y}
     * @param avatarfilep File name for {@link Images#get(String)}.
     */
    public Feature(int xp, int yp, String avatarfilep) {
	this(avatarfilep);
	x = xp;
	y = yp;
    }

    public Feature(String avatarfilep) {
	avatarfile = avatarfilep;
    }

    /**
     * TODO evolve on 2.0+
     *
     * @param map Adds itself to this map.
     */
    public void generate() {
    }

    /**
     * Called when a {@link Squad} reaches this location.
     *
     * @return <code>true</code> if the Squad activates this feature or
     *         <code>false</code> if it is ignored.
     */
    abstract public boolean activate();

    /**
     * Called when the {@link Squad} is passing nearby this feature. If it's hidden,
     * might have a chance of revealing it.
     *
     * @param searching  The unit that is actively looking around (usually the one
     *                   with highest perception).
     * @param searchroll A {@link Perception} roll (usually a take-10, to prevent
     *                   scumming). If you want to have an automatic success, inform
     *                   a high number like 9000, because if you use
     *                   {@link Integer#MAX_VALUE}, it may overflow after internal
     *                   bonuses being applied.
     *
     * @see #draw
     */
    public void discover(Combatant searching, int searchroll) {
	// nothing by default
    }

    public void place(Dungeon d) {
	// nothing by default
    }

    public void remove() {
	Dungeon.active.features.remove(this);
    }
}
