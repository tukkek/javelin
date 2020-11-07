package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

public class LockedDoor extends Table{
	public LockedDoor(DungeonFloor f){
		super();
		if(RPG.chancein(2)){
			add(true,1);
			add(false,3);
		}else{
			add(true,3);
			add(false,1);
		}
	}
}
