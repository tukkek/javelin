package javelin.model.world.location.town.diplomacy.quest.find;

import java.util.List;

import javelin.controller.comparator.ActorByDistance;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.branch.temple.Temple;
import javelin.model.world.location.dungeon.branch.temple.Temple.TempleEntrance;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Find an undiscovered {@link Town}, {@link Temple} or {@link Haunt}.
 *
 * @see Trait#EXPANSIVE
 * @see WorldScreen#discover(int, int)
 * @author alex
 */
public class Discover extends FindQuest{
  static final List<Class<? extends Actor>> LOCATIONS=List.of(Town.class,
      TempleEntrance.class,Haunt.class);

  Actor target;

  boolean validate(Actor target){
    if(target.cansee()) return false;
    for(var l:LOCATIONS) if(l.isInstance(target)) return true;
    return false;
  }

  @Override
  protected void define(Town t){
    super.define(t);
    var c=new ActorByDistance(t);
    var targets=RPG.shuffle(World.getactors()).stream().filter(this::validate)
        .sorted(c::compare).toList();
    if(targets.isEmpty()) return;
    target=Quest.select(targets);
    name="Discover %s %s".formatted(target,Quest.locate(target));
  }

  @Override
  public boolean validate(){
    return super.validate()&&target!=null;
  }

  @Override
  protected boolean complete(){
    return target!=null&&target.cansee();
  }

  @Override
  protected String message(){
    return "You have discovered %s!\n".formatted(target)+super.message();
  }
}
