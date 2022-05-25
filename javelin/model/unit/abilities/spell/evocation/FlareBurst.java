package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.AreaSpell;
import javelin.model.unit.condition.Dazzled;
import javelin.model.world.Period.Time;

/**
 * Makes creatures in a 10-feet radius {@link Dazzled}.
 *
 * @author alex
 */
public class FlareBurst extends AreaSpell{
  /** Constructor. */
  public FlareBurst(){
    super("Flare burst",1,ChallengeCalculator.ratespell(1),10);
    castinbattle=true;
  }

  @Override
  public int save(Combatant caster,Combatant target){
    return getsavetarget(target.source.getfortitude(),caster);
  }

  @Override
  protected String affect(Combatant target,boolean saved,Combatant caster,
      BattleState s){
    if(saved) return target+" resists.";
    var ap=caster==null?target.ap:caster.ap;
    target.addcondition(new Dazzled(this,ap+Time.MINUTE));
    return target+"is dazzled!";
  }
}
