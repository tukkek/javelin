package javelin.controller.upgrade.feat;

import javelin.model.feat.Cleave;
import javelin.model.feat.PowerAttack;
import javelin.model.unit.Combatant;

/**
 * @see Cleave
 * @author alex
 */
public class CleaveUpgrade extends FeatUpgrade {

	public CleaveUpgrade() {
		super(Cleave.SINGLETON);
		prerequisite = PowerAttack.SINGLETON;
	}

	@Override
	public String info(Combatant c) {
		return "";
	}
}
