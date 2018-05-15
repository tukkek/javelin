package javelin.view.mappanel.battle.action;

import javelin.controller.action.Fire;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattleMouse;

/**
 * TODO there is an edge case here for the future: if you're not engaged with an
 * opponent but could either attack with a ranged weapon or a reach weapon
 */
public class RangedMouseAction extends BattleMouseAction {
	@Override
	public boolean validate(Combatant current, Combatant target,
			BattleState s) {
		return !target.isally(current, s) && !current.isadjacent(target)
				&& !current.source.ranged.isEmpty() && !s.isengaged(current)
				&& s.haslineofsight(current, target) != Vision.BLOCKED;
	}

	@Override
	public Runnable act(final Combatant current, final Combatant target,
			final BattleState s) {
		return new Runnable() {
			@Override
			public void run() {
				current.rangedattacks(target, s);
			}
		};
	}

	@Override
	public void onenter(Combatant current, Combatant target, Tile t,
			BattleState s) {
		String hitchance = Fire.SINGLETON.describehitchance(current, target, s);
		BattleMouse.showstatus(hitchance, target, true);
	}
}