package javelin.old;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javelin.view.screen.BattleScreen;

/// Handles physical presses and clicks.
public class Interface{
  static final Object LOCK=new Object();

  /// `true` while {@link BattleScreen} waits for an interaction.
  static public boolean waiting=false;
  /// Last key pressed or `null`.
  static public Character key=null;
  /// {@link KeyEvent#getKeyCode()} equivalent of {@link #key}.
  static public Integer code=null;
  /// Last mouse-button clicked or `null`.
  static public Integer click=null;

  /// Clears prior presses and clicks.
  public static void clear(){
    key=null;
    code=null;
    click=null;
  }

  /// Release lock
  public static void release(){
    synchronized(LOCK){
      LOCK.notifyAll();
    }
  }

  /// Handles mouse-clicks.
  static public void click(MouseEvent e){
    clear();
    click=e.getButton();
    release();
  }

  /// Handles key-presses.
  static public void press(char keyp,int codep){
    clear();
    key=keyp;
    code=codep;
    release();
  }

  /// Handles key-presses.
  static public void press(KeyEvent e){
    press(e.getKeyChar(),e.getKeyCode());
  }

  /// Block til user presses or clicks.
  static public void block(){
    try{
      synchronized(LOCK){
        LOCK.wait();
      }
    }catch(InterruptedException e){
      e.printStackTrace();
    }finally{
      waiting=false;
    }
  }
}
