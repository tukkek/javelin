package javelin.model.world.location.town;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.ConstructionSite;
import javelin.model.world.location.Location;

/**
 * This class represents the aarea within a city's limits.
 *
 * Since this search can be a relatively costly operation if performed
 * repeatedly this serves as a discardable cache to be used within such an
 * operations. Instead of performing a full search of the relevant data upon
 * creation, data is only gathered when needed and then cached accordingly. As a
 * general rule, this means that calling a given method will only calculate the
 * information on the first ivnvocation, passing the previous result along
 * subsequently.
 *
 * For this reason, instances of this object should be shared and used as much
 * as possible but discarded as soon as cached data may be invalidated. If
 * unsure, get a new instance with {@link Town#getdistrict()} instead.
 *
 * @author alex
 */
public class District{
  /**
   * Not really the maximum size but the maximum natural size if no other
   * district improvement are affecting it.
   */
  public static final int RADIUSMAX=Rank.CITY.getradius()*2;

  static final int MOSTNEIGHBORSALLOWED=2;

  /**
   * Albeit not likely in most {@link Scenario}s, a district may encompass more
   * than one Town. However, it always has a primary Town which sits at the very
   * center of it.
   */
  public Town town;
  ArrayList<Location> locations=null;
  HashSet<Point> area=null;
  ArrayList<Squad> squads=null;

  /** @see #town */
  public District(Town t){
    town=t;
  }

  /** @return All {@link Location}s inside this district. */
  public ArrayList<Location> getlocations(){
    if(locations!=null) return locations;
    locations=new ArrayList<>();
    var all=World.getactors();
    for(Point p:getarea()){
      var a=World.get(p.x,p.y,all);
      if(a instanceof Location) locations.add((Location)a);
    }
    Collections.shuffle(locations);
    return locations;
  }

  /** @return All of the {@link World} coordinates present in this district. */
  public HashSet<Point> getarea(){
    if(area!=null) return area;
    var radius=getradius();
    area=new HashSet<>();
    for(var x=town.x-radius;x<=town.x+radius;x++)
      for(var y=town.y-radius;y<=town.y+radius;y++)
        if(World.validatecoordinate(x,y)) area.add(new Point(x,y));
    return area;
  }

  /**
   * @return Number of squares away from a {@link Town} to consider its
   *   district.
   */
  public int getradius(){
    return town.getrank().getradius();
  }

  /**
   * @param type Will check for this exact class, not subclasses.
   * @return Location of the given type or <code>null</code> if none was found.
   */
  public Actor getlocation(Class<? extends Location> type){
    for(Actor l:getlocations()) if(l.getClass().equals(type)) return l;
    return null;
  }

  /**
   * @return Similar to {@link #getlocations()} but only returning instances of
   *   the given class.
   *
   * @see Class#isInstance(Object)
   */
  public ArrayList<Location> getlocationtype(Class<? extends Location> type){
    var result=new ArrayList<Location>();
    for(Location l:getlocations()) if(type.isInstance(l)) result.add(l);
    return result;
  }

  /** @return All squads currently inside this district. */
  public ArrayList<Squad> getsquads(){
    if(squads!=null) return squads;
    getarea();
    var all=Squad.getsquads();
    squads=new ArrayList<>(all.size());
    for(Squad s:all) if(area.contains(s.getlocation())) squads.add(s);
    return squads;
  }

  /**
   * @return All spots that can be built on and do not have too many neighbors
   *   (as to prevent the creation of "walls" {@link Squad}s will have trouble
   *   passing through). This list is shuffled by default.
   */
  public ArrayList<Point> getfreespaces(){
    var actors=World.getactors();
    var locations=new ArrayList<Actor>();
    for(Actor a:actors) if(a instanceof Location) locations.add(a);
    var free=new ArrayList<Point>();
    final var w=World.getseed();
    searching:for(Point p:getarea()){
      if(Terrain.get(p.x,p.y).equals(Terrain.WATER)
          ||World.get(p.x,p.y,actors)!=null)
        continue searching;
      var neighbors=0;
      for(var x=p.x-1;x<=p.x+1;x++) for(var y=p.y-1;y<=p.y+1;y++){
        if(x==p.x&&y==p.y||!World.validatecoordinate(x,y)
            ||World.get(x,y,locations)==null)
          continue;
        neighbors+=1;
        if(neighbors>MOSTNEIGHBORSALLOWED) continue searching;
      }
      free.add(p);
    }
    Collections.shuffle(free);
    return free;
  }

  /**
   * @return <code>true</code> if is in the process of constructing the given
   *   {@link Location} type.
   *
   * @see ConstructionSite#goal
   */
  public boolean isbuilding(Class<? extends Location> site){
    for(Actor l:getlocationtype(ConstructionSite.class)){
      var c=(ConstructionSite)l;
      if(site.isInstance(c.goal)) return true;
    }
    return false;
  }

  /** @return <code>null</code> or the biggesg district in this coordinate. */
  static public District get(int x,int y){
    var towns=Town.gettowns();
    District main=null;
    for(Town t:towns){
      var d=t.getdistrict();
      if(t.distanceinsteps(x,y)<=d.getradius()
          &&(main==null||t.population>d.town.population))
        main=d;
    }
    return main;
  }
}
