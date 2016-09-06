package javelin.model.spell.conjuration.healing;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.Realm;
import javelin.model.condition.Neutralized;
import javelin.model.spell.Touch;
import javelin.model.spell.necromancy.Poison;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * http://www.d20srd.org/srd/spells/neutralizePoison.htm
 * 
 * @author alex
 */
public class NeutralizePoison extends Touch {
	/** Constructor. */
	public NeutralizePoison() {
		super("Neutralize poison", 4, SpellsFactor.ratespelllikeability(4),
				Realm.WATER);
		ispotion = true;
		isritual = true;
		castinbattle = true;
		castoutofbattle = true;
		castonallies = true;
		provokeaoo = false;
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		boolean engaged = s.isengaged(combatant);
		for (Combatant c : new ArrayList<Combatant>(targets)) {
			if (!Touch.isfar(combatant, c)) {
				final boolean ally = combatant.isally(c, s);
				final boolean poisonerenemy =
						!ally && (checkpoisoner(c, c.source.melee)
								|| checkpoisoner(c, c.source.ranged));
				if ((ally && !engaged) || poisonerenemy) {
					continue;
				}
			}
			targets.remove(c);
		}
	}

	boolean checkpoisoner(Combatant c, ArrayList<AttackSequence> attacks) {
		for (AttackSequence sequence : attacks) {
			for (Attack a : sequence) {
				if (a.effect.equals(Poison.instance)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		if (target.isally(caster, s)) {
			return castpeacefully(caster, target);
		}
		neutralize(target.source.melee);
		neutralize(target.source.ranged);
		return target + " is not poisonous anymore!";
	}

	void neutralize(ArrayList<AttackSequence> attacks) {
		for (AttackSequence sequence : attacks) {
			for (Attack a : sequence) {
				if (a.effect.equals(Poison.instance)) {
					a.effect = null;
				}
			}
		}
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.addcondition(new Neutralized(target, casterlevel));
		return target + " is immune to poison!";
	}
}
