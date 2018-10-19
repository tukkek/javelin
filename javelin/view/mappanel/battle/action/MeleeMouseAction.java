package javelin.view.mappanel.battle.action;

import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattleMouse;

public class MeleeMouseAction extends BattleMouseAction{
	@Override
	public boolean validate(Combatant current,Combatant target,BattleState s){
		return !target.isally(current,s)&&current.isadjacent(target)
				&&!current.source.melee.isEmpty();
	}

	@Override
	public Runnable act(final Combatant current,final Combatant target,
			final BattleState s){
		return ()->current.meleeattacks(target,s);
	}

	@Override
	public void onenter(Combatant current,Combatant target,Tile t,BattleState s){
		final String chance=MeleeAttack.SINGLETON.getchance(current,target,s);
		final String status=target+" ("+target.getstatus()+", "+chance+")";
		BattleMouse.showstatus(status,target,true);
	}
}