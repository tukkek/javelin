package javelin.model.world.location.dungeon.feature.door.trap;

import javelin.model.world.location.dungeon.feature.door.Door;

public class ArcaneLock extends DoorTrap {
	public static final DoorTrap INSTANCE = new ArcaneLock();

	private ArcaneLock() {
		// prevent instantiation
	}

	@Override
	public void generate(Door d) {
		d.unlockdc += 10;
	}
}
