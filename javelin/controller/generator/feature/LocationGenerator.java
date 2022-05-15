package javelin.controller.generator.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.scenario.Scenario;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.WorldGenerator;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.ContestedTerritory;
import javelin.model.world.location.Location;
import javelin.model.world.location.Outpost;
import javelin.model.world.location.PointOfInterest;
import javelin.model.world.location.ResourceSite;
import javelin.model.world.location.WarlocksTower;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.haunt.AbandonedManor;
import javelin.model.world.location.haunt.BeastLair;
import javelin.model.world.location.haunt.Conflux;
import javelin.model.world.location.haunt.DarkShrine;
import javelin.model.world.location.haunt.Graveyard;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.haunt.HolyGrounds;
import javelin.model.world.location.haunt.OrcSettlement;
import javelin.model.world.location.haunt.ShatteredTemple;
import javelin.model.world.location.haunt.Spire;
import javelin.model.world.location.haunt.SunkenShip;
import javelin.model.world.location.haunt.settlement.ChaoticSettlement;
import javelin.model.world.location.haunt.settlement.EvilSettlement;
import javelin.model.world.location.haunt.settlement.GoodSettlement;
import javelin.model.world.location.haunt.settlement.LawfulSettlement;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.governor.MonsterGovernor;
import javelin.model.world.location.town.labor.basic.BasicAcademy;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.basic.Shop;
import javelin.model.world.location.town.labor.expansive.Hub;
import javelin.model.world.location.unique.AdventurersGuild;
import javelin.model.world.location.unique.Arena;
import javelin.model.world.location.unique.DeepDungeon;
import javelin.model.world.location.unique.PillarOfSkulls;
import javelin.old.RPG;

/**
 * Responsible for generating those {@link Actor}s (mostly {@link Location}s
 * that can be spawned both during {@link World} generation and normal gameplay.
 *
 * @author alex
 */
public class LocationGenerator implements Serializable{
  /** All haunt types. */
  public static final List<Class<? extends Haunt>> HAUNTS=new ArrayList<>(
      List.of(SunkenShip.class,ShatteredTemple.class,Graveyard.class,
          OrcSettlement.class,OrcSettlement.class,AbandonedManor.class,
          BeastLair.class,Spire.class,Conflux.class,GoodSettlement.class,
          EvilSettlement.class,LawfulSettlement.class,ChaoticSettlement.class,
          HolyGrounds.class,DarkShrine.class));

  final HashMap<Class<? extends Actor>,Frequency> generators=new HashMap<>();

  /**
   * The ultimate goal of this method is to try and make it so one feature only
   * is generated per week. Since we want some features to have a higher chance
   * of being spawned this deals with these cases dynamically to avoid
   * manually-written methods from becoming too large.
   */
  void setup(){
    generators.put(Outpost.class,new Frequency(.1f));
    if(PointOfInterest.ENABLED)
      generators.put(PointOfInterest.class,new Frequency(2f));
    var resources=new Frequency(.5f);
    resources.seeds=Realm.REALMS.size()*2;
    resources.max=resources.seeds;
    generators.put(ResourceSite.class,resources);
    convertchances();
  }

  /**
   * Will convert all relative (non-absolute) {@link Frequency#chance} to an
   * absolute value so as to make them sum up to a 100%.
   *
   * @see Frequency#absolute
   */
  void convertchances(){
    var total=0F;
    for(Frequency g:generators.values()) if(!g.absolute) total+=g.chance;
    for(Frequency g:generators.values())
      if(!g.absolute) g.chance=g.chance/total;
  }

  /**
   * Spawns {@link Actor}s into the game world. Used both during world
   * generation and during a game's progress.
   *
   * @param chance Used to modify the default spawning chances. For example: if
   *   this is called daily but the target is to spawn one feature per week then
   *   one would provide a 1/7f value here. The default spawning chances are
   *   calculated so as to sum up to 100% so using a value of 1 would be likely
   *   to spawn 1 random feature in the world map, but could spawn more or none
   *   depending on the random number generator results.
   * @param generatingworld If <code>false</code> will limit spawning to only a
   *   starting set of actors. <code>true</code> is supposed to be used while
   *   the game is progressing to support the full feature set.
   * @see Frequency#starting
   */
  public void spawn(float chance,boolean generatingworld){
    if(count()>=World.scenario.startingfeatures
        ||!generatingworld&&!World.scenario.respawnlocations)
      return;
    var features=generators.keySet();
    for(var f:RPG.shuffle(new ArrayList<>(features))){
      var frequency=generators.get(f);
      if(generatingworld&&!frequency.starting
          ||frequency.max!=null&&World.getall(f).size()>=frequency.max)
        continue;
      if(RPG.random()<=chance*frequency.chance) frequency.generate(f).place();
    }
  }

