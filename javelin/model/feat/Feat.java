package javelin.model.feat;

import java.io.Serializable;
import java.util.TreeMap;

public abstract class Feat implements Serializable {
	public final String name;
	transient static public TreeMap<String, Feat> all = new TreeMap<String, Feat>();

	static {
		new ExoticWeaponProficiency();
		new GreatFortitude();
		new ImprovedInitiative();
		new IronWill();
		new Toughness();
		new WeaponFinesse();
		new WeaponFocus();
		new Multiattack();
		new MultiweaponFighting();
		new LightningReflexes();
		new PointBlankShot();
		new PreciseShot();
		new ImprovedPreciseShot();
		new RapidShot();
	}

	public Feat(String namep) {
		name = namep.toLowerCase();
		all.put(name, this);
	}

	public boolean equals(final Feat obj) {
		return name.equals(obj.name);
	}
}
