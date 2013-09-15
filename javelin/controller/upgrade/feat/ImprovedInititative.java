package javelin.controller.upgrade.feat;

import javelin.model.feat.Feat;
import javelin.model.feat.ImprovedInitiative;
import javelin.model.unit.Combatant;

public class ImprovedInititative extends FeatUpgrade {
	public ImprovedInititative(final String name, final Feat featp) {
		super(name, featp);
	}

	@Override
	public String info(final Combatant m) {
		return "Current initiative: " + m.source.initiative;
	}

	@Override
	public boolean apply(final Combatant m) {
		if (m.source.hasfeat(ImprovedInitiative.singleton) > 0) {
			return false;
		}
		super.apply(m);
		m.source.initiative += 4;
		m.source.addfeat(ImprovedInitiative.singleton);
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}
}