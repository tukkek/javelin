package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.AreaSpell;

/**
 * Produces a vertical column of divine fire.
 *
 * @author alex
 */
public class FlameStrike extends AreaSpell{
  /** Constructor. */
  public FlameStrike(){
    super("Flame strike",5,ChallengeCalculator.ratespell(5),10);
    castinbattle=true;
    iswand=true;
    isrod=true;
  }

  @Override
  protected String affect(Combatant target,Combatant caster,BattleState s){
    var saved=getsavetarget(target.source.ref,caster)<=10;
    var damage=Math.min(casterlevel,15)*6/2;
    if(saved) damage/=2;
    target.damage(damage/2,target.source.energyresistance,s);
    target.damage(damage/2,0,s);
    var effect=saved?"%s resists the flames!":"%s is engulfed in flames!";
    return effect.formatted(target);
  }
}
