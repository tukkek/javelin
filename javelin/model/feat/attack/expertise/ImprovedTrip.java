package javelin.model.feat.attack.expertise;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.discipline.expertise.TripManeuver;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedTrip extends ExpertiseFeat {
	/** Unique instance of this {@link Feat}. */
	public static final ImprovedTrip SINGLETON = new ImprovedTrip();

	/** Constructor. */
	private ImprovedTrip() {
		super("Improved trip");
		prerequisite = CombatExpertise.SINGLETON;
	}

	@Override
	public String inform(Combatant c) {
		return "";
	}

	@Override
	protected CombatExpertiseManeuver getmaneuver() {
		return new TripManeuver();
	}
}
