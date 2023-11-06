package javelin.view.mappanel.battle.action;

import javelin.controller.content.action.ai.attack.AttackResolver;
import javelin.controller.content.action.ai.attack.MeleeAttack;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattleMouse;
import javelin.view.mappanel.battle.overlay.TargetOverlay;

public class MeleeMouseAction extends BattleMouseAction{
  @Override
  public boolean validate(Combatant current,Combatant target,BattleState s){
    return !target.isally(current,s)&&current.isadjacent(target)
        &&!current.source.melee.isEmpty();
  }

  @Override
  public Runnable act(Combatant current,Combatant target,BattleState s){
    return ()->current.meleeattacks(target,s);
  }

  @Override
  public void onenter(Combatant c,Combatant target,Tile t,BattleState s){
    var sequence=c.source.melee.get(0);
    var resolver=new AttackResolver(MeleeAttack.INSTANCE,c,target,
        sequence.get(0),s);
    resolver.preview(target);
    var status=target+" ("+target.getstatus()+", "+resolver.chance+")";
    var p=target.getlocation();
    var o=new TargetOverlay(p.x,p.y);
    BattleMouse.showstatus(status,target,o);
  }
}
