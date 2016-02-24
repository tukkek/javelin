package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.model.condition.Prone;
import javelin.model.feat.ImprovedPreciseShot;
import javelin.model.feat.PreciseShot;
import javelin.model.state.BattleState;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

/**
 * needs to take into account range increments, each full range increment
 * imposes a cumulative -2 penalty on the attack roll. Actually doesn't sound
 * too important since most weapons used have really long ranges for our combats
 * 
 * also -4 if in melée with another enemy (many more details @
 * http://www.d20pfsrd.com/gamemastering/combat
 * 
 * @author alex
 * 
 */
public class RangedAttack extends AbstractAttack {
	static final public RangedAttack SINGLETON = new RangedAttack();

	private RangedAttack() {
		super("Ranged attack");
	}

	@Override
	protected boolean isMelee() {
		return false;
	}

	@Override
			List<AttackSequence> getattacks(final Combatant active) {
		return active.source.ranged;
	}

	@Override
			int getpenalty(final Combatant attacker, final Combatant target,
					final BattleState s) {
		return penalize(attacker, target, s);
	}

	static public int penalize(final Combatant attacker, final Combatant target,
			final BattleState s) {
		int penalty = target.surprise();
		if (!attacker.source.hasfeat(PreciseShot.SINGLETON)
				&& s.isEngaged(target)) {
			penalty += 4;
		}
		if (!attacker.source.hasfeat(ImprovedPreciseShot.SINGLETON)
				&& s.hasLineOfSight(attacker,
						target) == javelin.model.state.BattleState.Vision.COVERED) {
			penalty += 4;
		}
		if (target.hascondition(Prone.class)) {
			penalty += 2;
		}
		return penalty;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant active) {
		if (gameState.isEngaged(active)) {
			return Collections.EMPTY_LIST;
		}
		final ArrayList<List<ChanceNode>> successors =
				new ArrayList<List<ChanceNode>>();
		for (final Combatant target : gameState.getTargets(active)) {
			for (final Integer attack : getcurrentattack(active)) {
				final BattleState newstate = gameState.clone();
				final Combatant newactive = newstate.clone(active);
				newactive.currentranged.setcurrent(attack,
						newactive.source.ranged);
				successors.add(attack(newstate, newactive, target,
						newactive.currentranged, 0));
			}
		}
		return successors;
	}

	@Override
	public boolean cleave() {
		return false;
	}
}