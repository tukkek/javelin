package javelin.view.mappanel.battle.action;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.BattleWalker;
import javelin.view.mappanel.battle.overlay.BattleWalker.BattleStep;
import javelin.view.mappanel.overlay.MoveOverlay;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

public class MoveMouseAction extends BattleMouseAction{
  public MoveMouseAction(){
    clearoverlay=false;
  }

  @Override
  public boolean validate(Combatant current,Combatant target,BattleState s){
    return target==null;
  }

  @Override
  public Runnable act(Combatant current,Combatant target,BattleState s){
    var walk=Overlay.get() instanceof MoveOverlay overlay?overlay:null;
    if(walk==null||walk.steps.isEmpty()) return null;
    Overlay.clear();
    return ()->{
      var finalstep=walk.steps.size()-1;
      final var to=(BattleStep)walk.steps.get(finalstep);
      var move=Fight.state;
      var c=move.clone(current);
      c.location[0]=to.x;
      c.location[1]=to.y;
      c.ap+=to.totalcost-BattleScreen.partialmove;
      var m=move.getmeld(to.x,to.y);
      if(m!=null&&c.ap>=m.meldsat) Fight.current.meld(c,m);
      if(to.engaged) Javelin.message(c+" disengages...",Delay.WAIT);
    };
  }

  @Override
  public void onenter(Combatant current,Combatant target,Tile t,BattleState s){
    var from=current.getlocation();
    var to=new Point(t.x,t.y);
    Overlay.set(new MoveOverlay(new BattleWalker(from,to,current,s)));
  }
}
