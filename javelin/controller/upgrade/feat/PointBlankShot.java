package javelin.controller.upgrade.feat;

import javelin.model.unit.Combatant;

/**
 * @se {@link PointBlankShot}
 * @author alex
 */
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

	@Override
	public boolean apply(Combatant m) {
		return !m.source.ranged.isEmpty() && super.apply(m);
	}
}
