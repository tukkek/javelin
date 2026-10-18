package javelin.view.mappanel;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;

import javelin.controller.Point;
import javelin.model.state.Square;
import javelin.view.mappanel.MapPanel.Grid;
import javelin.view.screen.BattleScreen;

/**
 * Similar to a {@link Square} but for the view layer instead of the controller
 * layer.
 *
 * @author alex
 */
public abstract class Tile{
  /** Scaled image cache. */
  public static HashMap<Image,Image> cache=new HashMap<>();

  /** Position on X axis. */
  public final int x;
  /** Position on Y axis. */
  public final int y;
  /** <code>true</code> if tile has been seen already. */
  public boolean discovered;

  /** Constructor. */
  public Tile(final int xp,final int yp,final boolean discoveredp){
    x=xp;
    y=yp;
    discovered=discoveredp;
  }

  /// Draws the tile on the given {@link Canvas} coordinate.
  abstract public void paint(Graphics c);

  /// See {@link Grid#position(Tile)}.
  public Point draw(){
    return Grid.get().position(this);
  }

  /** Scales, caches and renders image. */
  protected void draw(Graphics g,Image i){
    var map=MapPanel.get();
    if(map==null) return;
    var size=map.gettilesize();
    var scaled=cache.get(i);
    if(scaled==null){
      scaled=i.getScaledInstance(size,size,Image.SCALE_DEFAULT);
      cache.put(i,scaled);
    }
    var point=draw();
    g.drawImage(scaled,point.x,point.y,size,size,null);
  }

  /** Sets as not-{@link #discovered}. */
  public void cover(){
    discovered=false;
  }

  /** Redraws. */
  public void repaint(Graphics g){
    var screen=BattleScreen.active;
    if(screen==null) return;
    var panel=screen.mappanel;
    if(panel==null) return;
    paint(g);
  }

  /** Fills with black. */
  protected void drawcover(Graphics g){
    if(g==null) return;
    g.setColor(Color.BLACK);
    var size=MapPanel.get().gettilesize();
    var point=draw();
    g.fillRect(point.x,point.y,size,size);
  }

  @Override
  public String toString(){
    return "%s %s:%s".formatted(getClass().getSimpleName(),x,y);
  }
}
