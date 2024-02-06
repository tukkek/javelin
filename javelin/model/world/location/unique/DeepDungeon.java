package javelin.model.world.location.unique;

import javelin.controller.challenge.Tier;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.model.world.location.dungeon.feature.door.trap.Alarm;
import javelin.model.world.location.dungeon.feature.rare.LearningStone;
import javelin.old.RPG;

/**
 * A {@link DungeonFloor} from {@link Tier#LOW} to {@link Tier#HIGH}, mainly for
 * roguelike-oriented players (or play sessions) who just want to dungeon crawl
 * and not bother with anything else.
 *
 * TODO should probably offer something cooler at the end of it? Maybe make it
 * one of the ways to win the game?
 *
 * @author alex
 */
public class DeepDungeon extends Dungeon{
  static final String DESCRIPTION="Deep dungeon";

  /** @see Location */
  public static class DeepDungeonEntrance extends DungeonEntrance{
    /** Constructor. */
    public DeepDungeonEntrance(DeepDungeon d){
      super(d);
    }

    @Override
    protected void generate(boolean water){
      //handled by LocationGenerator
    }
  }

  class DeepDungeonFloor extends DungeonFloor{
    DeepDungeonFloor(Integer level,Dungeon d){
      super(level,d);
    }

    @Override
    public DungeonTier gettier(){
      return DungeonTier.get(level);
    }

    @Override
    protected void generatefeatures(int nfeatures,DungeonZoner zoner){
      super.generatefeatures(nfeatures,zoner);
      while(RPG.chancein(2)) zoner.place(new LearningStone(this),this);
      for(var d:features.getall(Door.class)) if(d.stuck){
        d.stuck=false;
        d.trap=Alarm.INSTANCE;
      }
    }
  }

  /** Constructor. */
  public DeepDungeon(){
    super(DESCRIPTION,Tier.LOW.minlevel,Tier.EPIC.maxlevel);
    images=new DungeonImages(DungeonTier.TEMPLE);
    goals=null;
  }

  @Override
  public String getimagename(){
    return "deepdungeon";
  }

  @Override
  protected DungeonFloor createfloor(int level){
    return new DeepDungeonFloor(level,this);
  }

  @Override
  protected synchronized String baptize(String suffix){
    return name;
  }
}
