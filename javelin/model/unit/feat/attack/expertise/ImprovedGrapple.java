package javelin.model.unit.feat.attack.expertise;

import javelin.controller.action.maneuver.ExpertiseAction;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.abilities.discipline.expertise.GrappleManeuver;
import javelin.model.unit.feat.Feat;

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
	public boolean upgrade(Combatant c) {
		return c.source.dexterity >= 13 && super.upgrade(c);
	}

	@Override
	protected CombatExpertiseManeuver getmaneuver() {
		return new GrappleManeuver();
	}
}
