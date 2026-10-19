package javelin.model.unit.abilities.spell.evocation;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.AreaSpell;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Dazzled;
import javelin.model.world.Period.Time;
import javelin.old.RPG;

/**
 * Makes creatures in a 10-feet radius {@link Dazzled}.
 *
 * This is very weak so apply a persistent metamagic for save
 * {@link RPG#DISADVANTAGE} and +2 to {@link Spell#level}.
 *
 * @author alex
 */
public class FlareBurst extends AreaSpell{
  /// Constructor.
  public FlareBurst(){
    var level=1+2;
    var cr=ChallengeCalculator.ratespell(level);
    super("Flare burst",level,cr,10);
    castinbattle=true;
  }

  @Override
  public int save(Combatant caster,Combatant target){
    var save=target.source.getfortitude();
    if(save!=Integer.MAX_VALUE) save+=RPG.DISADVANTAGE;
    return getsavetarget(save,caster);
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
