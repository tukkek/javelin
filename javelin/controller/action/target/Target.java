package javelin.controller.action.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Charging;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Base class for all actions involving selecting an unit as target.
 * 
 * @author alex
 */
public abstract class Target extends Action {
	public class SelectTarget implements Comparator<Combatant> {
		final Combatant c;
		final BattleState state;

		public SelectTarget(Combatant c, BattleState state) {
			this.c = c;
			this.state = state;
		}

		/**
		 * A higher value means this should be selected first while browsing
		 * targets.
		 * 
		 * TODO turn into dynamic instead?
		 */
		public int prioritize(final Combatant c, final BattleState state,
				final Combatant target) {
			int priority = -target.surprise();
			if (state.haslineofsight(c, target) == Vision.COVERED) {
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

		@Override
		public int compare(final Combatant o1, final Combatant o2) {
			int priority1 = prioritize(c, state, o1);
			int priority2 = prioritize(c, state, o2);
			if (priority1 != priority2) {
				return priority1 > priority2 ? -1 : 1;
			}
			final double distance1 = Walker.distance(o1, c) * 10;
			final double distance2 = Walker.distance(o2, c) * 10;
			return Math.round(Math.round(distance1 - distance2));
		}
	}

	/**
	 * Pressing this key confirms the target selection, usually same as the
	 * action key.
	 * 
	 * @see Action#keys
	 */
	protected char confirmkey;

	/** Constructor. */
	public Target(String string) {
		super(string);
	}

	/** Constructor. */
	public Target(String string, String[] strings) {
		super(string, strings);
	}

	/** Constructor. */
	public Target(String name, String key) {
		super(name, key);
	}

	/**
	 * Used for descriptive purposes.
	 * 
	 * @return Minimum number the active combatant has to roll on a d20 to hit
	 *         the target.
	 */
	protected abstract int calculatehitdc(Combatant active,
			final Combatant target, BattleState s);

	/** Called once a target is confirmed. */
	protected abstract void attack(Combatant active, Combatant target,
			BattleState s);

	@Override
	public boolean perform(final Combatant c) {
		checkhero(c);
		final BattleState state = Fight.state.clone();
		if (checkengaged(state, state.clone(c))) {
			Game.messagepanel.clear();
			Game.message("Disengage first...", Delay.WAIT);
			throw new RepeatTurn();
		}
		final Combatant combatant = state.clone(c);
		final List<Combatant> targets = state.gettargets(combatant,
				state.getcombatants());
		filtertargets(combatant, targets, state);
		if (targets.isEmpty()) {
			Game.messagepanel.clear();
			Game.message("No valid targets...", Delay.WAIT);
			throw new RepeatTurn();
		}
		Collections.sort(targets, new SelectTarget(c, state));
		selecttarget(combatant, targets, state);
		return true;
	}

	private void selecttarget(final Combatant combatant,
			final List<Combatant> targets, BattleState state) {
		int targeti = 0;
		lockTarget(targets.get(0), combatant, state);
		while (true) {
			Game.redraw();
			final Character key = InfoScreen.feedback();
			if (Action.MOVE_W.isPressed(key) || key == '-') {
				targeti -= 1;
			} else if (Action.MOVE_E.isPressed(key) || key == '+') {
				targeti += 1;
			} else if (key == '\n' || key == confirmkey) {
				MapPanel.overlay.clear();
				Game.messagepanel.clear();
				attack(combatant, targets.get(targeti), state);
				break;
			} else if (key == 'v') {
				new StatisticsScreen(targets.get(targeti));
			} else {
				MapPanel.overlay.clear();
				Game.messagepanel.clear();
				throw new RepeatTurn();
			}
			final int max = targets.size() - 1;
			if (targeti > max) {
				targeti = 0;
			} else if (targeti < 0) {
				targeti = max;
			}
			lockTarget(targets.get(targeti), combatant, state);
		}
	}

	/**
	 * By default uses {@link BattleState#isengaged(Combatant)}
	 * 
	 * @return <code>true</code> if the active unit is currently engaded and
	 *         should not be allowed to continue targetting.
	 */
	protected boolean checkengaged(final BattleState state, Combatant c) {
		return state.isengaged(c);
	}

	/**
	 * Does nothing by default.
	 * 
	 * @param hero
	 *            Active unit.
	 * @throws RepeatTurn
	 */
	protected void checkhero(final Combatant hero) {

	}

	/**
	 * By default only allows targeting enemies that are in line-of-sight.
	 * 
	 * @param targets
	 *            Remove invalid targets from this list. Beware of
	 *            {@link ConcurrentModificationException}.
	 */
	protected void filtertargets(Combatant active, List<Combatant> targets,
			BattleState s) {
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			if (target.isally(active, s)
					|| s.haslineofsight(active, target) == Vision.BLOCKED) {
				targets.remove(target);
			}
		}
	}

	private void lockTarget(final Combatant target, Combatant active,
			BattleState state) {
		MapPanel.overlay = new TargetOverlay(target.location[0],
				target.location[1]);
		Game.messagepanel.clear();
		Game.message(
				"Use ← and → to select target, ENTER or " + confirmkey
						+ " to confirm, v to view target's sheet, q to quit.\n",
				Delay.NONE);
		Game.message(describehitchance(active, target, state), Delay.NONE);
		BattleScreen.active.centerscreen(target.location[0],
				target.location[1]);
	}

	/**
	 * @return Text with the name of the target and chance to hit.
	 */
	public String describehitchance(Combatant active, final Combatant target,
			BattleState state) {
		return target + " (" + target.getstatus() + ", " + Javelin
				.translatetochance(calculatehitdc(active, target, state))
				+ " to hit)";
	}
}