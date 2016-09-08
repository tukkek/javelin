package javelin.model.feat.attack.martial;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * This is offering the action "Attack defensively", with the appropriate
 * enhancements for the feat. Not otherwise available to prevent overloading the
 * AI with options.
 * 
 * @see ImprovedGrapple
 * @author alex
 */
public class CombatExpertise extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final CombatExpertise SINGLETON = new CombatExpertise();

	/** Constructor. */
	private CombatExpertise() {
		super("Combat expertise");
	}

	@Override
	public String inform(Combatant c) {
		return "Has " + c.source.melee.size()
				+ " mêlée attacks and intelligence " + c.source.intelligence;
	}

	@Override
	public boolean apply(Combatant c) {
		return c.source.intelligence >= 13 && !c.source.melee.isEmpty()
				&& super.apply(c);
	}
}
