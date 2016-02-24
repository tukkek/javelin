package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class EnergyResistance extends Upgrade {

	public EnergyResistance() {
		super("Energy resistance");
	}

	@Override
	public String info(Combatant m) {
		return "Currently resists " + m.source.resistance
				+ " points of energy damage";
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.resistance == Integer.MAX_VALUE) {
			return false;
		}
		m.source.resistance += 1;
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}
}
