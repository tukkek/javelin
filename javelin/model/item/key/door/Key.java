package javelin.model.item.key.door;

import java.util.ArrayList;

import javelin.model.item.Item;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;
import tyrant.mikera.engine.RPG;

public class Key extends Item {
	public Key(String name, int price) {
		super(name, price, null);
		usedinbattle = false;
		usedoutofbattle = false;
	}

	public static Key generate() {
		if (RPG.chancein(10)) {
			return new MasterKey();
		}
		ArrayList<Door> doors = new ArrayList<Door>();
		for (Feature f : Dungeon.active.features) {
			if (f instanceof Door) {
				doors.add((Door) f);
			}
		}
		if (doors.isEmpty()) {
			return new WoodenKey();
		}
		try {
			return RPG.pick(doors).key.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}