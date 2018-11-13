package javelin.controller.table.dungeon.feature;

import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;

public interface DungeonFeatureTable{

	/**
	 * Semantically identical to {@link CommonFeatureTable#rollfeature(Dungeon)}.
	 */
	Feature rollfeature(Dungeon d);

}