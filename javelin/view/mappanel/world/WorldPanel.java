package javelin.view.mappanel.world;

import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

public class WorldPanel extends MapPanel{

  static final HashMap<Point,Actor> ACTORS=new HashMap<>();
  public static final HashMap<Point,Location> DESTINATIONS=new HashMap<>();

  public WorldPanel(){
    super(World.getseed().map.length,World.getseed().map[0].length,
        Preferences.KEYTILEWORLD);
  }

  @Override
  protected Mouse getmouselistener(){
    return new WorldMouse(this);
  }

  @Override
  protected int gettilesize(){
    return Preferences.tilesizeworld;
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
  public void refresh(){
    updateactors();
    for(var ts:tiles) for(var t:ts) t.repaint();
  }
}
