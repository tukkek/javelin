package javelin.view.mappanel;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Timer;
import java.util.TimerTask;

import javelin.controller.db.Preferences;
import javelin.view.mappanel.overlay.Overlay;

/**
 * Game map view, either in or out of battle.
 *
 * @author alex
 */
public abstract class MapPanel extends Panel{
  /** Current tile size. */
  public static int tilesize=Preferences.tilesizeworld;
  /** Produces a temporary overlay effect on-screen. */
  public static Overlay overlay=null;
  /** Semaphore to block painting. */
  public static final Object PAINTER=new Object();

  /** Scrollbar functionality. */
  public ScrollPane scroll=new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
  /** Grid of tiles by coordinate. */
  public Tile[][] tiles=null;
  /** Main component for panel. */
  public Canvas canvas=new Canvas(){
    boolean first=true;

    @Override
    public void paint(Graphics g){
      refresh();
      if(first){
        first=false;
        new Timer().schedule(new TimerTask(){
          @Override
          public void run(){
            refresh();
          }
        },200);
      }
    }
  };
  Panel container=new Panel();
  /**
   * Make sure we have a field for this to ensure we're going to instantiate
   * {@link #tilesize}**2 listeners for this.
   */
  Mouse mouse=getmouselistener();
  String configurationkey;
  int mapwidth;
  int mapheight;

  /** Constructor. */
  public MapPanel(int widthp,int heightp,String configurationkeyp){
    mapwidth=widthp;
    mapheight=heightp;
    scroll.setFocusable(false);
    add(scroll);
    container.setFocusable(false);
    canvas.setFocusable(false);
    configurationkey=configurationkeyp;
  }

  /** @return Reactions to mouse events. */
  abstract protected Mouse getmouselistener();

  void resize(){
    canvas.setSize(tilesize*mapwidth,tilesize*mapheight);
  }

  /** TODO could use this to cache position */
  protected void updatetilesize(){
    resize();
    scroll.validate();
  }

  /** Configures size, scrolling, listeners, etc. */
  public void setup(){
    addComponentListener(new ComponentAdapter(){
      @Override
      public void componentResized(ComponentEvent e){
        scroll.setBounds(getBounds());
      }
    });
    tilesize=gettilesize();
    scroll.setVisible(false);
    resize();
    tiles=new Tile[mapwidth][mapheight];
    for(var y=0;y<mapheight;y++)
      for(var x=0;x<mapwidth;x++) tiles[x][y]=newtile(x,y);
    container.add(canvas);
    canvas.addMouseListener(mouse);
    canvas.addMouseMotionListener(mouse);
    canvas.addMouseWheelListener(mouse);
    scroll.add(container);
    scroll.setVisible(true);
  }

  /** @see Preferences */
  protected abstract int gettilesize();

  /** TODO can probably use reflection instead */
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
    tilesize+=factor*4;
    updatetilesize();
    center(x,y,true);
    Preferences.setoption(configurationkey,tilesize);
  }

  static boolean isinside(double from,int value,int offset){
    return from<=value&&value<=from+offset;
  }

  /**
   * @param force If false, will only center if coordinates are out-of-view.
   * @return Whether a scroll reposition has been performed.
   */
  public boolean center(int x,int y,boolean force){
    var width=scroll.getWidth();
    var height=scroll.getHeight();
    var current=scroll.getScrollPosition();
    x*=tilesize;
    y*=tilesize;
    var offset=tilesize*2;
    var fromx=current.getX();
    var fromy=current.getY();
    if(!force&&isinside(fromx,x,width-offset)&&isinside(fromy,y,height-offset)){
      scroll.setScrollPosition(current);
      return false;
    }
    x-=width/2;
    y-=height/2;
    x=Math.min(x,container.getWidth()-width+scroll.getHScrollbarHeight());
    y=Math.min(y,container.getHeight()-height+scroll.getVScrollbarWidth());
    x=Math.max(0,x);
    y=Math.max(0,y);
    scroll.setScrollPosition(x,y);
    return true;
  }

  /** Updates {@link Tile}s without redrawing the whole screen. */
  abstract public void refresh();

  /** @return {@link Canvas#getGraphics()}. */
  public Graphics getdrawgraphics(){
    return canvas.getGraphics();
  }
}
