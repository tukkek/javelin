package javelin.model.feat;

public class WeaponFinesse extends Feat {
	static public WeaponFinesse singleton;

	public WeaponFinesse() {
		super("weapon finesse");
		singleton = this;
	}

}
