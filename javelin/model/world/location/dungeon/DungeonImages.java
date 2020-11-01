package javelin.model.world.location.dungeon;

import java.util.HashMap;

import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Crate;
import javelin.model.world.location.dungeon.feature.Crate.TieredCrates;
import javelin.model.world.location.dungeon.feature.Fountain;
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
	/** @see Crate */
	public static final String CRATE="crate";
	/** @see Fountain */
	public static final String FOUNTAIN="fountain";

	/** Constructor. */
	public DungeonImages(DungeonTier t){
		put(WALL,t.wall);
		put(FLOOR,t.floor);
		put(CRATE,RPG.pick(new TieredCrates(t)));
		put(CHEST,"chest"+RPG.r(1,25));
		put(FOUNTAIN,"fountain"+RPG.r(1,3));
	}
}