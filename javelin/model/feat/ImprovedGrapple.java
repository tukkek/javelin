package javelin.model.feat;

import javelin.controller.action.maneuver.Maneuver;

/**
 * This and other special combat {@link Maneuver}s, instead of being an option
 * by default like in traditional D20 are exclusive to those that have the
 * appropriate feat. This is done because offering 4 new actions for every
 * single move in the game would slow the AI significantly, especially on deeper
 * thinking levels.
 * 
 * @see ImprovedFeint
 * @see ImprovedTrip
 * @see CombatExpertise
 * @author alex
 */
public class ImprovedGrapple extends Feat {
	public static ImprovedGrapple singleton;

	public ImprovedGrapple() {
		super("Improved grapple");
		ImprovedGrapple.singleton = this;
	}

}
