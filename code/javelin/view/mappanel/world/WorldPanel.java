package javelin.view.mappanel.world;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.dungeon.DungeonPanel;

public class WorldPanel extends MapPanel{
  public static final Map<Point,Location> DESTINATIONS=new HashMap<>();

  static final Map<Point,Actor> ACTORS=new HashMap<>();

  public WorldPanel(){
    super(World.getseed().map.length,World.getseed().map[0].length,
        Preferences.KEYTILEWORLD);
  }

  @Override
  protected Mouse getmouselistener(){
    return new WorldMouse();
  }

  @Override
  public int gettilesize(){
    return Preferences.tilesizeworld;
  }

  @Override
  protected void settilesize(int size){
    Preferences.tilesizeworld=size;
  }

  @Override
  protected Tile newtile(int x,int y){
    return new WorldTile(x,y,this);
  }

  void updateactors(){
    DESTINATIONS.clear();
    ACTORS.clear();
    for(Actor a:World.getactors()){
      ACTORS.put(new Point(a.x,a.y),a);
      if(!(a instanceof Location l)) continue;
      if(l.link) DESTINATIONS.put(new Point(l.x,l.y),l);
    }
  }

  @Override
  public void refresh(Graphics g){
    updateactors();
    DungeonPanel.refresh(Squad.active.getlocation(),g);
  }
}
