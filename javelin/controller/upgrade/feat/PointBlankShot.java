package javelin.controller.upgrade.feat;

import javelin.model.unit.Combatant;

/**
 * @se {@link PointBlankShot}
 * @author alex
 */
public class PointBlankShot extends FeatUpgrade {
	/** Constructor. */
	public PointBlankShot() {
		super(javelin.model.feat.PointBlankShot.SINGLETON);
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean apply(Combatant m) {
		return !m.source.ranged.isEmpty() && super.apply(m);
	}
}
