package javelin.model.item.potion.cure;

import javelin.model.item.Item;
import tyrant.mikera.engine.RPG;

/**
 * See the d20 SRD for more info.
 */
public class CureModerateWounds extends CurePotion {

	public CureModerateWounds() {
		super("moderate", 300, Item.GOOD);
	}

	@Override
			int rollhpcured() {
		int sum = 3;
		for (int i = 0; i < 2; i++) {
			sum += RPG.r(1, 8);
		}
		return sum;
	}

}
