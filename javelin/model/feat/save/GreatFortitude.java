package javelin.model.feat.save;

import javelin.model.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class GreatFortitude extends Feat {
	public GreatFortitude() {
		super("great fortitude");
	}

	public static Feat singleton = new GreatFortitude();

}
