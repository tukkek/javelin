package javelin.controller.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.RangedAttack;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.walker.Walker;
import javelin.model.BattleMap;
import javelin.model.condition.Charging;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Combatant;
import javelin.model.unit.CurrentAttack;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.StatisticsScreen;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Ranged attack.
 * 
 * TODO call this Target and make Fire a subclass. This will allow to create
 * reach attacks as well.
 * 
 * @author alex
 */
public class Fire extends AiAction {

	private char confirmkey;

	/**
	 * @param confirm
	 *            Usually the same as the action key so as to make pressing the
	 *            same key twice a "invoke action" + "confirm targeting".
	 * @see Action#Action(String, String).
	 */
	public Fire(final String name, final String key, char confirm) {
		super(name, key);
		this.confirmkey = confirm;
	}

	public Fire() {
		this("Fire or throw ranged weapon", "f", 'f');
	}

	@Override
	public boolean perform(final Combatant c, BattleMap map, Thing thing) {
		final Thing hero = Game.hero();
		checkhero(hero);
		final BattleState state = map.getState();
		if (checkengaged(state, state.clone(hero.combatant))) {
			Game.message("Disengage first!", null, Delay.WAIT);
			throw new RepeatTurn();
		}
		final Combatant combatant = state.clone(Game.hero().combatant);
		final List<Combatant> targets =
				state.getAllTargets(combatant, state.getCombatants());
		filtertargets(combatant, targets, state);
		if (targets.isEmpty()) {
			Game.message("No valid targets.", null, Delay.WAIT);
			throw new RepeatTurn();
		}
		Collections.sort(targets, new Comparator<Combatant>() {
			@Override
			public int compare(final Combatant o1, final Combatant o2) {
				int priority1 = prioritize(c, state, o1);
				int priority2 = prioritize(c, state, o2);
				if (priority1 == priority2) {
					return new Long(Math.round(Walker.distance(o1, c) * 10
							- Walker.distance(o2, c) * 10)).intValue();
				}
				return priority1 > priority2 ? -1 : 1;
			}
		});
		selecttarget(map, combatant, targets, state);
		return true;
	}

	/**
	 * TODO turn into dynamic instead?
	 */
	public int prioritize(final Combatant c, final BattleState state,
			final Combatant target) {
		int priority = -target.surprise();
		if (state.hasLineOfSight(c, target) == Vision.COVERED) {
			priority -= 4;
		}
		/* TODO take into account relevant feats */
		if (state.isengaged(target)) {
			priority -= 4;
		}
		if (target.hascondition(Charging.class) != null) {
			priority += 2;
		}
		return priority;
	}

	private void selecttarget(BattleMap map, final Combatant combatant,
			final List<Combatant> targets, BattleState state) {
		int targeti = 0;
		lockTarget(targets.get(0), map, combatant, state);
		while (true) {
			Game.redraw();
			final Character key = InfoScreen.feedback();
			if (Action.MOVE_W.isPressed(key)) {
				targeti -= 1;
			} else if (Action.MOVE_E.isPressed(key)) {
				targeti += 1;
			} else if (key == '\n' || key == confirmkey) {
				Game.messagepanel.clear();
				attack(combatant, targets.get(targeti), state, map);
				break;
			} else if (key == 'v') {
				new StatisticsScreen(targets.get(targeti));
				// } else if (key == 'C') {
				// int[] center = targets.get(targeti).location;
				// BattleScreen.active.centerscreen(center[0], center[1], true);
				// BattleScreen.active.mappanel.repaint();
			} else {
				Game.messagepanel.clear();
				Game.instance().hero = combatant.visual;
				throw new RepeatTurn();
			}
			final int max = targets.size() - 1;
			if (targeti > max) {
				targeti = 0;
			} else if (targeti < 0) {
				targeti = max;
			}
			lockTarget(targets.get(targeti), map, combatant, state);
		}
	}

	protected boolean checkengaged(final BattleState state, Combatant c) {
		return state.isengaged(c);
	}

	protected void checkhero(final Thing hero) {
		hero.combatant.checkAttackType(false);
	}

	protected void filtertargets(Combatant active, List<Combatant> targets,
			BattleState s) {
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			if (target.isAlly(active, s)
					|| s.hasLineOfSight(active, target) == Vision.BLOCKED) {
				targets.remove(target);
			}
		}
	}

	protected void attack(Combatant combatant, Combatant targetCombatant,
			BattleState battleState, final BattleMap map) {
		BattleState state = map.getState();
		combatant = state.clone(combatant);
		targetCombatant = state.clone(targetCombatant);
		Action.outcome(RangedAttack.SINGLETON.attack(state, combatant,
				targetCombatant, combatant.chooseattack(combatant.source.ranged,
						targetCombatant),
				0));
	}

	private void lockTarget(final Combatant target, BattleMap map,
			Combatant active, BattleState state) {
		Game.instance().hero = target.visual;
		Game.messagepanel.clear();
		Game.message(
				"Use ← and → arrows to select target, ENTER or " + confirmkey
						+ " to confirm, v to view target's sheet, q to quit.\n",
				null, Delay.NONE);
		Game.message(target + " (" + target.getStatus() + ", "
				+ Javelin.translatetochance(
						calculatehitdc(target, active, state))
				+ " to hit)", null, Delay.NONE);
		BattleScreen.active.centerscreen(target.location[0],
				target.location[1]);
	}

	/**
	 * @return Minimum number the active combatant has to roll on a d20 to hit
	 *         the target.
	 */
	protected int calculatehitdc(final Combatant target, Combatant active,
			BattleState state) {
		return target.ac()
				- predictattack(active.currentranged,
						active.source.ranged).bonus
				- prioritize(active, state, target)
				+ RangedAttack.SINGLETON.getpenalty(active, target, state);
	}

	Attack predictattack(CurrentAttack hint, List<AttackSequence> fallback) {
		AttackSequence currentranged = hint.sequenceindex == -1 ? null
				: fallback.get(hint.sequenceindex);
		Attack a = currentranged == null ? null : hint.peek();
		if (a == null) {
			a = fallback.get(0).get(0);
		}
		return a;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(BattleState s, Combatant active) {
		/* Handled by RangedAttack. */
		return new ArrayList<List<ChanceNode>>();
	}
}
