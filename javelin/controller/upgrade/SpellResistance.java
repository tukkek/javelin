package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
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
		// design parameter
		return m.source.sr <= m.source.hd.count() + 12;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
