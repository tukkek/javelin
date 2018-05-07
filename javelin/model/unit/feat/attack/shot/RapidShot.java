package javelin.model.unit.feat.attack.shot;

import java.util.ArrayList;

import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
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
		update = true;
	}

	@Override
	public String inform(Combatant m) {
		return "";
	}

	@Override
	public boolean upgrade(Combatant c) {
		if (c.source.dexterity >= 13 && super.upgrade(c)) {
			add(c);
			return true;
		}
		return false;
	}

	@Override
	public boolean add(Combatant c) {
		for (AttackSequence sequence : (Iterable<AttackSequence>) c.source.ranged
				.clone()) {
			AttackSequence rapid = sequence.clone();
			rapid.add(0, rapid.get(0).clone());
			for (Attack a : rapid) {
				a.bonus -= 2;
			}
			rapid.rapid = true;
			c.source.ranged.add(rapid);
		}
		return true;
	}

	@Override
	public void remove(Combatant c) {
		ArrayList<AttackSequence> ranged = c.source.ranged;
		for (AttackSequence s : new ArrayList<AttackSequence>(ranged)) {
			if (s.rapid) {
				ranged.remove(s);
			}
		}
	}
}
