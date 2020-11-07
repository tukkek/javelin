package javelin.model.unit.skill;

import javelin.model.item.key.door.Key;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.trap.Trap;

/**
 * Can be used in {@link DungeonFloor}s to disarm {@link Trap}s and also to unlock
 * {@link Door}s. Doors are guaranteed to have a {@link Key} in the same floor,
 * so this is more of a sequence-break type of benefit, especially because
 * Dungeon zones are defined by the Doors in the map and as such are more likely
 * to have {@link Feature}s found behind them.
 *
 * @see DungeonZoner
 * @author alex
 */
public class DisableDevice extends Skill{
	/** Constructor. */
	public DisableDevice(){
		super("Disable device",Ability.DEXTERITY);
		intelligent=true;
	}
}
