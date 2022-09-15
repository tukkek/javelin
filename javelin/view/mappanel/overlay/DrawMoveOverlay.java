package javelin.view.mappanel.overlay;

import java.util.Timer;
import java.util.TimerTask;

import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.BattleMouse;
import javelin.view.mappanel.dungeon.DungeonMouse;
import javelin.view.mappanel.world.WorldMouse;
import javelin.view.screen.BattleScreen;

/**
 * {@link MoveOverlay}s are a bit special when it comes to {@link Overlay}s
 * because they are draw in response to mouse movement. This class handles
 * introducing a small delay so that the User Interface feels more solid and
 * less twitchy. Under the hood, it also prevents a large number of
 * {@link Thread}s to be necessarily spawned.
 *
 * @see BattleMouse
 * @see WorldMouse
 * @see DungeonMouse
 *
 * @author alex
 */
public class DrawMoveOverlay extends TimerTask{
  static final Timer TIMER=new Timer("Move overlay drawer");
  static final int DELAY=50;
  static TimerTask task=null;

  MoveOverlay overlay;

  public DrawMoveOverlay(MoveOverlay o){
    overlay=o;
  }

  @Override
  public void run(){
    if(MapPanel.overlay!=null) MapPanel.overlay.clear();
    MapPanel.overlay=overlay;
    overlay.walk();
    for(var a:overlay.affected)
      BattleScreen.active.mappanel.tiles[a.x][a.y].repaint();
  }

  public synchronized static void draw(final MoveOverlay overlay){
    if(overlay.equals(MapPanel.overlay)) return;
    if(task!=null) task.cancel();
    task=new DrawMoveOverlay(overlay);
    TIMER.schedule(task,DELAY);
  }
}
