package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

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
		m.source.fasthealing += 1;
		return true;
	}

	@Override
	public boolean isstackable() {
		return true;
	}

}
