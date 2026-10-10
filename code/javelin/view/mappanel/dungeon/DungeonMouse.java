package javelin.view.mappanel.dungeon;

import java.awt.event.MouseEvent;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.Interface;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.overlay.MoveOverlay;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.mappanel.world.WorldMouse;
import javelin.view.screen.BattleScreen;

public class DungeonMouse extends Mouse{
  public DungeonMouse(MapPanel panel){
    super(panel);
  }

  @Override
  public void mouseClicked(MouseEvent e){
    if(overrideinput()||!Interface.userinterface.waiting) return;
    final var t=gettile(e);
    if(!t.discovered) return;
    if(e.getButton()==MouseEvent.BUTTON1&&WorldMouse.move()){
      var p=Dungeon.active.squadlocation;
      BattleScreen.active.mappanel.center(p.x,p.y,true);
    }else super.mouseClicked(e);
  }

  @Override
  public void mouseMoved(MouseEvent e){
    if(!Interface.userinterface.waiting) return;
    Overlay.clear();
    final var t=gettile(e);
    if(!t.discovered) return;
    var l=new Point(Dungeon.active.squadlocation);
    var walker=new DungeonWalker(l,new Point(t.x,t.y),Dungeon.active);
    Overlay.set(new MoveOverlay(walker));
  }
}
