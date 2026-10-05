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
import javelin.view.screen.BattleScreen;

public class MoveOverlay extends Overlay{
  HashMap<Color,Border> BORDERS=new HashMap<>();

  public OverlayWalker path;
  public List<Point> steps;

  public MoveOverlay(OverlayWalker mover){
    path=mover;
  }

  public void walk(){
    steps=path.walk();
    try{
      for(Point step:steps){
        affected.add(step);
        var mappanel=BattleScreen.active.mappanel;
        mappanel.tiles[step.x][step.y].repaint();
      }
    }catch(IndexOutOfBoundsException e){
      affected.clear();
    }
  }

  @Override
  public void overlay(Tile t){
    if(steps==null) return;
    var g=BattleScreen.active.mappanel.getdrawgraphics();
    var p=t.getposition();
    for(Point step:steps){
      var s=(OverlayStep)step;
      if(t.x!=s.x||t.y!=s.y) continue;
      var border=BORDERS.get(s.color);
      if(border==null){
        border=BorderFactory.createLineBorder(s.color,3);
        BORDERS.put(s.color,border);
      }
      border.paintBorder(BattleScreen.active.mappanel.canvas,g,p.x,p.y,
          MapPanel.tilesize,MapPanel.tilesize);
      g.setColor(s.color);
      g.drawString(s.text,p.x+5,p.y+MapPanel.tilesize-5);
    }
  }

  public void reset(){
    path.reset();
    affected.clear();
  }
}
