package javelin.controller.upgrade.feat;

import javelin.model.feat.attack.CombatExpertise;
import javelin.model.feat.attack.ImprovedGrapple;
import javelin.model.unit.Combatant;

/**
 * The correct prerequisite is Improved Unarmed Strike but it is not featured.
 * 
 * @see ImprovedGrapple
 * 
 * @author alex
 */
public class ImprovedGrappleUpgrade extends FeatUpgrade {
	/** Constructor. */
	public ImprovedGrappleUpgrade() {
		super(ImprovedGrapple.singleton);
		prerequisite = CombatExpertise.singleton;
	}

	@Override
	public String info(Combatant c) {
		return c.source.dexterity + " dexteriry";
	}

	@Override
	public boolean apply(Combatant c) {
		return c.source.dexterity >= 13 && super.apply(c);
	}
}
