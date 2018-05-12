package javelin.controller.table.dungeon;

import javelin.controller.table.Table;
import tyrant.mikera.engine.RPG;

public class LockedDoor extends Table {
	public LockedDoor() {
		if (RPG.chancein(2)) {
			add(true, 1);
			add(false, 3);
		} else {
			add(true, 3);
			add(false, 1);
		}
	}
}
