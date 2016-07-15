package javelin.controller.action.maneuver;

import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game.Delay;
import javelin.model.condition.Prone;
import javelin.model.feat.ImprovedTrip;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Trip extends Maneuver {

	public Trip() {
		super("Trip", "T", 'T', ImprovedTrip.singleton, +2);
	}

	@Override
	boolean validatetarget(Combatant target) {
		return target.hascondition(Prone.class) == null;
	}

	@Override
	ChanceNode miss(Combatant combatant, Combatant target,
			BattleState battleState, float chance) {
		return new ChanceNode(battleState, chance, "Trip attemp fails...",
				Delay.WAIT);
	}

	@Override
	ChanceNode hit(Combatant current, Combatant target, BattleState s,
			float chance) {
		s = s.clone();
		current = s.clone(current);
		target = s.clone(target);
		target.addcondition(new Prone(target.ap + .1f, target));
		return new ChanceNode(s, chance, target + " is prone!", Delay.BLOCK);
	}

	@Override
	int getsavebonus(Combatant targetCombatant) {
		return targetCombatant.source.ref;
	}

	@Override
	int getattackerbonus(Combatant combatant) {
		return Monster.getbonus(combatant.source.dexterity);
	}

}
