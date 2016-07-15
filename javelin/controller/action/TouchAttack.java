package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.old.Game.Delay;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.Thing;

/**
 * An attack that reaches out only 5 feet, like from the Digester or Shocker
 * Lizard.
 * 
 * @author alex
 */
public class TouchAttack extends Fire implements AiAction {

	public TouchAttack() {
		super("Touch attack", "t", 't');
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(BattleState gameState,
			Combatant combatant) {
		List<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>();
		if (combatant.source.touch == null) {
			return outcomes;
		}
		ArrayList<Combatant> opponents = gameState.getCombatants();
		filtertargets(combatant, opponents, gameState);
		for (Combatant target : opponents) {
			outcomes.add(attack(combatant, target, gameState));
		}
		return outcomes;
	}

	private List<ChanceNode> attack(Combatant active, final Combatant target,
			BattleState gameState) {
		gameState = gameState.clone();
		active = gameState.clone(active);
		active.ap += .5;
		javelin.model.unit.abilities.TouchAttack attack = active.source.touch;
		int damage = attack.damage[0] * attack.damage[1] / 2;
		List<ChanceNode> nodes = new ArrayList<ChanceNode>();
		String action =
				active + " uses " + attack.toString().toLowerCase() + "!\n";
		float savechance = CastSpell
				.convertsavedctochance(attack.savedc - active.source.ref);
		nodes.add(registerdamage(gameState, action + target + " resists, is ",
				savechance, target, damage / 2, active));
		nodes.add(registerdamage(gameState, action + target + " is ",
				1 - savechance, target, damage, active));
		return nodes;
	}

	ChanceNode registerdamage(BattleState gameState, String action,
			float chance, Combatant target, int damage, Combatant active) {
		gameState = gameState.clone();
		target = gameState.clone(target);
		target.damage(damage, gameState, target.source.energyresistance);
		return new ChanceNode(gameState, chance,
				action + target.getStatus() + ".", Delay.BLOCK);
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, BattleMap map) {
		Action.outcome(attack(combatant, targetCombatant, battleState));
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		ArrayList<Combatant> opponents =
				s.blueTeam.contains(combatant) ? s.redTeam : s.blueTeam;
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			if (!opponents.contains(target)
					|| Math.abs(target.location[0] - combatant.location[0]) > 1
					|| Math.abs(
							target.location[1] - combatant.location[1]) > 1) {
				targets.remove(target);
			}
		}
	}

	@Override
	protected void checkhero(Thing hero) {
		if (hero.combatant.source.touch == null) {
			throw new RepeatTurn();
		}
	}

	@Override
	protected boolean checkengaged(BattleState state, Combatant c) {
		return false;// engaged is fine
	}

	@Override
	protected int calculatehitdc(Combatant target, Combatant active,
			BattleState state) {
		/* should be ignored */
		return 1;
	}
}
