package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.door.ExcellentWoodenDoor;
import javelin.model.world.location.dungeon.feature.door.GoodWoodenDoor;
import javelin.model.world.location.dungeon.feature.door.IronDoor;
import javelin.model.world.location.dungeon.feature.door.StoneDoor;
import javelin.model.world.location.dungeon.feature.door.WoodenDoor;

/** Type of {@link Door}. */
public class DoorType extends Table{
  /** Constructor. */
  @SuppressWarnings("unused")
  public DoorType(DungeonFloor f){
    add(WoodenDoor.class,3);
    add(GoodWoodenDoor.class,2);
    add(ExcellentWoodenDoor.class,2);
    add(StoneDoor.class,1);
    add(IronDoor.class,1);
  }
}
