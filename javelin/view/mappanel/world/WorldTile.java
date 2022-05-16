package javelin.view.mappanel.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.challenge.Tier;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class WorldTile extends Tile{
  public static final HashMap<Point,Image> COASTLINES=new HashMap<>(4);

  static{
    COASTLINES.put(new Point(-1,0),
        Images.get(List.of("overlay","coastlineleft")));
    COASTLINES.put(new Point(+1,0),
        Images.get(List.of("overlay","coastlineright")));
    COASTLINES.put(new Point(0,-1),
        Images.get(List.of("overlay","coastlineup")));
    COASTLINES.put(new Point(0,+1),
        Images.get(List.of("overlay","coastlinedown")));
  }

  public WorldTile(int xp,int yp,WorldPanel p){
    super(xp,yp,false);
  }

  @Override
  public void paint(Graphics g){
    if(g==null) return;
    if(!discovered){
      drawcover(g);
      return;
    }
    draw(g,JavelinApp.context.gettile(x,y));
    if(Terrain.get(x,y).liquid){
      final var t=Terrain.get(x,y);
      for(final Point p:COASTLINES.keySet()){
        final var x=this.x+p.x;
        final var y=this.y+p.y;
        if(World.validatecoordinate(x,y)&&!Terrain.get(x,y).equals(t))
          draw(g,COASTLINES.get(p));
      }
    }
    final var a=WorldPanel.ACTORS.get(new Point(x,y));
    if(a!=null) drawactor(g,a);
    if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
  }

  void drawactor(final Graphics g,final Actor a){
    var x=this.x*MapPanel.tilesize;
    var y=this.y*MapPanel.tilesize;
    if(a==Squad.active){
      g.setColor(Color.GREEN);
      g.fillRect(x,y,MapPanel.tilesize,MapPanel.tilesize);
      if(Squad.active.getdistrict()!=null) DistrictOverlay.paint(this,g);
    }
    draw(g,a.getimage());
    if(a.getrealmoverlay()!=null){
      g.setColor(a.getrealmoverlay().color);
      g.fillRect(x,y+MapPanel.tilesize-5,MapPanel.tilesize,5);
    }
    final var l=a instanceof Location?(Location)a:null;
    if(l==null) return;
    if(l.drawgarisson()) draw(g,Images.HOSTILE.get(Tier.get(l.getel())));
    if(l.hascrafted()) draw(g,Images.CRAFTING);
    if(l.hasupgraded()) draw(g,Images.UPGRADING);
    if(l.isworking()) draw(g,Images.LABOR);
    final var t=l instanceof Town?(Town)l:null;
    if(t!=null&&!t.ishostile()&&t.isworking()) draw(g,Images.LABOR);
  }
}
