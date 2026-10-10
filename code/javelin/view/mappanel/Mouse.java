package javelin.view.mappanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import javelin.JavelinApp;
import javelin.old.Interface;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;

public abstract class Mouse extends MouseAdapter{
  MapPanel panel;

  public Mouse(MapPanel panel){
    this.panel=panel;
  }

  @Override
  public void mouseClicked(MouseEvent e){
    if(e.getButton()==MouseEvent.BUTTON3){
      var t=gettile(e);
      BattleScreen.active.mappanel.center(t.x,t.y,true);
    }
  }

  protected Tile gettile(MouseEvent e){
    var size=MapPanel.tilesize;
    var xy=List.of(e.getX(),e.getY());
    xy=xy.stream().map(number->Math.floorDiv(number,size)).toList();
    var tile=panel.tiles[xy.get(0)][xy.get(1)];
    return tile;
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e){
    var p=JavelinApp.context==null?BattlePanel.current.getlocation()
        :JavelinApp.context.getsquadlocation();
    panel.zoom(-e.getWheelRotation(),p.x,p.y);
  }

  /**
   * @return Subclasses should call this at the beggining of
   *   {@link #mouseClicked(MouseEvent)} and return without further action if
   *   <code>true</code>.
   */
  public boolean overrideinput(){
    if(Interface.userinterface.waiting) return false;
    final var k=new KeyEvent(BattleScreen.active.mappanel,0,
        System.currentTimeMillis(),0,0,'c');
    k.setKeyChar('\n');
    Interface.userinterface.go(k);
    return true;
  }
}
