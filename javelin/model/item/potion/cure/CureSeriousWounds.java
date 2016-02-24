package javelin.model.item.potion.cure;

import javelin.model.item.Item;
import tyrant.mikera.engine.RPG;

/**
 * See the d20 SRD for more info.
 */
public class CureSeriousWounds extends CurePotion {

	public CureSeriousWounds() {
		super("serious", 750, Item.WATER);
	}

	@Override
			int rollhpcured() {
		int sum = 5;
		for (int i = 0; i < 3; i++) {
			sum += RPG.r(1, 8);
		}
		return sum;
	}

}
