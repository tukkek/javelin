package javelin.model.feat.attack.martial;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedTrip extends Feat {
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
}
