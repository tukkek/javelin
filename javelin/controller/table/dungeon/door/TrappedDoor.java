package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

public class TrappedDoor extends Table{
	public TrappedDoor(DungeonFloor f){
		super();
		add(true,1);
		add(false,RPG.chancein(2)?3:5);
	}
}
