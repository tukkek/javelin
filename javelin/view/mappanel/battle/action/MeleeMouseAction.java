package javelin.view.mappanel.battle.action;

import java.util.List;

import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.Attack;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattleMouse;
import javelin.view.screen.BattleScreen;

public class MeleeMouseAction extends BattleMouseAction {
	@Override
	public boolean validate(Combatant current, Combatant target,
			BattleState s) {
		return !target.isally(current, s) && current.isadjacent(target)
				&& !current.source.melee.isEmpty();
	}

	@Override
	public void act(final Combatant current, final Combatant target,
			final BattleState s) {
		BattleScreen.perform(new Runnable() {
			@Override
			public void run() {
				current.meleeattacks(target, s);
			}
		});
	}

	@Override
	public void onenter(Combatant current, Combatant target, Tile t,
			BattleState s) {
		final List<Attack> attack = current.currentmelee.next == null
				|| current.currentmelee.next.isEmpty()
						? current.source.melee.get(0)
						: current.currentmelee.next;
		final String chance = MeleeAttack.SINGLETON.getchance(current, target,
				attack.get(0), s);
		final String status = target + " (" + target.getstatus() + ", " + chance
				+ " to hit)";
		BattleMouse.showstatus(status, target, true);
	}
}