package javelin.model.world.location.town.diplomacy.quest.fetch;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.ActorByDistance;
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

  static String describe(Item i,DungeonFloor f){
    return "Fetch %s from %s".formatted(i.name.toLowerCase(),f);
  }

  @Override
  protected void define(Town t){
    super.define(t);
    var floor=search(type,el,t);
    if(floor==null) return;
    goal=RPG.pick(RPG.pick(floor.features.getall(type)).items);
    gold+=goal.price;
    name=describe(goal,floor);
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

  /** Helper to test if {@link #goal}s can be found in all ELs. */
  static public String test(Class<? extends Chest> type,int el,Town t){
    var floor=search(type,el,t);
    if(floor==null) return null;
    var i=floor.features.get(type).items.get(0);
    return describe(i,floor);
  }
}
