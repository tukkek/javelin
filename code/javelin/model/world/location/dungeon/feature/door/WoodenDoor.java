package javelin.model.world.location.dungeon.feature.door;

import javelin.model.item.key.door.WoodenKey;
import javelin.model.world.location.dungeon.DungeonFloor;

public class WoodenDoor extends Door{
	public WoodenDoor(DungeonFloor f){
		this("doorwood",13,15,f);
	}

	public WoodenDoor(String avatar,int breakdcstuck,int breakdclocked,
			DungeonFloor f){
		super(avatar,breakdcstuck,breakdclocked,WoodenKey.class,f);
	}
}
