package javelin.model.feat;

public class ImprovedInitiative extends Feat {

	public static Feat singleton = null;

	public ImprovedInitiative() {
		super("improved initiative");
		singleton = this;
	}
}