  /**
   * @param a Spawns this actor near the given {@link Town}.
   * @param min Minimum distance.
   * @param max Maximum distance.
   * @param clear Whether to capture the garrison, if the given actor is a
   *   {@link Location}.
   * @return The given actor, for call-chaining.
   */
  static Actor spawnnear(Town t,Actor a,World w,int min,int max,boolean clear){
    Point p=null;
    var actors=World.getactors();
    while(p==null||World.get(t.x+p.x,t.y+p.y,actors)!=null
        ||!World.validatecoordinate(t.x+p.x,t.y+p.y)
        ||w.map[t.x+p.x][t.y+p.y].equals(Terrain.WATER)){
      p=new Point(RPG.r(min,max),RPG.r(min,max));
      if(RPG.chancein(2)) p.x*=-1;
      if(RPG.chancein(2)) p.y*=-1;
      WorldGenerator.retry();
    }
    a.x=p.x+t.x;
    a.y=p.y+t.y;
    var l=a instanceof Location?(Location)a:null;
    a.place();
    if(l!=null&&clear) l.capture();
    return a;
  }

  long countadjacent(Point p){
    var actors=World.getactors();
    var s=World.scenario.size;
    return p.getadjacent().stream()
        .filter(a->a.validate(s,s)&&World.get(a.x,a.y,actors)!=null).count();
  }

  void placecontested(){
    var s=World.scenario.size;
    var free=Point.getrange(s,s);
    var w=World.getseed();
    for(var a:World.getactors()) free.remove(a.getlocation());
    for(var t:RPG.shuffle(new ArrayList<>(Arrays.asList(Terrain.STANDARD)))){
      var maps=new ArrayList<>(t.maps);
      maps.addAll(t.shoremaps);
      for(var m:RPG.shuffle(maps)){
        var locations=free.stream().filter(p->Terrain.get(p.x,p.y).equals(t)
            &&countadjacent(p)==0&&District.get(p.x,p.y)==null).toList();
        if(t.shoremaps.contains(m)) locations=locations.stream()
            .filter(p->Terrain.search(p,Terrain.WATER,1,w)>0).toList();
        if(!locations.isEmpty()) try{
          var i=m.getConstructor().newInstance();
          var l=RPG.pick(locations);
          new ContestedTerritory(i).place(l);
          free.remove(l);
        }catch(ReflectiveOperationException e){
          throw new RuntimeException(e);
        }
      }
    }
  }

  void generatestaticlocations(){
    var locations=new ArrayList<Location>();
    locations.add(new PillarOfSkulls());
    for(var h:RPG.shuffle(HAUNTS)) try{
      locations.add(h.getConstructor().newInstance());
    }catch(ReflectiveOperationException e){
      throw new RuntimeException();
    }
    for(var level=Tier.LOW.minlevel;level<=Tier.HIGH.maxlevel;level++){
      var nfloors=1;
      var t=DungeonTier.get(level);
      var maxdepth=DungeonTier.TIERS.indexOf(t)+1;
      while(nfloors<maxdepth&&RPG.chancein(2)) nfloors+=1;
      locations.add(new DungeonEntrance(new Dungeon(t.name,level,nfloors)));
    }
    for(var i=0;i<15;i++) locations.add(new DungeonEntrance(new Wilderness()));
    for(var l:RPG.shuffle(locations)) l.place();
    placecontested();
  }

