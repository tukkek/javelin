package javelin.model.feat.attack;

import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 * 
 * @see Cleave
 */
public class GreatCleave extends Feat {
	public static final GreatCleave SINGLETON = new GreatCleave();

	public GreatCleave() {
		super("Great cleave");
	}
}
