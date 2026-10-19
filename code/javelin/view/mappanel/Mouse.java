package javelin.view.mappanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javelin.JavelinApp;
import javelin.old.Interface;
import javelin.view.OpenGL;
import javelin.view.mappanel.battle.BattlePanel;

public abstract class Mouse extends MouseAdapter{
  @Override
  public void mouseClicked(MouseEvent e){
    synchronized(OpenGL.LOCK){
      if(e.getButton()==MouseEvent.BUTTON3){
        var t=gettile(e);
        MapPanel.get().center(t.x,t.y,true);
      }
    }
  }

  protected Tile gettile(MouseEvent e){
    var map=MapPanel.get();
    return map.grid.get(e.getX(),e.getY());
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e){
    synchronized(OpenGL.LOCK){
      var p=JavelinApp.context==null?BattlePanel.current.getlocation()
          :JavelinApp.context.getsquadlocation();
      MapPanel.get().zoom(-e.getWheelRotation(),p.x,p.y);
    }
  }

  /**
   * @return Subclasses should call this at the beggining of
   *   {@link #mouseClicked(MouseEvent)} and return without further action if
   *   <code>true</code>.
   */
  public boolean overrideinput(){
    if(Interface.userinterface.waiting) return false;
    var k=new KeyEvent(MapPanel.get(),0,System.currentTimeMillis(),0,0,'c');
    k.setKeyChar('\n');
    Interface.userinterface.go(k);
    return true;
  }
}
