package javelin.model.world.location.dungeon.feature.door;

import javelin.model.item.key.door.WoodenKey;

public class WoodenDoor extends Door{
	public WoodenDoor(){
		this("dungeondoorwood",13,15);
	}

	public WoodenDoor(String avatar,int breakdcstuck,int breakdclocked){
		super(avatar,breakdcstuck,breakdclocked,WoodenKey.class);
	}
}
