package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.state.Square;
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

  /** Draws the tile. */
  abstract public void paint(Graphics c);

  /** Scales, caches and renders image. */
  protected void draw(final Graphics g,final Image i){
    var size=MapPanel.tilesize;
    var scaled=cache.get(i);
    if(scaled==null){
      scaled=i.getScaledInstance(size,size,Image.SCALE_DEFAULT);
      cache.put(i,scaled);
    }
    var p=getposition();
    g.drawImage(scaled,p.x,p.y,size,size,null);
  }

  /** Sets as not-{@link #discovered}. */
  public void cover(){
    discovered=false;
  }

  /** Redraws. */
  public void repaint(){
    try{
      paint(BattleScreen.active.mappanel.getdrawgraphics());
    }catch(Exception e){
      if(Javelin.DEBUG) throw e;
    }
  }

  /**
   * TODO might decide to turn into field instead of generating so many times.
   */
  public Point getposition(){
    return new Point(x*MapPanel.tilesize,y*MapPanel.tilesize);
  }

  /** Fills with black. */
  protected void drawcover(final Graphics g){
    if(g==null) return;
    var p=getposition();
    g.setColor(Color.BLACK);
    g.fillRect(p.x,p.y,MapPanel.tilesize,MapPanel.tilesize);
  }
}
