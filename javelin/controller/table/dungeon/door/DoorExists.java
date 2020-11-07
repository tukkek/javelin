package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;

public class DoorExists extends Table{
	public DoorExists(DungeonFloor f){
		add(true,1);
		add(false,3);
	}
}
