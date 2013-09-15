package javelin.controller.action;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.ai.ChanceNode;
import javelin.controller.walker.ChargePath;
import javelin.controller.walker.Walker;
import javelin.controller.walker.Walker.Step;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * TODO still needs to add prerequisites such as 10feet minimum and 2x speed
 * maximum. Plus computer part.
 * 
 */
public class Charge extends Fire {
	public Charge(String name, String key) {
		super(name, key, 'c');
		limiter = "range";
	}

	@Override
	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState s, BattleMap map) {
		Action.outcome(charge(map.getState(), combatant, targetCombatant)
				.get(0));
	}

	private ArrayList<List<ChanceNode>> charge(BattleState state, Combatant me,
			Combatant target) {
		state = state.clone();
		me = state.translatecombatant(me);
		target = state.translatecombatant(target);
		final ArrayList<Step> walk = walk(me, target, state);
		final Step destination;
		try {
			destination = walk.get(walk.size() - 1);
		} catch (NullPointerException e) {
			// TODO
			throw e;
		}
		me.location[0] = destination.x;
		me.location[1] = destination.y;
		me.charge();
		final ArrayList<List<ChanceNode>> allattacks = new ArrayList<List<ChanceNode>>();
		List<ChanceNode> move = MeleeAttack.SINGLETON.attack(state, me, target,
				usedefaultattack(me), 2, 0, 1);
		for (ChanceNode node : move) {
			node.action = me + " charges!\n" + node.action;
			node.delay = Delay.BLOCK;
		}
		allattacks.add(move);
		return allattacks;
	}

	public Attack usedefaultattack(Combatant me) {
		return me.source.melee.get(0).get(0);
	}

	@Override
	int calculatehitchance(Combatant target, Combatant active, BattleState state) {
		return target.ac() - (2 + usedefaultattack(active).bonus);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			final ArrayList<Step> steps = walk(combatant, target, s);
			if (steps == null) {
				targets.remove(target);
				continue;
			}
			final double distance = steps.size();
			if (distance < 2
					|| distance > 2 * combatant.source.gettopspeed() / 5
					|| distance > combatant.view(s.period)) {
				targets.remove(target);
			}
		}
	}

	ArrayList<Step> walk(final Combatant me, Combatant target,
			final BattleState state) {
		final Walker walk = new ChargePath(new Point(me.location[0],
				me.location[1]), new Point(target.location[0],
				target.location[1]), state, me.source.swim());
		walk.walk();
		if (walk.solution == null) {
			return null;
		}
		final ArrayList<Combatant> myteam = state.getTeam(me);
		ArrayList<Step> threatened = (ArrayList<Step>) walk.solution.clone();
		if (threatened.isEmpty()) {
			throw new RuntimeException();
		}
		threatened.remove(threatened.size() - 1);
		for (Combatant neighbor : state.getCombatants()) {
			for (Step s : threatened) {
				for (int deltax : Walker.DELTAS) {
					for (int deltay : Walker.DELTAS) {
						if (s.x + deltax == neighbor.location[0]
								&& s.y + deltay == neighbor.location[1]
								&& myteam != state.getTeam(neighbor)) {
							return null;
						}
					}
				}
			}
		}
		return walk.solution;
	}

	@Override
	public List<List<ChanceNode>> getSucessors(BattleState gameState,
			Combatant combatant) {
		ArrayList<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>();
		if (gameState.isEngaged(combatant)) {
			return outcomes;
		}
		List<Combatant> targets = gameState.getTargets(combatant);
		filtertargets(combatant, targets, gameState);
		for (Combatant target : targets) {
			outcomes.addAll(charge(gameState, combatant, target));
		}
		return outcomes;
	}

	@Override
	protected void checkhero(Thing hero) {
		return;
	}
}
