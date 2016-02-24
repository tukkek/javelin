package javelin.model.item.potion.cure;

import javelin.model.item.Item;
import tyrant.mikera.engine.RPG;

/**
 * See the d20 SRD for more info.
 */
public class CureLightWounds extends CurePotion {

	public CureLightWounds() {
		super("light", 50, Item.WATER);
	}

	@Override
			int rollhpcured() {
		return RPG.r(1, 8) + 1;
	}

}
