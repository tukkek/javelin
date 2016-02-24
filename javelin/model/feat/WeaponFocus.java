package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class WeaponFocus extends Feat {

	static public WeaponFocus singleton;

	public WeaponFocus() {
		super("weapon focus");
		singleton = this;
	}

}
