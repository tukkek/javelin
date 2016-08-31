package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.model.condition.Prone;
import javelin.model.state.BattleState;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;

public class MeleeAttack extends AbstractAttack {
	static final public MeleeAttack SINGLETON = new MeleeAttack();

	private MeleeAttack() {
		super("Melee attack");
	}

	@Override
	protected boolean isMelee() {
		return true;
	}

	@Override
	List<AttackSequence> getattacks(final Combatant active) {
		return active.source.melee;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final BattleState gameState,
			final Combatant active) {
		final ArrayList<List<ChanceNode>> successors =
				new ArrayList<List<ChanceNode>>();
		for (final Combatant target : gameState.getsurroundings(active)) {
			for (final Integer attack : getcurrentattack(active)) {
				if (!target.isAlly(active, gameState)) {
					final BattleState newstate = gameState.clone();
					final Combatant newactive = newstate.clone(active);
					newactive.currentmelee.setcurrent(attack,
							newactive.source.melee);
					successors.add(attack(newstate, newactive, target,
							newactive.currentmelee, 0));
				}
			}
		}
		return successors;
	}

	@Override
	public boolean cleave() {
		return true;
	}

	@Override
	public int getpenalty(Combatant attacker, Combatant target, BattleState s) {
		int penalty = super.getpenalty(attacker, target, s);
		if (s.isflanked(target, attacker)) {
			penalty -= 2;
		}
		if (target.hascondition(Prone.class) != null) {
			penalty -= 2;
		}
		return penalty;
	}
}