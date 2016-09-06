package javelin.controller.upgrade.feat;

import javelin.model.feat.attack.Cleave;
import javelin.model.feat.attack.PowerAttack;
import javelin.model.unit.Combatant;

/**
 * @see Cleave
 * @author alex
 */
public class CleaveUpgrade extends FeatUpgrade {

	/** Constructor. */
	public CleaveUpgrade() {
		super(Cleave.SINGLETON);
		prerequisite = PowerAttack.SINGLETON;
	}

	@Override
	public String info(Combatant c) {
		return "";
	}
}
