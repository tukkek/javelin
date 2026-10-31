package javelin.test;

import java.util.ArrayList;

import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;

/// Generates {@link RandomDungeonEncounter}s for all {@link DungeonFloor}s.
public class DungeonEncounters extends Test{
  @Override
  public void run(){
    var dungeons=new ArrayList<>(DungeonEntrance.getdungeonsandtemples()
        .stream().map(entrance->entrance.dungeon).toList());
    dungeons
        .addAll(dungeons.stream().flatMap(d->d.branch(true).stream()).toList());
    var floors=dungeons.stream().flatMap(dungeon->dungeon.floors.stream())
        .toList();
    for(var f:floors){
      say(f.toString());
      for(var i=0;i<100;i+=1)
        if(new RandomDungeonEncounter(f).enemies.isEmpty())
          fail("No encounter for %s".formatted(f));
    }
  }
}
