package javelin.controller.upgrade.feat;

import javelin.model.feat.PreciseShot;
import javelin.model.unit.Combatant;

public class ImprovedPreciseShot extends FeatUpgrade {
	public ImprovedPreciseShot() {
		super(javelin.model.feat.ImprovedPreciseShot.SINGLETON);
		prerequisite = PreciseShot.SINGLETON;
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean isstackable() {
		return false;
	}

	@Override
	public boolean apply(Combatant m) {
		return m.source.dexterity >= 19 && m.source.getbaseattackbonus() >= 11
				&& super.apply(m);
	}
}
