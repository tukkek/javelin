package javelin.model.world.location.town.diplomacy.quest.kill;

import javelin.controller.comparator.ActorByDistance;
import javelin.model.world.location.Location;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.model.world.location.town.labor.Trait;

/**
 * Clear a {@link Haunt}.
 *
 * @see Trait#MILITARY
 * @author alex
 */
public class Raid extends KillQuest{
  Location target;

  /** Constructor. */
  public Raid(){
    duration=SHORT;
  }

  @Override
  protected void define(Town t){
    super.define(t);
    var comparator=new ActorByDistance(town);
    var targets=Haunt.gethaunts().stream()
        .filter(h->h.ishostile()&&challenge(h.getel(el)))
        .sorted(comparator::compare).toList();
    if(targets.isEmpty()) return;
    target=Quest.select(targets);
    name="Clear %s %s".formatted(target,Quest.locate(target));
  }

  @Override
  public boolean validate(){
    return super.validate()&&target!=null;
  }

  @Override
  protected boolean complete(){
    return !target.ishostile();
  }
}
