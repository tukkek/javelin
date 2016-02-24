package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
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
		m.source.dr += 5;
		// design parameter
		return m.source.dr <= 5
				+ Math.round(Math.floor(m.source.hd.count() / 2f));
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
