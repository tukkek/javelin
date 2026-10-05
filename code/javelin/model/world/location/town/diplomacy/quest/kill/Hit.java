package javelin.model.world.location.town.diplomacy.quest.kill;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.ActorsByDistance;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Inhabitant;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Prisoner;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * Kill an {@link Inhabitant}.
 *
 * @see Trait#CRIMINAL
 * @author alex
 */
public class Hit extends KillQuest{
  record Target(Inhabitant npc,DungeonFloor floor) implements Serializable{
    //
  }

  Target target;

  Target find(Town t){
    var fixed=DungeonEntrance.getfixed();
    fixed.sort(new ActorsByDistance(t));
    var targets=new ArrayList<Target>();
    for(var entrance:fixed) for(var f:entrance.dungeon.floors)
      for(var i:f.features.getall(Inhabitant.class)){
        var challenge=Math.max(f.level,i.inhabitant.source.cr);
        if(challenge+Difficulty.EASY<=el&&el<challenge+Difficulty.DEADLY)
          targets.add(new Target(i,f));
      }
    return targets.isEmpty()?null:RPG.select(targets);
  }

  @Override
  protected void define(Town t){
    super.define(t);
    target=find(t);
    if(target==null) return;
    var d=target.floor.dungeon;
    var action=target.npc instanceof Prisoner?"Free":"Kill";
    name="%s %s inside %s".formatted(action,target.npc,d.entrance);
    if(!(d instanceof Wilderness)) name+=" "+locate(d.entrance);
  }

  @Override
  public boolean validate(){
    return super.validate()&&target!=null;
  }

  @Override
  protected boolean complete(){
    return !target.floor.features.getall().contains(target.npc);
  }
}
