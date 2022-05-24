package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.AreaSpell;
import javelin.model.unit.condition.Stunned;
import javelin.old.RPG;

/**
 * Blasts an area with a tremendous cacophony.
 *
 * @author alex
 */
public class SoundBurst extends AreaSpell{
  /** Constructor. */
  public SoundBurst(){
    super("Sound burst",2,ChallengeCalculator.ratespell(2),10);
    castinbattle=true;
  }

  @Override
  protected String affect(Combatant target,Combatant caster,BattleState s){
    target.damage(RPG.average(1,8),0,s);
    if(getsavetarget(target.source.getfortitude(),caster)<=10)
      return target+" resists.";
    target.addcondition(new Stunned(target,this));
    return target+" is stunned!";
  }
}
