package javelin.model.feat.attack;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.model.feat.Feat;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

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
		for (AttackSequence attack : (ArrayList<AttackSequence>) c.source.melee
				.clone()) {
			if (attack.powerful) {
				c.source.melee.remove(attack);
			}
		}
	}

	@Override
	public void add(Combatant c) {
		update(c.source);
	}

	private AttackSequence createattack(AttackSequence attack, int target) {
		attack = attack.clone();
		for (Attack a : attack) {
			a.bonus -= target;
			a.damage[2] += target;
		}
		attack.powerful = true;
		return attack;
	}

	@Override
	public void update(Monster m) {
		int nattacks = m.melee.size();
		HashSet<Integer> targets = new HashSet<Integer>(2);
		int bab = m.getbaseattackbonus();
		if (nattacks == 1) {
			targets.add(Math.round(bab * 1 / 3f));
			targets.add(Math.round(bab * 2 / 3f));
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
	}

	@Override
	public String inform(Combatant c) {
		return "Current base attack bonus: +" + c.source.getbaseattackbonus();
	}

	@Override
	public boolean apply(Combatant c) {
		return c.source.strength >= 13 && c.source.getbaseattackbonus() >= 1
				&& super.apply(c);
	}
}
