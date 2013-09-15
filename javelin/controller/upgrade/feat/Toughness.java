package javelin.controller.upgrade.feat;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

public class Toughness extends FeatUpgrade {
	public Toughness(final String name, final Feat featp) {
		super(name, featp);
		stack = true;
	}

	@Override
	public String info(final Combatant m) {
		return "Max HP: " + m.maxhp;
	}

	@Override
	public boolean apply(final Combatant m) {
		super.apply(m);
		m.maxhp += 3;
		m.hp += 3;
		m.source.hd.extrahp += 3;
		return true;
	}
}