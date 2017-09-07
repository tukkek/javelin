package javelin.model.unit.feat.attack;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 * 
 * TODO the way it's working right now, an {@link Attack} that uses Weapon
 * Finesse can only be upgraded through {@link Monster#raisedexterity(int)}.
 * Ideally, it should choose between the best of either bonus.
 */
public class WeaponFinesse extends Feat {
	/** Unique instance of this {@link Feat}. */
	static public final WeaponFinesse SINGLETON = new WeaponFinesse();
	String weapon;

	/** Constructor. */
	private WeaponFinesse() {
		super("weapon finesse");
	}

	@Override
	public Feat generate(String name) {
		WeaponFinesse wf = new WeaponFinesse();
		String weapon = name.split("\\(")[1];
		weapon = weapon.split("\\)")[0];
		wf.weapon = weapon.toLowerCase();
		return wf;
	}

	public boolean affects(Attack a) {
		return a.name.toLowerCase().contains(weapon);
	}

	public List<Attack> getallaffected(Monster m) {
		ArrayList<Attack> affected = new ArrayList<Attack>();
		for (AttackSequence attacks : m.melee) {
			for (Attack a : attacks) {
				if (affects(a)) {
					affected.add(a);
				}
			}
		}
		return affected;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + weapon + ")";
	}
}
