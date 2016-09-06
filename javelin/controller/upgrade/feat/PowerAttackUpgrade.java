package javelin.controller.upgrade.feat;

import javelin.model.feat.attack.PowerAttack;
import javelin.model.unit.Combatant;

/**
 * @see PowerAttack
 * @author alex
 */
public class PowerAttackUpgrade extends FeatUpgrade {
	/** Constructor. */
	public PowerAttackUpgrade() {
		super(PowerAttack.SINGLETON);
	}

	@Override
	public String info(Combatant c) {
		return "Current base attack bonus: +" + c.source.getbaseattackbonus();
	}

	@Override
	public boolean apply(Combatant c) {
		return c.source.strength >= 13 && c.source.getbaseattackbonus() >= 1
				&& super.apply(c);
	}
}
