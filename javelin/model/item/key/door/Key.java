package javelin.model.item.key.door;

import javelin.model.item.Item;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.door.IronDoor;

/**
 * Keys open {@link DungeonFloor} {@link Door}s. Like in Resident Evil games, a
 * single key can open all the doors of the given type - reducing item
 * management and making a bigger deal out of finding an individual key.
 *
 * The particular Key subtype and {@link #dungeon} define what doors it can
 * open. For example, an {@link IronKey} found at level 1 of the
 * {@link Megadungeon} will open any {@link IronDoor}s in that particular level.
 *
 * @see MasterKey
 * @see DungeonZoner
 * @author alex
 */
public class Key extends Item{
	/** Particular dungeon floor this key is tied to. */
	public DungeonFloor dungeon;

	/** Constructor. */
	public Key(String name,DungeonFloor d){
		super(name,0,false);
		usedinbattle=false;
		usedoutofbattle=false;
		dungeon=d;
	}

	@Override
	public String toString(){
		var s=super.toString();
		if(dungeon!=null) s+=", "+dungeon;
		return s;
	}
}