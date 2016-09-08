package javelin.model.feat.attack;

import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class WeaponFinesse extends Feat {
	/** Unique instance of this {@link Feat}. */
	static public final WeaponFinesse SINGLETON = new WeaponFinesse();

	/** Constructor. */
	private WeaponFinesse() {
		super("weapon finesse");
	}
}
