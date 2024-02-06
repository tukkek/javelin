package javelin.view.mappanel.overlay;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.screen.BattleScreen;

public abstract class Overlay{
  public ArrayList<Point> affected=new ArrayList<>();

  abstract public void overlay(Tile t);

  /**
   * Here we clear not only the {@link #affected} tiles but all neighbors - this
   * is necessary because depending on zoom scale, text might "bleed" into
   * nearby tiles. Not ideal but still better than redrawing the whole
   * {@link MapPanel}.
   */
  public void clear(){
    MapPanel.overlay=null;
    var m=BattleScreen.active.mappanel;
    final var tiles=m.tiles;
    var affected=new HashSet<Point>();
    for(Point p:new ArrayList<>(this.affected)){
      affected.add(p);
      for(Point neighbor:Point.getadjacentorthogonal()){
        neighbor.x+=p.x;
        neighbor.y+=p.y;
        affected.add(neighbor);
      }
    }
    for(Point p:affected) if(p.validate(0,0,tiles.length,tiles[0].length))
      tiles[p.x][p.y].repaint();
    m.repaint();
  }

  /**
   * Draws image on given tile and adds it to #affected.
   */
  protected void draw(Tile t,Image i){
    var p=t.getposition();
    BattleScreen.active.mappanel.getdrawgraphics().drawImage(i,p.x,p.y,
        MapPanel.tilesize,MapPanel.tilesize,null);
    affected.add(new Point(t.x,t.y));
  }

  public void refresh(MapPanel mappanel){
    for(Point p:affected) mappanel.tiles[p.x][p.y].repaint();
  }
}
