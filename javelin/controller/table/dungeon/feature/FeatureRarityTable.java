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
	public FeatureRarityTable(DungeonFloor f){
		super();
		add(f.gettable(CommonFeatureTable.class),2);
		add(f.gettable(RareFeatureTable.class),1,4);
	}

	@Override
	public DungeonFeatureTable roll(){
		return (DungeonFeatureTable)super.roll();
	}
}
