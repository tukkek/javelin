package javelin.model.world.location.dungeon.feature.door;

import javelin.model.item.key.door.IronKey;
import javelin.model.world.location.dungeon.DungeonFloor;

public class IronDoor extends Door{
	public IronDoor(DungeonFloor f){
		super("dooriron",28,28,IronKey.class,f);
	}
}
