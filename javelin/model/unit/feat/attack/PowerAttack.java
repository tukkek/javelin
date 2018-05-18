package javelin.model.unit.feat.attack;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 * 
 * @author alex
 */
public class PowerAttack extends Feat {
	/** Unique instance of this {@link Feat}. */
	public final static Feat SINGLETON = new PowerAttack();

	/** Constructor. */
	private PowerAttack() {
		super("Power attack");
		update = true;
	}

	@Override
	public void remove(Combatant c) {
		ArrayList<AttackSequence> melee = c.source.melee;
		for (AttackSequence attack : new ArrayList<AttackSequence>(melee)) {
			if (attack.powerful) {
				melee.remove(attack);
			}
		}
	}

	@Override
	public boolean add(Combatant c) {
		Monster m = c.source;
		int nattacks = m.melee.size();
		HashSet<Integer> targets = new HashSet<Integer>(2);
		int bab = m.getbab();
		if (nattacks == 1) {
			targets.add(Math.round(bab * 2 / 3f));
			targets.add(Math.round(bab * 1 / 3f));
		} else {
			targets.add(bab / 2);
		}
		targets.remove(new Integer(0));
		ArrayList<AttackSequence> original = m.melee;
		m.melee = new ArrayList<AttackSequence>(nattacks * targets.size());
		for (AttackSequence attack : original) {
			m.melee.add(attack);
			for (int target : targets) {
				m.melee.add(createattack(attack, target));
			}
		}
		return true;
	}

	private AttackSequence createattack(AttackSequence sequence, int target) {
		if (sequence.powerful) {
			throw new RuntimeException(
					"Cannot derivate from a Power Attack sequence!");
		}
		sequence = sequence.clone();
		for (Attack a : sequence) {
			a.bonus -= target;
			a.damage[2] += target;
		}
		sequence.powerful = true;
		return sequence;
	}

	@Override
	public String inform(Combatant c) {
		return "Current base attack bonus: +" + c.source.getbab();
	}

	@Override
	public boolean upgrade(Combatant c) {
		return c.source.strength >= 13 && c.source.getbab() >= 1
				&& super.upgrade(c);
	}
}
