package javelin.model.feat.attack.expertise;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.discipline.expertise.FeintManeuver;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedFeint extends ExpertiseFeat {
	/** Unique instance of this {@link Feat}. */
	public static final ImprovedFeint SINGLETON = new ImprovedFeint();

	/** Constructor. */
	private ImprovedFeint() {
		super("Improved feint");
		prerequisite = CombatExpertise.SINGLETON;
	}

	@Override
	public String inform(Combatant c) {
		return "";
	}

	@Override
	protected CombatExpertiseManeuver getmaneuver() {
		return new FeintManeuver();
	}
}
