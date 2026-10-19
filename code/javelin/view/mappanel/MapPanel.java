package javelin.view.mappanel;

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.lwjgl.opengl.awt.AWTGLCanvas;

import javelin.controller.Point;
import javelin.controller.Steam;
import javelin.controller.db.Preferences;
import javelin.view.OpenGL;
import javelin.view.OpenGL.Canvas;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

/// Game map view, either in or out of battle.
public abstract class MapPanel extends Panel{
  /// The {@link Steam} overlay requires a plain rendering area so
  /// {@link ScrollPane}s are not viable. This class alternatively only draws the
  /// seen {@link Tile}s on the {@link MapPanel#canvas}.
  ///
  /// This essentially works as the subset of {@link MapPanel#tiles} that
  /// are drawn.
  public class Grid{
    /// Replaces the anchor-{@link Tile} if not `null`.
    public Tile center=null;

    /// Only tiles currently drawn. May contain `null` values if out of bounds.
    Tile[][] drawn=null;
    /// Last {@link MapPanel#gettilesize()}. {@link #ready()} post changing.
    int size=-1;
    /// Location of first drawn pixel.
    Point origin=null;
    /// Force a {@link #drawn} if `true`.
    boolean clear=false;

    void center(){
      var size=gettilesize();
      var width=canvas.getWidth()-tiles.length*size;
      if(width>0) origin.x=width/2;
      var height=canvas.getHeight()-tiles[0].length*size;
      if(height>0) origin.y=height/2;
    }

    /// Calculates {@link #origin} and {@link #drawn}. Tries to stay in bounds.
    void ready(Tile anchor){
      size=gettilesize();
      var canvas=MapPanel.this.canvas.getSize();
      var center=new Point(canvas.width/2-size/2,canvas.height/2-size/2);
      origin=new Point(center.x%size,center.y%size);
      var width=(canvas.width-origin.x)/size;
      var height=(canvas.height-origin.y)/size;
      center();
      var tilex=anchor.x-(center.x-origin.x)/size;
      if(tilex+width>tiles.length) tilex=tiles.length-width;
      if(tilex<0) tilex=0;
      var tiley=anchor.y-(center.y-origin.y)/size;
      if(tiley+height>tiles[0].length) tiley=tiles[0].length-height;
      if(tiley<0) tiley=0;
      var tile=tiles[tilex][tiley];
      drawn=new Tile[width][height];
      for(var x=0;x<width;x+=1) for(var y=0;y<height;y+=1){
        var point=new Point(tile.x+x,tile.y+y);
        if(point.validate(0,0,tiles.length,tiles[0].length))
          drawn[x][y]=tiles[point.x][point.y];
      }
    }

    boolean contains(Tile t){
      if(drawn==null) return false;
      var tile=drawn[0][0];
      if(tile.x<=t.x&&t.x<tile.x+drawn.length)
        if(tile.y<=t.y&&t.y<tile.y+drawn[0].length) return true;
      return false;
    }

    /// @return `false` if Grid has to be recalculated.
    boolean check(Tile anchor){
      if(clear||gettilesize()!=size||!contains(anchor)) return false;
      var width=drawn.length;
      var tile=drawn[0][0];
      var x=anchor.x-tile.x;
      if(x==0||x==width-1) return false;
      var height=drawn[0].length;
      var y=anchor.y-tile.y;
      if(y==0||y==height-1) return false;
      return true;
    }

    void overlay(Tile tile,Graphics g){
      var overlay=Overlay.get();
      if(overlay!=null&&overlay.affected.contains(tile))
        overlay.overlay(tile,g);
    }

    /// See {@link Tile#paint(Graphics)}.
    public void draw(Tile anchor,Graphics g){
      if(center!=null) anchor=center;
      if(!check(anchor)) ready(anchor);
      for(var tiles:drawn) for(var tile:tiles) if(tile!=null){
        tile.repaint(g);
        overlay(tile,g);
      }
      clear=false;
    }

