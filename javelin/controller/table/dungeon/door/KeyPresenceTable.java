package javelin.controller.table.dungeon.door;

import javelin.controller.table.Table;
import javelin.model.item.key.door.Key;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.door.Door;

/**
 * Number of {@link Key}s in {@link Chest}s on a {@link Dungeon} level per
 * locked {@link Door}. From 0 to 2, with a big boost to 1 in
 * {@link DungeonTier#CAVE} levels to prevent early-game players from being
 * stuck.
 *
 * @author alex
 */
public class KeyPresenceTable extends Table{
	public KeyPresenceTable(){
		if(Dungeon.active.gettier()!=DungeonTier.CAVE) add(0,1);
		add(1,2);
		add(2,1);
	}
}
