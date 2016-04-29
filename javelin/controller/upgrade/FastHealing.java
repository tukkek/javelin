package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class FastHealing extends Upgrade {
	public FastHealing() {
		super("Fast healing");
	}

	@Override
	public String info(final Combatant m) {
		return "Current: " + m.source.fasthealing + " ("
				+ Math.round(100 * m.source.fasthealing / m.maxhp) + "%)";
	}

	@Override
	public boolean apply(final Combatant m) {
		int heal = m.source.hd.count();
		if (m.source.fasthealing >= heal) {
			// design parameter (fast healing + regeneration)
			return false;
		}
		m.source.fasthealing = heal;
		return true;
	}

	@Override
	public boolean isstackable() {
		return true;
	}

}
