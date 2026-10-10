package javelin.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.awt.GLData.Profile;
import org.lwjgl.opengl.awt.GLData.VersionPolicy;
import org.lwjgl.system.MemoryUtil;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Steam;
import javelin.old.Screen;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

/**
 * <p>
 * LWJGL3-AWT was the easiest way to migrate from Swing to Open GL for the
 * Steam-overlay to work. This class encapsulates all related operations.
 * </p>
 * <h2>Architecture</h2>
 * <ul>
 * <li>{@link #ready()} schedules a Swing-{@link Timer} that calls
 * {@link Canvas#render()}</li>
 * <li>{@link MapPanel#refresh(Graphics)} delegates to {@link Tile}s which draw
 * on a {@link BufferedImage}</li>
 * <li>The image is copied to the {@link GL11#GL_BACK} buffer</li>
 * <li>Steam injects the overlay when {@link Canvas#swapBuffers()} is called
 * </li>
 * </ul>
 * <p>
 * If not run from Steam then the game just renders normally. See
 * {@link Steam#ENABLED}.
 * </p>
 * <h2>Direct rendering</h2>
 * <p>
 * Conversion would not be needed if {@link Tile}s drew directly on {@link GL11}
 * rather than {@link Graphics2D} but a full UI rewrite to Java FX is preferred.
 * </p>
 * <p>
 * If desired this can easily be done. Most operations have direct equivalents.
 * Only {@link Image#getScaledInstance(int, int, int)} would be a moderate task.
 * </p>
 * <h2>Flags</h2>
 * <p>
 * These do not seem required posttesting. They are documented as
 * potential-future fixes. See {@link #check()}.
 * <ul>
 * <li>-Dsun.java2d.noddraw=true</li>
 * <li>-Dsun.java2d.opengl=true</li>
 * <li>-Dsun.awt.noerasebackground=false</li>
 * </ul>
 * </p>
 */
public class OpenGL{
  /// If `true` then render only one frame per second.
  public static final boolean DEBUG=false;
  /**
   * To ensure consistency per frame rendered this lock synchronizes:
   * <ul>
   * <li>{@link Canvas#render()}</li>
   * <li>{@link JavelinApp#switchScreen(java.awt.Component)} so {@link Screen}s
   * are not removed midpaint</li>
   * <li>Changing the {@link Overlay}</li>
   * </ul>
   */
  public static final Object LOCK=new Object();

  static final int FREQUENCY=DEBUG?1:10;
  static final int TICK=1_000/FREQUENCY;
  /// System-property that prevents Open GL from initializing implicitly.
  static final String EXPLICIT="org.lwjgl.opengl.explicitInit";
  /// Synchronize vertically. May hog the Swing-{@link Timer}-thread (EDT) but
  /// should not matter at slowly refreshing rates. Change to 0 to disable.
  static final int SYNCHRONIZE=1;
  /// Not supported on Mac, Optional but ubiquitous on Linux and Windows.
  /// Dropping Converter and only using GL would allow use of the Core profile.
  static final Profile COMPATIBLE=GLData.Profile.COMPATIBILITY;
  /// Clears backlayer on Windown to prevent artifacts posttiles.
  static final boolean REPAINT=true;

