package javelin.model.world.location.dungeon.feature.door;

import javelin.model.item.key.door.StoneKey;
import javelin.model.world.location.dungeon.DungeonFloor;

public class StoneDoor extends Door{
	public StoneDoor(DungeonFloor f){
		super("doorstone",28,28,StoneKey.class,f);
	}
}
