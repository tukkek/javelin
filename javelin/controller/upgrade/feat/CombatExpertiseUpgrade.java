package javelin.controller.upgrade.feat;

import javelin.model.feat.attack.CombatExpertise;
import javelin.model.unit.Combatant;

/**
 * @see CombatExpertise
 * 
 * @author alex
 */
public class CombatExpertiseUpgrade extends FeatUpgrade {

	/** Constructor. */
	public CombatExpertiseUpgrade() {
		super(CombatExpertise.singleton);
	}

	@Override
	public String info(Combatant c) {
		return "Has " + c.source.melee.size()
				+ " mêlée attacks and intelligence " + c.source.intelligence;
	}

	@Override
	public boolean apply(Combatant c) {
		return c.source.intelligence >= 13 && !c.source.melee.isEmpty()
				&& super.apply(c);
	}
}
