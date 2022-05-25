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
  public int save(Combatant caster,Combatant target){
    return getsavetarget(target.source.getfortitude(),caster);
  }

  @Override
  protected String affect(Combatant target,boolean saved,Combatant caster,
      BattleState s){
    target.damage(RPG.average(1,8),0,s);
    if(saved) return target+" resists.";
    target.addcondition(new Stunned(target,this));
    return target+" is stunned!";
  }
}
