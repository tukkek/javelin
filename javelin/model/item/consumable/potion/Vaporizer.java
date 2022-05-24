package javelin.model.item.consumable.potion;

import static java.util.stream.Collectors.joining;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.exception.RepeatTurn;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.AreaSpell;
import javelin.model.unit.abilities.spell.Spell;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * A {@link Item#consumable} that affects everyone in a 10-foot cloud.
 *
 * @author alex
 */
public class Vaporizer extends Potion{
  static final String CONFIRM="""
      Do you want to use the %s at this location?

      Press ENTER to confirm or any other key to cancel...
      """;
  static final String OUTCOME="""
      %s uses the %s!
      %s
      """;
  static final int TARGETS=12;
  static final String CHOOSE="Select %s units to be affected by the %s:";

  /** @see Potion#Potion(Spell) */
  public Vaporizer(Spell s){
    super("Vaporizer",s,price(s.level+4,s.casterlevel+8),true);
    if(!s.ispotion)
      throw new IllegalArgumentException(s.name+" is not a vaporizer!");
    targeted=false;
  }

  @Override
  public boolean use(Combatant user){
    var a=AreaSpell.getarea(user.getlocation(),10);
    MapPanel.overlay=new AiOverlay(a);
    Javelin.redraw();
    if(Javelin.prompt(CONFIRM,true)!='\n') throw new RepeatTurn();
    var effect=AreaSpell.getcombatants(a,Fight.state).stream()
        .map(c->spell.cast(null,c,false,Fight.state,null))
        .collect(joining(" "));
    Javelin.redraw();
    Javelin.prompt(OUTCOME.formatted(user,toString().toLowerCase(),effect));
    return true;
  }

  @Override
  public boolean usepeacefully(Combatant user){
    var targets=Squad.active.members;
    if(targets.size()>TARGETS){
      targets=choose();
      if(targets==null) return false;
    }
    var effect=targets.stream().map(t->spell.castpeacefully(null,t))
        .collect(joining(" "));
    Javelin.prompt(effect,true);
    return true;
  }

  Combatants choose(){
    var targets=new Combatants(TARGETS);
    while(targets.size()<TARGETS){
      var choices=new Combatants(Squad.active.members);
      choices.removeAll(targets);
      var descriptions=choices.stream()
          .map(c->"%s (%s)".formatted(c,c.printstatus(null))).toList();
      var prompt=CHOOSE.formatted(TARGETS-targets.size(),this);
      var choice=Javelin.choose(prompt,descriptions,true,false);
      if(choice<0) return null;
      targets.add(choices.get(choice));
    }
    return targets;
  }
}
