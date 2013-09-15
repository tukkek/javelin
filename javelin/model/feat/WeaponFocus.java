package javelin.model.feat;

public class WeaponFocus extends Feat {

	static public WeaponFocus singleton;

	public WeaponFocus() {
		super("weapon focus");
		singleton = this;
	}

}
