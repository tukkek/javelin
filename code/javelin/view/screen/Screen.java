package javelin.view.screen;

import java.awt.Component;
import java.awt.EventQueue;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.QuestApp;
import javelin.old.TPanel;
import javelin.view.OpenGL;
import javelin.view.mappanel.overlay.Overlay;

public class Screen extends TPanel{
  public Screen(){
    setFont(QuestApp.mainfont);
    setForeground(QuestApp.INFOTEXTCOLOUR);
    setBackground(QuestApp.INFOSCREENCOLOUR);
  }

  public Character getinput(){
    return InfoScreen.feedback();
  }

  public void close(){
    // nothing by default
  }

  /** Switches to a new screen, discarding the old one. */
  public static void change(Component component){
    var application=Javelin.app;
    var last=application.mainComponent;
    if(last==component) return;
    synchronized(OpenGL.LOCK){
      BattleScreen.active=null;
      if(last instanceof Screen screen) screen.close();
      application.removeAll();
      Overlay.clear();
    }
    if(Squad.active!=null&&component instanceof WorldScreen w) w.firstdraw=true;
    var screen=component instanceof BattleScreen s?s:null;
    if(screen!=null) BattleScreen.active=screen;
    application.mainComponent=component;
    application.add(component);
    component.revalidate();
    component.requestFocus();
    if(screen==null) return;
    var point=screen.anchor();
    var map=screen.mappanel;
    if(point!=null)
      EventQueue.invokeLater(()->map.setposition(point.x,point.y));
  }

  /// Opens a [WorldScreen]; [DungeonScreen] or [BattleScreen].
  public static void reopen(){
    var screen=BattleScreen.active;
    if(screen!=null&&screen==Javelin.app.mainComponent){
      Javelin.redraw();
      return;
    }
    var dungeon=Dungeon.active;
    if(Fight.current!=null) screen=new BattleScreen(true);
    else if(dungeon==null) screen=new WorldScreen();
    else dungeon.enter();
    if(screen==null) return;
    screen.open();
    Screen.change(screen);
  }
}
