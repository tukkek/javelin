package javelin.controller.table.dungeon;

import javelin.controller.table.Table;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.model.world.location.dungeon.feature.inhabitant.Broker;
import javelin.model.world.location.dungeon.feature.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.inhabitant.Prisoner;
import javelin.model.world.location.dungeon.feature.inhabitant.Trader;

public class InhabitantTable extends Table{
	public InhabitantTable(){
		add(Broker.class,CommonFeatureTable.MAX);
		add(Prisoner.class,CommonFeatureTable.MAX);
		add(Leader.class,CommonFeatureTable.MAX);
		add(Trader.class,CommonFeatureTable.MAX);
	}
}
