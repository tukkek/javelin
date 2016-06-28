package javelin.controller.upgrade.feat;

import javelin.model.feat.CombatExpertise;
import javelin.model.feat.ImprovedTrip;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedTrip
 * @author alex
 */
public class ImprovedTripUpgrade extends FeatUpgrade {
	/** Constructor. */
	public ImprovedTripUpgrade() {
		super(ImprovedTrip.singleton);
		prerequisite = CombatExpertise.singleton;
	}

	@Override
	public String info(Combatant c) {
		return "";
	}
}