  static class Renderer implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent e){
      var screen=BattleScreen.active;
      if(screen==null) return;
      var panel=screen.mappanel;
      if(panel==null) return;
      var canvas=panel.canvas;
      if(canvas.isDisplayable()&&canvas.isShowing()) synchronized(LOCK){
        canvas.render();
      }
    }
  }

  static class Texture{
    int identity;
    int width;
    int height;
  }

  static class Converter{
    static final float[][] QUADS={new float[]{0.0f,0.0f,-1f,-1f},
        new float[]{1.0f,0.0f,1f,-1f},new float[]{1.0f,1.0f,1f,1f},
        new float[]{0.0f,1.0f,-1f,1f}};

    static Texture texture=new Texture();

    Canvas canvas;
    BufferedImage image;
    Graphics2D graphics;

    Converter(Canvas canvasp){
      canvas=canvasp;
      var width=canvasp.getWidth();
      var height=canvasp.getHeight();
      image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
      graphics=image.createGraphics();
    }

    static void ready(){
      texture=new Texture();
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
      var target=GL11.GL_TEXTURE_2D;
      GL11.glEnable(target);
      var identity=GL11.glGenTextures();
      texture.identity=identity;
      GL11.glBindTexture(target,identity);
      var linear=GL11.GL_LINEAR;
      GL11.glTexParameteri(target,GL11.GL_TEXTURE_MIN_FILTER,linear);
      GL11.glTexParameteri(target,GL11.GL_TEXTURE_MAG_FILTER,linear);
      GL11.glBindTexture(target,0);
    }

    void surface(){
      var key=RenderingHints.KEY_ANTIALIASING;
      var value=RenderingHints.VALUE_ANTIALIAS_ON;
      graphics.setRenderingHint(key,value);
      key=RenderingHints.KEY_TEXT_ANTIALIASING;
      value=RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
      graphics.setRenderingHint(key,value);
      graphics.setBackground(new Color(0,0,0,0));
      graphics.clearRect(0,0,image.getWidth(),image.getHeight());
    }

    ByteBuffer buffer(){
      var width=image.getWidth();
      var height=image.getHeight();
      var pixels=new int[width*height];
      image.getRGB(0,0,width,height,pixels,0,width);
      var buffer=MemoryUtil.memAlloc(width*height*4);
      for(var y=height-1;y>=0;y--) for(var x=0;x<width;x++){
        var pixel=pixels[y*width+x];
        buffer.put((byte)(pixel>>16&0xFF)); // Red
        buffer.put((byte)(pixel>>8&0xFF)); // Green
        buffer.put((byte)(pixel&0xFF)); // Blue
        buffer.put((byte)(pixel>>24&0xFF)); // Alpha
      }
      buffer.flip();
      return buffer;
    }

    void update(){
      var buffer=buffer();
      var target=GL11.GL_TEXTURE_2D;
      GL11.glBindTexture(target,texture.identity);
      var size=new int[]{image.getWidth(),image.getHeight()};
      var rgba=GL11.GL_RGBA;
      var type=GL11.GL_UNSIGNED_BYTE;
      if(size[0]==texture.width&&size[1]==texture.height)
        GL11.glTexSubImage2D(target,0,0,0,size[0],size[1],rgba,type,buffer);
      else{
        GL11.glTexImage2D(target,0,rgba,size[0],size[1],0,rgba,type,buffer);
        texture.width=size[0];
        texture.height=size[1];
      }
      GL11.glBindTexture(target,0);
      MemoryUtil.memFree(buffer); // Release native memory
    }

    void copy(){
      var target=GL11.GL_TEXTURE_2D;
      GL11.glBindTexture(target,texture.identity);
      GL11.glBegin(GL11.GL_QUADS);
      for(var quad:QUADS){
        GL11.glTexCoord2f(quad[0],quad[1]);
        GL11.glVertex2f(quad[2],quad[3]);
      }
      GL11.glEnd();
      GL11.glBindTexture(target,0);
    }

    void predraw(){
      GL11.glViewport(0,0,canvas.getWidth(),canvas.getHeight());
      surface();
    }

    void postdraw(){
      graphics.dispose();
      update();
      copy();
    }
  }

  /// @see MapPanel#canvas
  public static class Canvas extends AWTGLCanvas{
    /// Constructor
    public Canvas(){
      var data=new GLData();
      data.swapInterval=SYNCHRONIZE;
      data.doubleBuffer=true;
      data.profile=COMPATIBLE;
      data.majorVersion=3;
      data.minorVersion=2;
      data.versionPolicy=VersionPolicy.AT_LEAST;
      super(data);//JDK25 allows this
      setFocusable(false);
      setFocusTraversalKeysEnabled(false);
      setIgnoreRepaint(!REPAINT);
    }

    @Override
    public void initGL(){
      GL.createCapabilities();
      var color=0f;
      GL11.glClearColor(color,color,color,1);
      Converter.ready();
    }

    void overlay(BattleScreen screen,Graphics2D graphics){
      var overlay=Overlay.get();
      if(overlay==null) return;
      for(var point:new ArrayList<>(overlay.affected)){
        var tile=screen.mappanel.tiles[point.x][point.y];
        if(tile.discovered) overlay.overlay(tile,graphics);
      }
    }

    @Override
    public void paintGL(){
      var screen=BattleScreen.active;
      if(screen==null) return;
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
      var converter=new Converter(this);
      converter.predraw();
      // executes in 8ms so should support 100+ FPS
      var graphics=converter.graphics;
      screen.mappanel.refresh(graphics);
      overlay(screen,graphics);
      converter.postdraw();
      swapBuffers();
    }
  }

  static Timer timer=null;

  /// - {@link #EXPLICIT} start for {@link GL} so {@link Steam} is ready to hook
  /// - Schedule rendering
  public static void ready(){
    GL.create();
    Debug.log("GL created.",OpenGL.class);
    timer=new Timer(TICK,new Renderer());
    timer.start();
  }

  /// Check that system-properties have been set at launch. Doing so now is
  /// usually too late for AWT.
  ///
  /// @see Javelin#main(String[])
  public static void check(){
    var property=EXPLICIT;
    var expected="true";
    if(expected.equals(System.getProperty(property))) return;
    var text="%s not set to %s,".formatted(property,expected);
    throw new RuntimeException(text);
  }

  /// Dispose the current {@link Canvas}.
  public static void dispose(){
    var screen=BattleScreen.active;
    if(screen!=null) screen.mappanel.canvas.disposeCanvas();
  }

  /// Release system-resources when closing {@link Javelin}.
  public static void destroy(){
    GL.destroy();
  }

  /// Configure for maximum compatibility.
  public static void configure(JFrame frame){
    frame.getRootPane().setDoubleBuffered(false);
    frame.setFocusTraversalKeysEnabled(false);
    var manager=KeyboardFocusManager.getCurrentKeyboardFocusManager();
    var keys=KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS;
    manager.setDefaultFocusTraversalKeys(keys,Collections.emptySet());
  }
}
