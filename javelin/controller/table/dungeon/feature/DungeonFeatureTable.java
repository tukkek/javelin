package javelin.controller.table.dungeon.feature;

import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;

/**
 * @see CommonFeatureTable
 * @see RareFeatureTable
 * @author alex
 */
public interface DungeonFeatureTable{
	/** Default number of chances per {@link Feature}. */
	int ROWS=10;

	/**
	 * Semantically identical to {@link CommonFeatureTable#rollfeature(DungeonFloor)}.
	 */
	Feature rollfeature(DungeonFloor d);
}