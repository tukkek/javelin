package javelin.controller.table.dungeon;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.feature.inhabitant.Broker;
import javelin.model.world.location.dungeon.feature.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.inhabitant.Prisoner;

public class InhabitantTable extends Table {
	public InhabitantTable() {
		add(Broker.class, CommonFeatureTable.MAX);
		add(Prisoner.class, CommonFeatureTable.MAX);
		add(Leader.class, CommonFeatureTable.MAX);
	}
}
