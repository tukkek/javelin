package javelin.view.mappanel.battle.overlay;

import java.awt.Image;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.walker.state.ClearPath;
import javelin.controller.walker.state.ObstructedPath;
import javelin.view.Images;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;

/** Overlay each square between points. */
public class TraceOverlay extends Overlay{
  static final Image TRACE=Images.get(List.of("overlay","trace"));

  Point to;

  public TraceOverlay(Point from,Point to){
    this.to=to;
    var s=Fight.state;
    List<Point> l=new ClearPath(from,to,s).walk();
    if(l==null) l=new ObstructedPath(from,to,s).walk();
    affected.addAll(l);
  }

  @Override
  public void overlay(Tile t){
    var p=new Point(t.x,t.y);
    if(p.equals(to)) draw(t,TargetOverlay.TARGET);
    else if(affected.contains(p)) draw(t,TRACE);
  }
}
