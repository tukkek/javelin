package javelin.view.mappanel.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldMouse.Movement;
import javelin.view.screen.BattleScreen;

public class MoveOverlay extends Overlay{
  HashMap<Color,Border> BORDERS=new HashMap<>();

  public OverlayWalker path;
  public List<Point> steps;

  public MoveOverlay(OverlayWalker mover){
    path=mover;
    walk();
  }

  public void walk(){
    steps=path.walk();
    var map=MapPanel.get().tiles;
    var tiles=steps.stream().map(point->map[point.x][point.y]).toList();
    affected.addAll(tiles);
  }

  @Override
  public void overlay(Tile tile,Graphics graphics){
    if(steps==null) return;
    var p=tile.draw();
    for(Point step:steps){
      var s=(OverlayStep)step;
      if(tile.x!=s.x||tile.y!=s.y) continue;
      var border=BORDERS.get(s.color);
      if(border==null){
        border=BorderFactory.createLineBorder(s.color,3);
        BORDERS.put(s.color,border);
      }
      var map=MapPanel.get();
      var size=map.gettilesize();
      var point=tile.draw();
      border.paintBorder(map.canvas,graphics,point.x,point.y,size,size);
      graphics.setColor(s.color);
      graphics.drawString(s.text,point.x+5,point.y+size-5);
    }
  }

  public void reset(){
    path.reset();
    affected.clear();
  }

  @Override
  public boolean click(){
    if(steps.isEmpty()) return false;
    BattleScreen.perform(new Movement(this));
    return true;
  }
}
