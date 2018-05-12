package javelin.controller.fight;

import javelin.controller.upgrade.Upgrade;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;

/**
 * {@link Temple} fights are different from normal {@link Dungeon} encounters
 * because the creatures are upgraded with {@link Upgrade}s from the respective
 * {@link Realm}.
 *
 * @author alex
 */
public class TempleEncounter extends RandomDungeonEncounter {
	Temple temple;

	/** Constructor. */
	public TempleEncounter(Temple temple, Dungeon d) {
		super(d);
		this.temple = temple;
	}

	@Override
	public boolean onend() {
		super.onend();
		Temple.leavingfight = true;
		return true;
	}
}
