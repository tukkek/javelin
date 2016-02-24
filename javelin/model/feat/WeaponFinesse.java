package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class WeaponFinesse extends Feat {
	static public WeaponFinesse singleton;

	public WeaponFinesse() {
		super("weapon finesse");
		singleton = this;
	}

}
