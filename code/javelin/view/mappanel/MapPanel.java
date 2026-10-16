package javelin.view.mappanel;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.lwjgl.opengl.awt.AWTGLCanvas;

import javelin.controller.db.Preferences;
import javelin.view.OpenGL;
import javelin.view.OpenGL.Canvas;
import javelin.view.screen.BattleScreen;

/// Game map view, either in or out of battle.
public abstract class MapPanel extends Panel{
  /** Scrollbar functionality. */
  public ScrollPane scroll=new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
  /** Grid of tiles by coordinate. */
  public Tile[][] tiles=null;
  /** Main component. */
  public AWTGLCanvas canvas=new Canvas();

  /// Centers the {@link #canvas}.
  Panel container=new Panel();
  Mouse mouse=getmouselistener();
  String configurationkey;

  /** Constructor. */
  public MapPanel(int widthp,int heightp,String configurationkeyp){
    scroll.setFocusable(false);
    configurationkey=configurationkeyp;
    tiles=new Tile[widthp][heightp];
    for(var x=0;x<widthp;x++)
      for(var y=0;y<heightp;y++) tiles[x][y]=newtile(x,y);
  }

  /** @return Reactions to mouse events. */
  abstract protected Mouse getmouselistener();

  void resize(){
    synchronized(OpenGL.LOCK){
      var bounds=getBounds();
      scroll.setBounds(bounds);
      container.setSize(scroll.getViewportSize());
      var tilesize=gettilesize();
      var width=tilesize*tiles.length;
      var height=tilesize*tiles[0].length;
      canvas.setSize(width,height);
      revalidate();
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
      canvas.addMouseListener(mouse);
      canvas.addMouseMotionListener(mouse);
      canvas.addMouseWheelListener(mouse);
      container.setLayout(new GridBagLayout());
      var constraints=new GridBagConstraints();
      constraints.anchor=GridBagConstraints.CENTER;
      container.add(canvas,constraints);
      scroll.add(container);
      add(scroll);
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

  /**
   * @param force If false, will only center if coordinates are out-of-view.
   * @return Whether a scroll reposition has been performed.
   */
  public boolean center(int x,int y,boolean force){
    var width=scroll.getWidth();
    var height=scroll.getHeight();
    var current=scroll.getScrollPosition();
    var tilesize=gettilesize();
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
    x=Math.min(x,canvas.getWidth()-width+scroll.getHScrollbarHeight());
    y=Math.min(y,canvas.getHeight()-height+scroll.getVScrollbarWidth());
    x=Math.max(0,x);
    y=Math.max(0,y);
    synchronized(OpenGL.LOCK){
      scroll.setScrollPosition(x,y);
    }
    return true;
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
