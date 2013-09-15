package javelin.controller.upgrade.ability;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;

public class DamageReduction extends Upgrade {

	public DamageReduction() {
		super("Damage reduction");
	}

	@Override
	public String info(Combatant m) {
		return "Currently reducing " + m.source.dr + " points of damage";
	}

	@Override
	public boolean apply(Combatant m) {
		m.source.dr += 1;
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
