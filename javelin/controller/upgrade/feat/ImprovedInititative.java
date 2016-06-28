package javelin.controller.upgrade.feat;

import javelin.model.feat.ImprovedInitiative;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedInitiative
 * @author alex
 */
public class ImprovedInititative extends FeatUpgrade {
	/** Constructor. */
	public ImprovedInititative() {
		super("Improved initiative", ImprovedInitiative.singleton);
	}

	@Override
	public String info(final Combatant m) {
		return "Current initiative: " + m.source.initiative;
	}

	@Override
	public boolean apply(final Combatant m) {
		if (super.apply(m)) {
			m.source.initiative += 4;
			return true;
		}
		return false;
	}

}