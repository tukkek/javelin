package javelin.controller.upgrade.feat;

import javelin.model.feat.PreciseShot;
import javelin.model.unit.Combatant;

/**
 * @see javelin.model.feat.ImprovedPreciseShot
 * @author alex
 */
public class ImprovedPreciseShot extends FeatUpgrade {
	/** Constructor. */
	public ImprovedPreciseShot() {
		super(javelin.model.feat.ImprovedPreciseShot.SINGLETON);
		prerequisite = PreciseShot.SINGLETON;
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean apply(Combatant m) {
		return m.source.dexterity >= 19 && m.source.getbaseattackbonus() >= 11
				&& super.apply(m);
	}
}
