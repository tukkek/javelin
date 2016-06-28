package javelin.controller.upgrade.feat;

import javelin.model.feat.PointBlankShot;
import javelin.model.unit.Combatant;

/**
 * @see javelin.model.feat.PreciseShot
 * @author alex
 */
public class PreciseShot extends FeatUpgrade {
	/** Constructor. */
	public PreciseShot() {
		super(javelin.model.feat.PreciseShot.SINGLETON);
		prerequisite = PointBlankShot.SINGLETON;
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

}
