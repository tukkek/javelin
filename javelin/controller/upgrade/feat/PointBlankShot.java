package javelin.controller.upgrade.feat;

import javelin.model.unit.Combatant;

public class PointBlankShot extends FeatUpgrade {
	public PointBlankShot() {
		super(javelin.model.feat.PointBlankShot.SINGLETON);
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
