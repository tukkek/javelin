package javelin.controller.content.map.terrain.marsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.challenge.Tier;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.map.Section;
import javelin.controller.content.map.Section.Sections;
import javelin.controller.content.map.Section.ThinSection;
import javelin.controller.content.map.location.town.TownMap;
import javelin.controller.content.map.terrain.underground.DwarvenCave;
import javelin.controller.content.terrain.Marsh;
import javelin.controller.exception.GaveUp;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.Images;

/** @author alex */
public class MarshShore extends Map{
  static final HashMap<Tier,Double> OCCUPATION=new HashMap<>();
  static final int SIZE=DndMap.SIZE;

  static{
    OCCUPATION.put(Tier.LOW,.1);
    OCCUPATION.put(Tier.MID,.3);
    OCCUPATION.put(Tier.HIGH,.4);
    OCCUPATION.put(Tier.EPIC,.65);
  }

  /**
   * TODO register, test
   *
   * @see Marsh
   * @see Town
   * @author alex
   */
  public static class MarshTown extends MarshShore{
    double occupation;

    /** Constructor. */
    public MarshTown(Tier t){
      wall=TownMap.WALL;
      innerarea=SIZE*SIZE/(Tier.EPIC.equals(t)||Tier.HIGH.equals(t)?2:3);
      occupation=OCCUPATION.get(t);
    }

    @Override
    void drawinside(Set<Point> path){
      var seeds=Math.round(Math.round(SIZE*SIZE*occupation/10));
      Sections sections=null;
      while(sections==null) try{
        sections=new Sections(Section.class,this);
        sections.segment(seeds,occupation,path);
        for(var s:sections) for(var a:s.area) map[a.x][a.y].blocked=true;
      }catch(GaveUp e){
        continue;
      }
    }
  }

  int innerarea=SIZE*SIZE/4;

  /** Constructor. */
  public MarshShore(){
    super("Marsh shore",SIZE,SIZE);
    obstacle=Images.get(List.of("terrain","bush2"));
  }

  Set<Point> walk(Point p){
    p=new Point(p);
    var path=new HashSet<Point>();
    while(p.validate(SIZE,SIZE)){
      path.add(new Point(p));
      p.displace();
    }
    return path;
  }

  void drawinside(Set<Point> path){
    for(var p:Point.getrange(SIZE,SIZE)){
      var m=map[p.x][p.y];
      if(m.flooded&&RPG.chancein(100)){
        m.flooded=false;
        m.obstructed=true;
      }
    }
    Sections patches=null;
    while(patches==null) try{
      var npatches=RPG.randomize(6,0,Integer.MAX_VALUE);
      patches=new Sections(ThinSection.class,this);
      patches.segment(npatches,.05,path);
      for(var p:patches) for(var a:p.area) map[a.x][a.y].obstructed=true;
    }catch(GaveUp e){
      continue;
    }
  }

  @Override
  public void generate(){
    var seeds=SIZE*4/3;
    var border=DwarvenCave.occupyborder(seeds,seeds*3);
    for(var b:border) map[b.x][b.y].obstructed=true;
    for(var p:Point.getrange(SIZE,SIZE))
      if(!border.contains(p)) map[p.x][p.y].flooded=true;
    var path=new HashSet<Point>(innerarea);
    var from=SIZE*1/4;
    var to=SIZE*3/4;
    path.add(new Point(RPG.r(from,to),RPG.r(from,to)));
    while(path.size()<innerarea)
      path.addAll(walk(RPG.pick(new ArrayList<>(path))));
    for(var p:path) if(!border.contains(p)) map[p.x][p.y].flooded=false;
    drawinside(path);
  }
}
