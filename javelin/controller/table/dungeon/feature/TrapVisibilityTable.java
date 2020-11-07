package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;

public class TrapVisibilityTable extends Table{
	public TrapVisibilityTable(DungeonFloor f){
		add(true,19);
		add(false,1);
	}
}
