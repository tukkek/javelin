package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class GreatFortitude extends Feat {
	public GreatFortitude() {
		super("great fortitude");
	}

	public static Feat singleton = new GreatFortitude();

}
