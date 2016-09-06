package javelin.model.feat.attack;

import javelin.model.feat.Feat;

/**
 * This is offering the action "Attack defensively", with the appropriate
 * enhancements for the feat. Not otherwise available to prevent overloading the
 * AI with options.
 * 
 * @see ImprovedGrapple
 * @author alex
 */
public class CombatExpertise extends Feat {

	public static CombatExpertise singleton;

	public CombatExpertise() {
		super("Combat expertise");
		singleton = this;
	}

}
