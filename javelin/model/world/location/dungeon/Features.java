package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.rare.Spirit;
import javelin.old.RPG;
import javelin.view.screen.DungeonScreen;

public class Features implements Iterable<Feature>,Serializable{
  /** Much faster, may help drawing {@link DungeonScreen}. */
  HashMap<Point,Feature> placement=new HashMap<>();
  DungeonFloor dungeon;

  public Features(DungeonFloor d){
    dungeon=d;
  }

  @Override
  public Iterator<Feature> iterator(){
    return placement.values().iterator();
  }

  public boolean isEmpty(){
    return placement.isEmpty();
  }

  public void add(Feature f){
    if(Javelin.DEBUG&&f.getlocation()==null) throw new NullPointerException();
    placement.put(f.getlocation(),f);
  }

  public ArrayList<Feature> copy(){
    return new ArrayList<>(placement.values());
  }

  public void remove(Feature f){
    placement.remove(f.getlocation());
  }

  public <K extends Feature> K get(Class<K> type){
    var all=getall(type);
    return all.isEmpty()?null:all.get(0);
  }

  public <K extends Feature> List<K> getall(Class<K> type){
    return placement.values().stream().filter(f->type.isInstance(f))
        .map(f->(K)f).collect(Collectors.toList());
  }

  public Feature get(int x,int y){
    return placement.get(new Point(x,y));
  }

  public List<Feature> getallundiscovered(){
    return placement.values().stream().filter(
        f->(!dungeon.visible[f.x][f.y]||!f.draw)&&!(f instanceof Decoration))
        .toList();
  }

  /**
   * TODO return a list so that {@link Spirit} can show the closest one. It'd be
   * more versatile anyway.
   */
  public Feature getundiscovered(){
    var undiscovered=getallundiscovered();
    return undiscovered.isEmpty()?null:RPG.pick(undiscovered);
  }

  public boolean has(Class<? extends Feature> feature){
    return dungeon.features.get(feature)!=null;
  }

  public void getknown(){
    var knowledge=Squad.active.getbest(Skill.KNOWLEDGE)
        .taketen(Skill.KNOWLEDGE);
    var reveal=knowledge-(10+dungeon.level);
    while(dungeon.knownfeatures<reveal){
      dungeon.knownfeatures+=1;
      var f=getundiscovered();
      if(f!=null) dungeon.discover(f);
    }
  }

  /** @return A stream for functional processing. */
  public Stream<Feature> stream(){
    return placement.values().stream();
  }

  public int size(){
    return placement.size();
  }

  /** @return A copy of the internal Feature list. */
  public List<Feature> getall(){
    return new ArrayList<>(placement.values());
  }
}
