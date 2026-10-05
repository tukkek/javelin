package javelin.controller.table.dungeon;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;

public class RoomSizeTable extends Table{
	public RoomSizeTable(DungeonFloor f){
		for(int size=3;size<=7;size++)
			add(size,20);
	}
}
