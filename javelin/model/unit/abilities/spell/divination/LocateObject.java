package javelin.model.unit.abilities.spell.divination;

import java.util.stream.Collectors;

import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.rare.Spirit;

/**
 * Allows player to find nearest treasure chest in a {@link DungeonFloor}. If no
 * chest is present, reveals the closest {@link Feature}.
 */
public class LocateObject extends Spell{
  /** Constructor. */
  public LocateObject(){
    super("Locate object",2,ChallengeCalculator.ratespell(2));
    castinbattle=false;
    castoutofbattle=true;
  }

  @Override
  public boolean validate(Combatant caster,Combatant target){
    return Dungeon.active!=null;
  }

  @Override
  public String castpeacefully(Combatant caster,Combatant target){
    var closest=find();
    if(closest==null)
      return "There doesn't seem to be anything else of interest here...";
    Spirit.reveal("A point of interest is revealed!",closest);
    return null;
  }

  /** @return Closest treasure chest. */
  public static Feature find(){
    var hero=JavelinApp.context.getsquadlocation();
    var undiscovered=Dungeon.active.features.getallundiscovered().stream()
        .sorted((a,
            b)->(int)(Walker.distance(hero.x,hero.y,a.x,a.y)
                -Walker.distance(hero.x,hero.y,b.x,b.y)))
        .collect(Collectors.toList());
    if(undiscovered.isEmpty()) return null;
    for(var f:undiscovered) if(f instanceof Chest) return f;
    for(var f:undiscovered) if(!(f instanceof Decoration)) return f;
    return undiscovered.get(0);
  }
}
