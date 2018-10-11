package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;

public class FeatureRarityTable extends Table{
	public FeatureRarityTable(){
		add(true,1);
		add(false,1,5);
	}
}
