package javelin.controller.action.ai;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
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
	public List<List<ChanceNode>> getSucessors(final BattleState gameState,
			final Combatant active) {
		final ArrayList<List<ChanceNode>> successors = new ArrayList<List<ChanceNode>>();
		for (final Combatant target : gameState.getSurroudings(active)) {
			for (final Integer attack : getcurrentattack(active)) {
				if (!target.isAlly(active, gameState)) {
					final BattleState newstate = gameState.clone();
					final Combatant newactive = newstate
							.translatecombatant(active);
					if (Javelin.DEBUG && attack == -1) {
						System.out.println("#buggycurrent");
					}
					newactive.currentmelee.setcurrent(attack,
							newactive.source.melee);
					successors.add(attack(newstate, newactive,
							gameState.translatecombatant(target),
							newactive.currentmelee, 0));
				}
			}
		}
		return successors;
	}
}