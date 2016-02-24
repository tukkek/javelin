package javelin.model.item.potion.cure;

import javelin.model.item.Item;
import tyrant.mikera.engine.RPG;

/**
 * See the d20 SRD for more info.
 */
public class CureCriticalWounds extends CurePotion {

	public CureCriticalWounds() {
		super("critical", 1400, Item.GOOD);
	}

	@Override
			int rollhpcured() {
		int sum = 7;
		for (int i = 0; i < 4; i++) {
			sum += RPG.r(1, 8);
		}
		return sum;
	}

}
