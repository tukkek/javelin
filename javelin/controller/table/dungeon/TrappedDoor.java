package javelin.controller.table.dungeon;

import javelin.controller.table.Table;
import tyrant.mikera.engine.RPG;

public class TrappedDoor extends Table {
	public TrappedDoor() {
		add(true, 1);
		add(false, RPG.chancein(2) ? 3 : 5);
	}
}
