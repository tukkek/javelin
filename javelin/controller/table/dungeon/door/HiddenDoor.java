package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;

public class HiddenDoor extends Table{
	public HiddenDoor(){
		if(Dungeon.active.gettier()!=DungeonTier.CAVE) add(true,1);
		add(false,19);
	}
}
