package javelin.controller.action.maneuver;

import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game.Delay;
import javelin.model.condition.Grappling;
import javelin.model.feat.attack.ImprovedGrapple;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Grapple extends Maneuver {

	public Grapple() {
		super("Grapple", "G", 'G', ImprovedGrapple.singleton, +2);
	}

	@Override
	boolean validatetarget(Combatant target) {
		return target.hascondition(Grappling.class) == null;
	}

	@Override
	ChanceNode miss(Combatant combatant, Combatant target,
			BattleState battleState, float chance) {
		return new ChanceNode(battleState, chance, "Grapple attempt fails...",
				Delay.WAIT);
	}

	@Override
	ChanceNode hit(Combatant current, Combatant target, BattleState s,
			float chance) {
		s = s.clone();
		current = s.clone(current);
		target = s.clone(target);
		int duration = Math.round(
				1 / calculatesavechance(current, calculatesavebonus(target)));
		if (duration < 1) {
			duration = 1;
		}
		current.ap += duration;
		target.ap += duration;
		current.addcondition(new Grappling(current.ap + .1f, current));
		target.addcondition(new Grappling(target.ap + .1f, target));
		return new ChanceNode(s, chance, current + " and " + target
				+ " are grappling for " + duration + " turn(s)!", Delay.BLOCK);
	}

	@Override
	int getsavebonus(Combatant targetCombatant) {
		return targetCombatant.source.fort;
	}

	@Override
	int getattackerbonus(Combatant combatant) {
		return Monster.getbonus(combatant.source.strength);
	}
}
