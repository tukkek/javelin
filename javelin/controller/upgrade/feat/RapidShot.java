package javelin.controller.upgrade.feat;

import javelin.model.feat.PreciseShot;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

public class RapidShot extends FeatUpgrade {

	public RapidShot() {
		super(javelin.model.feat.RapidShot.SINGLETON);
		prerequisite = PreciseShot.SINGLETON;
	}

	@Override
	public String info(Combatant m) {
		return "";
	}

	@Override
	public boolean isstackable() {
		return false;
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.dexterity >= 13 && super.apply(m)) {
			for (AttackSequence sequence : (Iterable<AttackSequence>) m.source.ranged
					.clone()) {
				AttackSequence rapid = (AttackSequence) sequence.clone();
				rapid.add(0, rapid.get(0).clone());
				for (Attack a : rapid) {
					a.bonus -= 2;
				}
				m.source.ranged.add(rapid);
			}
			return true;
		}
		return false;
	}
}
