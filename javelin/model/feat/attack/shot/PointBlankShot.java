package javelin.model.feat.attack.shot;

import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class PointBlankShot extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new PointBlankShot();

	private PointBlankShot() {
		super("Point blank shot");
	}

	@Override
	public String inform(Combatant m) {
		return "";
	}

	@Override
	public boolean apply(Combatant m) {
		return !m.source.ranged.isEmpty() && super.apply(m);
	}
}
