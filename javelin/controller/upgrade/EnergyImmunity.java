package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class EnergyImmunity extends Upgrade {

	public EnergyImmunity() {
		super("Energy immunity");
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.resistance == Integer.MAX_VALUE) {
			return false;
		}
		m.source.resistance = Integer.MAX_VALUE;
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
