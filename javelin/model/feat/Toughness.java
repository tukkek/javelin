package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class Toughness extends Feat {
	public static Toughness singleton;

	public Toughness() {
		super("toughness");
		Toughness.singleton = this;
	}

}
