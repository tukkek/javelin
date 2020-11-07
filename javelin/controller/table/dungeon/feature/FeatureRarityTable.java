package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;

/**
 * A table that contains either the Dungeon's {@link RareFeatureTable} or
 * {@link CommonFeatureTable}..
 *
 * @author alex
 */
public class FeatureRarityTable extends Table{
	/** Constructor. */
	public FeatureRarityTable(){
		add(DungeonFloor.gettable(CommonFeatureTable.class),2);
		add(DungeonFloor.gettable(RareFeatureTable.class),1,4);
	}

	@Override
	public DungeonFeatureTable roll(){
		return (DungeonFeatureTable)super.roll();
	}
}
