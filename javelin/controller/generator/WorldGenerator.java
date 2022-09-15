package javelin.controller.generator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.collection.CountingSet;
import javelin.controller.content.fight.RandomEncounter;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.db.Preferences;
import javelin.controller.exception.RestartWorldGeneration;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.feature.BranchPortal;
import javelin.old.RPG;
import javelin.view.screen.InfoScreen;

/**
 * Mulit-threaded {@link World} generation. Also does {@link Dungeon#generate()}
 * once a valid world is generated.
 *
 * @see World#seed
 * @author alex
 */
public class WorldGenerator extends Thread{
  /** {@link Terrain} generation steps. */
  public static final Terrain[] GENERATIONORDER={Terrain.MOUNTAINS,
      Terrain.MOUNTAINS,Terrain.DESERT,Terrain.PLAIN,Terrain.HILL,Terrain.WATER,
      Terrain.WATER,Terrain.MARSH,Terrain.FOREST};
  /** Arbitrary number to serve as guideline for {@link Terrain} generation. */
  public static final int NREGIONS=16;
  /** @see Debug */
  public static final CountingSet RESETS=Javelin.DEBUG?new CountingSet():null;

  static final Terrain[] NOISE={Terrain.PLAIN,Terrain.HILL,Terrain.FOREST,
      Terrain.MOUNTAINS};
  static final String PROGRESSHEADER="Building world, using %S thread(s)...";
  static final int NTHREADS=Math.max(1,Preferences.maxthreads);
  static final List<WorldGenerator> WORLDTHREADS=new ArrayList<>(NTHREADS);
  static final int NOISEAMOUNT=World.SIZE*World.SIZE/10;
  static final String PROGRESS="%s: %s%%...";
  static final int MAXRETRIES=1000*2;
  static final boolean DEBUG=false;
  static final int REFRESH=100;

  /** Where to place first {@link Squad}. */
  public static Location start;

  static boolean working=false;
  static int discarded=0;

  static class ProgressScreen extends InfoScreen{
    LinkedList<String> reports=new LinkedList<>();

    public ProgressScreen(String header){
      super(header);
      reports.addAll(List.of(header,""));
      fix();
    }

    void fix(){
      reports.add(null);
    }

    @Override
    public void print(String line){
      reports.removeLast();
      reports.add(line);
      super.print(String.join("\n",reports));
    }

    void reset(){
      while(reports.size()>3) reports.remove(3);
    }
  }

  /** {@link World#seed} being generated. */
  public World world;

  int retries=0;

  @Override
  public final void run(){
    while(World.seed==null) try{
      generate();
    }catch(RestartWorldGeneration e){
      continue;
    }
  }

  /** Creates {@link World} geography and {@link Location}s. */
  protected void generate(){
    retries=0;
    world=new World();
    var realms=RPG.shuffle(new LinkedList<>(Realm.REALMS));
    var regions=new ArrayList<Set<Point>>(realms.size());
    generategeography(regions,world);
    world.featuregenerator=new LocationGenerator();
    var start=world.featuregenerator.generate(realms,regions,world);
    finish(start,world);
  }

  synchronized void finish(Location start,World w){
    if(World.seed!=null) return;
    World.seed=w;
    RandomEncounter.generate(w);
    WorldGenerator.start=start;
  }

  /** Handles when {@link World} generation is taking too long. */
  synchronized final void bumpretry(){
    if(Thread.interrupted()) return;
    retries+=1;
    if(retries<MAXRETRIES) return;
    retries=0;
    discarded+=1;
    if(DEBUG){
      var e=new RuntimeException("Reset");
      e.fillInStackTrace();
      RESETS.add(JavelinApp.printstacktrace(e));
    }
    throw new RestartWorldGeneration();
  }

  /** Calls {@link #bumpretry()} on {@link Thread#currentThread()}. */
  public static void retry(){
    if(!working) return;
    if(World.seed!=null) throw new RestartWorldGeneration();
    var t=(WorldGenerator)Thread.currentThread();
    t.bumpretry();
  }

