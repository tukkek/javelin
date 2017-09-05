package javelin.model.feat.attack.expertise;

import javelin.controller.action.maneuver.ExpertiseAction;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.discipline.expertise.GrappleManeuver;

/**
 * This and other special combat {@link ExpertiseAction}s, instead of being an
 * option by default like in traditional D20 are exclusive to those that have
 * the appropriate feat. This is done because offering 4 new actions for every
 * single move in the game would slow the AI significantly, especially on deeper
 * thinking levels.
 * 
 * @see ImprovedFeint
 * @see ImprovedTrip
 * @see CombatExpertise
 * @author alex
 */
public class ImprovedGrapple extends ExpertiseFeat {
	/** Unique instance of this {@link Feat}. */
	public static final ImprovedGrapple SINGLETON = new ImprovedGrapple();

	/** Constructor. */
	private ImprovedGrapple() {
		super("Improved grapple");
		prerequisite = CombatExpertise.SINGLETON;
	}

	@Override
	public String inform(Combatant c) {
		return c.source.dexterity + " dexteriry";
	}

	@Override
	public boolean apply(Combatant c) {
		return c.source.dexterity >= 13 && super.apply(c);
	}

	@Override
	protected CombatExpertiseManeuver getmaneuver() {
		return new GrappleManeuver();
	}
}
