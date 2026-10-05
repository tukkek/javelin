package javelin.model.item.key.door;

import java.util.List;

import javelin.model.item.Item;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.door.Door;

/**
 * Keys open {@link Door}s. Like in Resident Evil games, a single key can open
 * all the doors of the given type - reducing item management and making finding
 * it more impactful.
 *
 * Keys are limited to a single {@link DungeonFloor}.
 *
 * @see DungeonZoner
 * @author alex
 */
public class Key extends Item{
  DungeonFloor floor;

  /** Constructor. */
  public Key(String type,DungeonFloor d){
    super(type+" key",0,false);
    usedinbattle=false;
    usedoutofbattle=false;
    floor=d;
  }

  @Override
  public String toString(){
    var d=floor.dungeon.toString();
    return String.join(", ",List.of(name,d,"floor "+floor.getdepth()));
  }

  /** @return <code>true</code> if same {@link #floor}. */
  public boolean open(DungeonFloor f){
    return f==floor;
  }
}
