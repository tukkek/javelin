package javelin.model.feat.attack;

import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class MultiweaponFighting extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new MultiweaponFighting();

	/** Constructor. */
	private MultiweaponFighting() {
		super("multiweapon fighting");
	}
}
