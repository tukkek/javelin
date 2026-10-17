package javelin.view.mappanel.battle.overlay;

import java.awt.Graphics;
import java.awt.Image;
import java.util.Collection;

import javelin.controller.Point;
import javelin.controller.content.action.ai.AiAction;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;

/**
 * Used to simply allow {@link AiAction}s to present some visual feedback. Takes
 * one or more {@link Point}s and draws a target on them.
 *
 * @author alex
 */
public class AiOverlay extends Overlay{
  public Image image=TargetOverlay.TARGET;

  public AiOverlay(int x,int y){
    this(new Point(x,y));
  }

  public AiOverlay(Collection<Point> area){
    var map=MapPanel.get();
    var tiles=area.stream().map(point->map.tiles[point.x][point.y]).toList();
    affected.addAll(tiles);
  }

  public AiOverlay(Point p){
    var tile=MapPanel.get().tiles[p.x][p.y];
    affected.add(tile);
  }

  public AiOverlay(Combatant c){
    this(c.getlocation());
  }

  @Override
  public void overlay(Tile t,Graphics g){
    if(affected.contains(t)) draw(t,image,g);
  }
}