  static void generatestartingarea(World w,Town t){
    var lodge=new Lodge();
    lodge.impermeable=true;
    for(var l:RPG.shuffle(new ArrayList<>(
        List.of(lodge,Shop.newbasic(),new BasicAcademy(),new Hub()))))
      spawnnear(t,l,w,1,2,true);
    var p=t.getlocation();
    var recruits=RPG.shuffle(Terrain.get(p.x,p.y).getmonsters());
    recruits.sort((o1,o2)->{
      var difference=o1.cr-o2.cr;
      if(difference==0) return 0;
      return difference>0?1:-1;
    });
    spawnnear(t,new AdventurersGuild(),w,2,3,true).reveal();
    spawnnear(t,new Arena(t),w,2,3,false).reveal();
    var r=District.RADIUSMAX/2;
    spawnnear(t,new WarlocksTower(),w,r+1,(int)Math.round(r*1.5),false)
        .reveal();
    placedeepdungeon(w,t);
    w.discovered.addAll(t.getdistrict().getarea());
  }

  /**
   * The {@link DeepDungeon} represents one of the major playstyles for Javelin.
   * As such we want it to be easily accessible from the start. This way,
   * players who only want to dungeon crawl can learn to look for it and focus
   * mostly on that.
   */
  static void placedeepdungeon(World w,Town t){
    var d=new DungeonEntrance(new DeepDungeon());
    var allowed=Set.of(Terrain.FOREST,Terrain.HILL,Terrain.PLAIN);
    while(d.x<0||!allowed.contains(Terrain.get(d.x,d.y)))
      spawnnear(t,d,w,4,4,false);
    d.reveal();
  }

  /**
   * Starts generating the inital state for this {@link World}.
   *
   * TODO parameters should be a Map instead of 2 lists
   *
   * @param realms Shuffled list of realms.
   * @param regions Each area in the world, in the same order as the realms.
   *
   * @see Terrain
   */
  public Location generate(LinkedList<Realm> realms,
      ArrayList<HashSet<Point>> regions,World w){
    generatetowns(realms,regions);
    var starting=determinestartingtown(w);
    normalizemap(starting);
    generatefeatures(w,starting);
    normalizemap(starting);
    for(Town t:Town.gettowns()) World.scenario.populate(t,t==starting);
    return starting;
  }

  void generatefeatures(World w,Town starting){
    setup();
    Temple.generatetemples();
    generatestartingarea(w,starting);
    generatestaticlocations();
    for(Class<? extends Actor> feature:generators.keySet())
      generators.get(feature).seed(feature);
    var target=World.scenario.startingfeatures-Location.count();
    while(count()<target) spawn(1,true);
  }

  int count(){
    var count=0;
    var actors=World.getseed().actors.values();
    for(ArrayList<Actor> instances:actors) count+=instances.size();
    return count;
  }

  static Town gettown(Terrain terrain,World seed,ArrayList<Town> towns){
    Collections.shuffle(towns);
    for(Town town:towns) if(seed.map[town.x][town.y]==terrain) return town;
    throw new RestartWorldGeneration();
    /* TODO there is a bug that is allowing the generation to fall here, debug
     * when it happens and make sure towns are being generated properly */
  }

  Town determinestartingtown(World seed){
    var starton=RPG.r(1,2)==1?Terrain.PLAIN:Terrain.HILL;
    var towns=Town.gettowns();
    var starting=World.scenario.easystartingtown?gettown(starton,seed,towns)
        :RPG.pick(towns);
    if(Terrain.search(new Point(starting.x,starting.y),Terrain.WATER,2,seed)!=0)
      throw new RestartWorldGeneration();
    return starting;
  }

  /**
   * Turn whole map into 2 {@link Realm}s only so that there won't be
   * in-fighting between hostile {@link Town}s.
   *
   * @param starting
   *
   * @see Scenario#normalizemap
   */
  void normalizemap(Town starting){
    if(!World.scenario.normalizemap) return;
    var towns=Town.gettowns();
    towns.remove(starting);
    var r=towns.get(0).originalrealm;
    for(Actor a:World.getactors()){
      var l=a instanceof Location?(Location)a:null;
      if(l!=null&&l.realm!=null){
        l.realm=r;
        if(a instanceof Town t){
          t.originalrealm=r;
          t.setgovernor(new MonsterGovernor(t));
        }
      }
    }
  }

  void generatetowns(LinkedList<Realm> realms,
      ArrayList<HashSet<Point>> regions){
    var towns=World.scenario.towns;
    for(var i=0;i<regions.size()&&towns>0;i++){
      var t=WorldGenerator.GENERATIONORDER[i];
      if(!t.equals(Terrain.WATER)){
        new Town(regions.get(i),realms.pop()).place();
        towns-=1;
      }
    }
  }
}