  void generategeography(List<Set<Point>> regions,World w){
    var size=World.SIZE;
    for(var i=0;i<size;i++) for(var j=0;j<size;j++) w.map[i][j]=Terrain.FOREST;
    for(Terrain t:WorldGenerator.GENERATIONORDER) regions.add(t.generate(w));
    var nw=new Point(0,0);
    var sw=new Point(0,size-1);
    var se=new Point(size-1,size-1);
    var ne=new Point(size-1,0);
    floodedge(nw,sw,+1,0,w);
    floodedge(sw,se,0,-1,w);
    floodedge(ne,se,-1,0,w);
    floodedge(nw,ne,0,+1,w);
  }

  void floodedge(Point from,Point to,int deltax,int deltay,World w){
    var edge=new ArrayList<Point>(World.SIZE);
    edge.add(from);
    edge.add(to);
    if(from.x!=to.x)
      for(var x=from.x+1;x!=to.x;x++) edge.add(new Point(x,from.y));
    else for(var y=from.y+1;y!=to.y;y++) edge.add(new Point(from.x,y));
    final var map=w.map;
    for(Point p:edge){
      map[p.x][p.y]=Terrain.WATER;
      if(RPG.random()<=.5f){
        map[p.x+deltax][p.y+deltay]=Terrain.WATER;
        if(RPG.random()<=.33f) map[p.x+deltax*2][p.y+deltay*2]=Terrain.WATER;
      }
    }
  }

  static void generateworld(InfoScreen s){
    try{
      for(var i=0;i<NTHREADS;i++) startthread();
      var lastdiscarded=-1;
      while(World.seed==null){
        if(lastdiscarded!=discarded){
          s.print("Worlds discarded: "+discarded+'.');
          lastdiscarded=discarded;
        }
        Thread.sleep(REFRESH);
      }
      for(var t:WORLDTHREADS){
        t.interrupt();
        t.join();
      }
    }catch(InterruptedException e){
      for(var t:WORLDTHREADS) t.interrupt();
      throw new RuntimeException(e);
    }
  }

  static void addbranches(Dungeon d,List<Dungeon> dungeons){
    for(var f:d.floors){
      var portals=f.features.getall(BranchPortal.class);
      for(var p:portals){
        dungeons.add(p.destination);
        addbranches(p.destination,dungeons);
      }
    }
  }

  static boolean generatedungeons(String label,ProgressScreen s){
    s.print(String.format(PROGRESS,label,0));
    var dungeons=World.getactors().stream()
        .filter(a->a instanceof DungeonEntrance)
        .map(a->((DungeonEntrance)a).dungeon).collect(Collectors.toList());
    for(var d:new ArrayList<>(dungeons)) addbranches(d,dungeons);
    dungeons=dungeons.stream().filter(d->d.floors.getFirst().features.isEmpty())
        .sorted((a,b)->Integer.compare(a.floors.size(),b.floors.size()))
        .collect(Collectors.toList());
    if(dungeons.isEmpty()) return false;
    var pool=Executors.newFixedThreadPool(NTHREADS);
    var ndungeons=dungeons.size();
    var tasks=new ArrayList<Future<?>>(ndungeons);
    for(var d:dungeons) tasks.add(pool.submit(()->d.generate()));
    for(var i=0;i<ndungeons;i++) try{
      tasks.get(i).get();
      var progress=100.0*(i+1)/ndungeons;
      s.print(String.format(PROGRESS,label,Math.round(progress)));
    }catch(Exception e){
      pool.shutdownNow();
      throw new RuntimeException(e);
    }
    s.fix();
    pool.shutdown();
    return true;
  }

  static void startthread(){
    var thread=new WorldGenerator();
    thread.start();
    WORLDTHREADS.add(thread);
  }

  /**
   * Multi-threaded steps to generate {@link World} and {@link Dungeon}s.
   * Reports on progress through an {@link InfoScreen}. Blocks synchronously.
   *
   * @see LocationGenerator
   * @see Dungeon#generate()
   */
  public static void build(){
    var header=String.format(PROGRESSHEADER,NTHREADS);
    var s=new ProgressScreen(header);
    working=true;
    while(true) try{
      generateworld(s);
      s.fix();
      generatedungeons("Generating dungeons",s);
      generatedungeons("Generating branches",s);
      while(generatedungeons("Generating more branches",s)){
        //continue
      }
      working=false;
      return;
    }catch(Exception e){
      World.seed=null;
      s.reset();
      continue;
    }
  }
}
