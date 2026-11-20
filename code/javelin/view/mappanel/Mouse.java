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
      Interface.click(e);
      if(e.getButton()!=MouseEvent.BUTTON3) return;
      var t=gettile(e);
      if(t!=null) MapPanel.get().center(t.x,t.y,true);
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

  /// Convert click to enter if not {@link Interface#waiting}.
  static public boolean enter(MouseEvent e){
    if(Interface.waiting) return false;
    if(e.getButton()==3) Interface.press('q',KeyEvent.VK_ENTER);
    else Interface.press('\n',KeyEvent.VK_ENTER);
    return true;
  }
}
