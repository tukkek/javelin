package javelin.model.feat.attack.martial;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedFeint extends Feat {
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

}
