package javelin.controller.upgrade.ability;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;

public class SpellResistance extends Upgrade {
	public SpellResistance() {
		super("Spell resistance");
	}

	@Override
	public String info(Combatant m) {
		return "Current spell resistance is " + m.source.sr;
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.sr == Integer.MAX_VALUE) {
			return false;
		}
		m.source.sr += 1;
		if (m.source.sr < 11) {
			m.source.sr = 11;
		}
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
