package javelin.model.item.key.door;

import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.inhabitant.Prisoner;

/**
 * A master key will open any one {@link Door} but is consumed in the process.
 *
 * @see Prisoner
 * @author alex
 */
public class MasterKey extends Key{
	/** Constructor. */
	public MasterKey(DungeonFloor d){
		this();
	}

	/** Constructor. */
	public MasterKey(){
		super("Master key",null);
	}
}
