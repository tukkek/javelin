package javelin.model.world.location.town.diplomacy.quest.kill;

import java.util.ArrayList;
import java.util.List;

import javelin.model.world.location.ContestedTerritory;
import javelin.model.world.location.Fortification;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.quest.Quest;
import javelin.model.world.location.town.labor.Trait;

/**
 * Clear a {@link Haunt} or {@link ContestedTerritory}.
 *
 * @see Trait#MILITARY
 * @author alex
 */
public class Raid extends KillQuest{
  Fortification target;

  /** Constructor. */
  public Raid(){
    duration=SHORT;
  }

  static List<? extends Fortification> gettargets(int el){
    return List.of(Haunt.gethaunts(),ContestedTerritory.getall()).stream()
        .flatMap(List::stream)
        .filter(t->t.ishostile()&&challenge(el,t.getel())).toList();
  }

  @Override
  protected void define(Town t){
    super.define(t);
    var targets=gettargets(el);
    if(targets.isEmpty()) return;
    target=select(targets);
    var territory=target.descriptionknown.toLowerCase();
    name="Clear %s, %s".formatted(territory,Quest.locate(target));
  }

  @Override
  public boolean validate(){
    return super.validate()&&target!=null;
  }

  @Override
  protected boolean complete(){
    return !target.ishostile();
  }

  /** @return {@link Town}s with valid target EL (or <code>null</code>). */
  public static String test(){
    var towns=Town.gettowns();
    var output=new ArrayList<String>(towns.size());
    for(var t:towns){
      var targets=gettargets(t.population);
      var el=targets.isEmpty()?null:targets.get(0).getel();
      output.add("%s: raid EL %s".formatted(t,el));
    }
    return String.join("\n",output);
  }
}
