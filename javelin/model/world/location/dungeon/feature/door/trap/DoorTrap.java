package javelin.model.world.location.dungeon.feature.door.trap;

import java.io.Serializable;

import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.feature.door.Door;

public abstract class DoorTrap implements Serializable {
	abstract public void generate(Door d);

	public void activate(Combatant opening) {
		// nothing by default
	}
}
