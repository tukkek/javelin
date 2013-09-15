package javelin.model.feat;

public class Toughness extends Feat {
	public static Toughness singleton;

	public Toughness() {
		super("toughness");
		Toughness.singleton = this;
	}

}
