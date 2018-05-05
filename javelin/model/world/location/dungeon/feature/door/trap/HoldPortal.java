package javelin.model.world.location.dungeon.feature.door.trap;

import javelin.model.world.location.dungeon.feature.door.Door;

public class HoldPortal extends DoorTrap {
	public static final DoorTrap INSTANCE = new HoldPortal();

	private HoldPortal() {
		// prevent instantiation
	}

	@Override
	public void generate(Door d) {
		d.breakdc += 5;
	}
}
