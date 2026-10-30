package javelin.view.mappanel.battle.overlay;

import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.walker.state.ClearPath;
import javelin.controller.walker.state.ObstructedPath;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;

/** Overlay each square between points. */
public class TraceOverlay extends Overlay{
  static final Image TRACE=Images.get(List.of("overlay","trace"));

  Point to;

  public TraceOverlay(Point from,Point to){
    this.to=to;
    var state=Fight.state;
    var points=new ClearPath(from,to,state).walk();
    if(points==null) points=new ObstructedPath(from,to,state).walk();
    points.add(to);
    var map=MapPanel.get();
    var tiles=points.stream().map(point->map.tiles[point.x][point.y]).toList();
    affected.addAll(tiles);
  }

  @Override
  public void overlay(Tile t,Graphics g){
    var p=new Point(t.x,t.y);
    if(p.equals(to)) draw(t,TargetOverlay.TARGET,g);
    else if(affected.contains(t)) draw(t,TRACE,g);
  }
}
