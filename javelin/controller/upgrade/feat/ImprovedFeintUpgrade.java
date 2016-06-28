package javelin.controller.upgrade.feat;

import javelin.model.feat.CombatExpertise;
import javelin.model.feat.ImprovedFeint;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedFeint
 * @author alex
 *
 */
public class ImprovedFeintUpgrade extends FeatUpgrade {
	/** Constructor. */
	public ImprovedFeintUpgrade() {
		super(ImprovedFeint.singleton);
		prerequisite = CombatExpertise.singleton;
	}

	@Override
	public String info(Combatant c) {
		return "";
	}
}
