package javelin.model.feat.save;

import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class LightningReflexes extends Feat {

	public LightningReflexes() {
		super("lightning reflexes");
	}

	public static Feat singleton = new LightningReflexes();

}
