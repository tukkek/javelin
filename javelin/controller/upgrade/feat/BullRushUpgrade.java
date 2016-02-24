package javelin.controller.upgrade.feat;

import javelin.model.feat.BullRush;
import javelin.model.feat.PowerAttack;
import javelin.model.unit.Combatant;

/**
 * @see BullRush
 * @author alex
 */
public class BullRushUpgrade extends FeatUpgrade {

	/**
	 * Need to rename because the official version is "Improved Bull Rush".
	 * 
	 * @see BullRush
	 */
	public BullRushUpgrade() {
		super(BullRush.SINGLETON);
		name = "Bull rush";
		prerequisite = PowerAttack.SINGLETON;
	}

	@Override
	public String info(Combatant c) {
		return "";
	}

}
