package javelin.model.feat.attack.shot;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class PreciseShot extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new PreciseShot();

	private PreciseShot() {
		super("Precise shot");
		prerequisite = javelin.model.feat.attack.shot.PointBlankShot.SINGLETON;
	}

	@Override
	public String inform(Combatant m) {
		return "";
	}
}
