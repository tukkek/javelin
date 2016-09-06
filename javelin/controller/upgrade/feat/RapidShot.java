package javelin.controller.upgrade.feat;

import javelin.model.feat.attack.PreciseShot;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see javelin.model.feat.attack.RapidShot
 * @author alex
 */
public class RapidShot extends FeatUpgrade {
	/** Constructor. */
	public RapidShot() {
		super(javelin.model.feat.attack.RapidShot.SINGLETON);
		prerequisite = PreciseShot.SINGLETON;
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.dexterity >= 13 && super.apply(m)) {
			update(m.source);
			return true;
		}
		return false;
	}

	static public void update(Monster m) {
		for (AttackSequence sequence : (Iterable<AttackSequence>) m.ranged
				.clone()) {
			AttackSequence rapid = sequence.clone();
			rapid.add(0, rapid.get(0).clone());
			for (Attack a : rapid) {
				a.bonus -= 2;
			}
			rapid.rapid = true;
			m.ranged.add(rapid);
		}
	}
}
