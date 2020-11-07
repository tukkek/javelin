package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;

public class StuckDoor extends Table{
	public StuckDoor(DungeonFloor f){
		add(true,1);
		add(false,9);
	}
}
