package javelin.controller.upgrade.feat;

import javelin.model.feat.attack.Cleave;
import javelin.model.feat.attack.GreatCleave;
import javelin.model.unit.Combatant;

/**
 * @see GreatCleave
 * @author alex
 */
public class GreatCleaveUpgrade extends FeatUpgrade {

	/** Constructor. */
	public GreatCleaveUpgrade() {
		super(GreatCleave.SINGLETON);
		prerequisite = Cleave.SINGLETON;
	}

	@Override
	public String info(Combatant c) {
		return "Base attack bonus: +" + c.source.getbaseattackbonus();
	}

	@Override
	public boolean apply(Combatant m) {
		return m.source.getbaseattackbonus() >= 4 && super.apply(m);
	}
}
