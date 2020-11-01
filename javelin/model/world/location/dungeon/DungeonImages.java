package javelin.model.world.location.dungeon;

import java.util.HashMap;

import javelin.model.world.location.dungeon.feature.Chest;
import javelin.old.RPG;

/**
 * Instance-specific {@link Dungeon} images.
 *
 * @see Dungeon#images
 */
public class DungeonImages extends HashMap<String,String>{
	/** Floor tile. */
	public static final String FLOOR="floor";
	/** Wall tile. */
	public static final String WALL="wall";
	/** @see Chest */
	public static final String CHEST="chest";

	/** Constructor. */
	public DungeonImages(DungeonTier tier){
		put(WALL,tier.wall);
		put(FLOOR,tier.floor);
		put(CHEST,"chest"+RPG.r(1,25));
	}
}