package javelin.model.feat.save;

import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class IronWill extends Feat {

	public IronWill() {
		super("iron will");
	}

	public static Feat singleton = new IronWill();

}