    /// Forces next {@link #drawn}.
    public void clear(){
      clear=true;
    }

    /// @return Tile at the given coordinate.
    public Tile get(int x,int y){
      if(origin==null) return null;
      var size=gettilesize();
      x=(x-origin.x)/size;
      y=(y-origin.y)/size;
      var point=new Point(x,y);
      return point.validate(0,0,drawn.length,drawn[0].length)?drawn[x][y]:null;
    }

    /// @return {@link Canvas}~cCoordinates for the tile.
    public Point position(Tile target){
      var tile=drawn[0][0];
      var size=gettilesize();
      var x=origin.x+(target.x-tile.x)*size;
      var y=origin.y+(target.y-tile.y)*size;
      return new Point(x,y);
    }

    /// @return Current instance.
    public static Grid get(){
      return MapPanel.get().grid;
    }

    /// Sets {@link #center} to this {@link Tile}. Can force a {@link #clear()}.
    public void center(Tile tile,boolean force){
      synchronized(OpenGL.LOCK){
        if(force) clear();
        center=tile;
      }
    }
  }

  /// All tiles
  public Tile[][] tiles=null;
  /** Main component. */
  public AWTGLCanvas canvas=new Canvas();
  /// See {@link #refresh(Graphics)}.
  public Grid grid=new Grid();

  Mouse mouse=getmouselistener();
  String configurationkey;

  /** Constructor. */
  public MapPanel(int widthp,int heightp,String configurationkeyp){
    configurationkey=configurationkeyp;
    tiles=new Tile[widthp][heightp];
    for(var x=0;x<widthp;x++)
      for(var y=0;y<heightp;y++) tiles[x][y]=newtile(x,y);
  }

  /** @return Reactions to mouse events. */
  abstract protected Mouse getmouselistener();

  void resize(){
    synchronized(OpenGL.LOCK){
      canvas.setBounds(getBounds());
      revalidate();
      grid.clear();
    }
  }

  /** Configures size, scrolling, listeners, etc. */
  public void setup(){
    addComponentListener(new ComponentAdapter(){
      @Override
      public void componentResized(ComponentEvent e){
        resize();
      }
    });
    synchronized(OpenGL.LOCK){
      canvas.setFocusable(false);
      canvas.addMouseListener(mouse);
      canvas.addMouseMotionListener(mouse);
      canvas.addMouseWheelListener(mouse);
      add(canvas);
    }
  }

  /** @see Preferences */
  public abstract int gettilesize();

  /// See {@link #zoom(int, int, int)}.
  protected abstract void settilesize(int size);

  /// TODO can probably use reflection to create tiles
  protected abstract Tile newtile(int x,int y);

  /** Non-forceful {@link #center(int, int, boolean)}. */
  public void viewposition(int x,int y){
    center(x,y,false);
  }

  /** Forceful {@link #center(int, int, boolean)}. */
  public void setposition(int x,int y){
    center(x,y,true);
  }

  /**
   * @param factor How much to zoom in (positive) or out (negative) for. 0
   *   results in no changes.
   * @param x Coordinate to center on.
   * @param y Coordinate to center on.
   */
  public void zoom(int factor,int x,int y){
    Tile.cache.clear();
    var size=gettilesize()+factor*4;
    settilesize(size);
    resize();
    center(x,y,true);
    Preferences.setoption(configurationkey,size);
  }

  static boolean isinside(double from,int value,int offset){
    return from<=value&&value<=from+offset;
  }

  /// @param force If false, will only center if coordinates are out-of-view.
  public void center(int x,int y,boolean force){
    grid.center(tiles[x][y],force);
  }

  /// Draws the panel.
  ///
  /// @see Tile#paint(Graphics)
  abstract public void refresh(Graphics g);

  /// @return Map-component for {@link BattleScreen#active}.
  static public MapPanel get(){
    return BattleScreen.active.mappanel;
  }
}
