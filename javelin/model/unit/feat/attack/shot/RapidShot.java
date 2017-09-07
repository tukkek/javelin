package javelin.model.unit.feat.attack.shot;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class RapidShot extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new RapidShot();

	/** Constructor. */
	private RapidShot() {
		super("Rapid shot");
		prerequisite = javelin.model.unit.feat.attack.shot.PreciseShot.SINGLETON;
	}

	@Override
	public String inform(Combatant m) {
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

	@Override
	public void update(Monster m) {
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
