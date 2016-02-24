package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class ImprovedInitiative extends Feat {

	public static Feat singleton = null;

	public ImprovedInitiative() {
		super("improved initiative");
		singleton = this;
	}
}
