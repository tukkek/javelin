package javelin.view.mappanel.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;

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
import javelin.view.screen.WorldScreen;

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

  void decorate(Graphics g){
    var terrain=Terrain.get(x,y);
    if(!terrain.liquid) return;
    var world=World.SIZE;
    for(var point:COASTLINES.keySet()){
      var x=this.x+point.x;
      var y=this.y+point.y;
      if(!new Point(x,y).validate(0,0,world,world)
          ||Terrain.get(x,y).equals(terrain))
        continue;
      var canvas=draw();
      draw(g,COASTLINES.get(point));
    }
  }

  @Override
  public void paint(Graphics g){
    if(!discovered){
      drawcover(g);
      return;
    }
    draw(g,WorldScreen.get().gettile(x,y));
    decorate(g);
    var actor=WorldPanel.ACTORS.get(new Point(x,y));
    if(actor!=null) draw(actor,g);
  }

  void draw(Location location,Graphics g){
    if(location.drawgarisson())
      draw(g,Images.HOSTILE.get(Tier.get(location.getel())));
    if(location.hascrafted()) draw(g,Images.CRAFTING);
    else if(!location.explored) draw(g,Images.UNEXPLORED);
    if(location.hasupgraded()) draw(g,Images.UPGRADING);
    var working=location.isworking()
        ||location instanceof Town town&&!town.ishostile()&&town.isworking();
    if(working) draw(g,Images.LABOR);
  }

  void draw(Actor actor,Graphics g){
    var size=MapPanel.get().gettilesize();
    var point=draw();
    if(actor==Squad.active){
      g.setColor(Color.GREEN);
      g.fillRect(point.x,point.y,size,size);
      if(Squad.active.getdistrict()!=null)
        DistrictOverlay.paint(point.x,point.y,g);
    }
    draw(g,actor.getimage());
    if(actor.getrealmoverlay()!=null){
      g.setColor(actor.getrealmoverlay().color);
      g.fillRect(point.x,point.y+size-5,size,5);
    }
    if(actor instanceof Location location) draw(location,g);
  }
}
