package javelin.controller.upgrade.ability;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;

public class SpellImmunity extends Upgrade {

	public SpellImmunity() {
		super("Spell immunity");
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.sr == Integer.MAX_VALUE) {
			return false;
		}
		m.source.sr = Integer.MAX_VALUE;
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
