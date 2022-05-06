package javelin.model.world.location.town.diplomacy.quest.fetch;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.ActorByDistance;
import javelin.controller.content.terrain.Terrain;
import javelin.model.item.Item;
import javelin.model.item.precious.PreciousObject;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.old.RPG;

/**
 * A {@link Location}-based quest: players go into a {@link Dungeon}, find a
 * {@link PreciousObject} and deliver it to {@link Town}.
 *
 * @author alex
 */
public abstract class FetchQuest extends Quest{
  /** If <code>true</code>, add {@link Item#price} to {@link #gold}. */
  protected boolean sellitem=true;

  Class<? extends Chest> type;
  Item goal;

  /** Constructor. */
  public FetchQuest(Class<? extends Chest> goal){
    type=goal;
    duration=LONG;
  }

  static DungeonFloor search(Class<? extends Chest> type,int el,Town t){
    var dungeons=new ArrayList<>(DungeonEntrance.getdungeons());
    dungeons.addAll(Wilderness.getwildernesses());
    var sort=new ActorByDistance(t);
    var floors=dungeons.stream().sorted((a,b)->sort.compare(a,b))
        .flatMap(d->d.dungeon.floors.stream())
        .filter(f->el+Difficulty.VERYEASY<f.level&&f.level<el+Difficulty.DEADLY
            &&f.features.get(type)!=null)
        .collect(toList());
    return floors.isEmpty()?null:floors.get(0);
  }

  static String describe(Item i,DungeonFloor f,boolean showterrain){
    var name="Fetch %s from %s".formatted(i.name.toLowerCase(),f);
    if(showterrain){
      var l=f.dungeon.entrance.getlocation();
      name+=" in the "+Terrain.get(l.x,l.y).toString().toLowerCase();
    }
    return name;
  }

  @Override
  protected void define(Town t){
    super.define(t);
    var floor=search(type,el,t);
    if(floor==null) return;
    goal=RPG.pick(RPG.pick(floor.features.getall(type)).items);
    if(sellitem) gold+=goal.price;
    name=describe(goal,floor,!(floor.dungeon instanceof Wilderness));
  }

  @Override
  public boolean validate(){
    return super.validate()&&goal!=null;
  }

  @Override
  protected boolean complete(){
    var s=Squad.active;
    return s!=null&&town.getdistrict().getsquads().contains(s)
        &&s.equipment.remove(goal)!=null;
  }

  static String test(int el,Class<? extends Chest> type,Town t){
    var floor=search(type,el,t);
    if(floor==null) return "";
    var c=floor.features.get(type);
    return describe(c.items.get(0),floor,false);
  }

  /** Helper to test if {@link #goal}s can be found in all ELs. */
  static public String test(Class<? extends Chest> type,Town t){
    var results=new ArrayList<String>(20);
    for(var population=1;population<=20;population++)
      results.add("EL%s: %s".formatted(population,test(population,type,t)));
    return String.join("\n",results);
  }
}
