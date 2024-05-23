package javelin.model.world.location;

import static java.util.stream.Collectors.toList;

import java.awt.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * {@link Resource}s boost {@link Town} productivity.
 *
 * TODO add an instantaneous
 * {@link #getupgrades(javelin.model.world.location.town.District)} to connect
 * resources in the {@link District}.
 *
 * @author alex
 */
public class ResourceSite extends Location{
  /** A type of natural resource. */
  public static class Resource implements Serializable,Comparable<Resource>{
    /** Name of this natural resource. */
    public String name;
    /** Type of terrain the relevant {@link ResourceSite} can be found on. */
    public Terrain terrain;

    /**
     * @param name Description.
     * @param t Terrain resource is found on.
     */
    public Resource(String name,Terrain t){
      this.name=name;
      terrain=t;
    }

    @Override
    public boolean equals(Object o){
      return o instanceof Resource r&&r.name.equals(name);
    }

    @Override
    public int hashCode(){
      return name.hashCode();
    }

    @Override
    public String toString(){
      return name;
    }

    @Override
    public int compareTo(Resource o){
      return name.compareTo(o.name);
    }
  }

  /** All existing resources. */
  public static final HashMap<Terrain,Resource> RESOURCES=new HashMap<>();

  static{
    var c=new Resource("Crystal",Terrain.MOUNTAINS);
    var fish=new Resource("Fish",Terrain.WATER);
    var fruit=new Resource("Fruits",Terrain.FOREST);
    var gems=new Resource("Gems",Terrain.DESERT);
    var grain=new Resource("Grains",Terrain.PLAIN);
    var m=new Resource("Mercury",Terrain.MARSH);
    var s=new Resource("Stone",Terrain.HILL);
    for(var t:List.of(c,fish,fruit,gems,grain,m,s)) RESOURCES.put(t.terrain,t);
  }

  /** Resource present on site. */
  public Resource type=RPG.pick(new ArrayList<>(RESOURCES.values()));

  /** Constructor. */
  public ResourceSite(){
    super(null);
    vision=0;
    link=false;
    discard=false;
    allowentry=false;
  }

  @Override
  protected boolean validateplacement(boolean water,World w,List<Actor> actors){
    return type.terrain.equals(Terrain.get(x,y))
        &&super.validateplacement(water,w,actors);
  }

  @Override
  protected void generate(boolean water){
    super.generate(true);
    description=type.name+" (resource)";
    sacrificeable=true;
    allowentry=!type.terrain.equals(Terrain.WATER);
  }

  @Override
  public List<Combatant> getcombatants(){
    return null;
  }

  @Override
  public Image getimage(){
    return Images.get(List.of("world","resource"+type.name.toLowerCase()));
  }

  @Override
  public boolean interact(){
    var towns=Town.gettowns().stream()
        .filter(t->!t.ishostile()&&!t.resources.contains(type)).toList();
    var n=type.name.toLowerCase();
    if(towns.isEmpty()){
      Javelin.message("No friendly town needs %s...".formatted(n),false);
      return false;
    }
    var prompt="Connect this %s resource-site to which friendly town?";
    var c=Javelin.choose(prompt.formatted(n),towns,true,false);
    if(c<0) return false;
    towns.get(c).resources.add(type);
    remove();
    Squad.active.setlocation(getlocation());
    return true;
  }

  /** @return All sites in the {@link World}. */
  public static List<ResourceSite> getall(){
    var sites=World.getall(ResourceSite.class);
    return sites.stream().map(a->(ResourceSite)a).collect(toList());
  }
}
