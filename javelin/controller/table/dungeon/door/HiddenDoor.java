package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonTier;

public class HiddenDoor extends Table{
	public HiddenDoor(DungeonFloor f){
		if(f.dungeon.gettier()!=DungeonTier.CAVE) add(true,1);
		add(false,19);
	}
}
