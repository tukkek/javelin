package javelin.view.mappanel.overlay;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.view.OpenGL;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

/// Produces a temporary effect on {@link Canvas}.
public abstract class Overlay{
  static Overlay active=null;

  public ArrayList<Point> affected=new ArrayList<>();

  abstract public void overlay(Tile t,Graphics g);

  /// Draws image on given tile and adds it to #affected.
  protected void draw(Tile t,Image i,Graphics g){
    var p=t.getposition();
    var size=MapPanel.tilesize;
    g.drawImage(i,p.x,p.y,size,size,null);
    affected.add(new Point(t.x,t.y));
  }

  public boolean click(){
    return false;
  }

  /// changes the active overlay
  public static void set(Overlay o){
    synchronized(OpenGL.LOCK){
      active=o;
    }
  }

  /// clears the active overlay
  public static void clear(){
    set(null);
  }

  /// gets the active overlay
  public static Overlay get(){
    return active;
  }
}
