package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/** Power word: kill. */
public class Kill extends Spell{
  /** Constructor. */
  public Kill(){
    super("Kill",9,ChallengeCalculator.ratespell(9));
    castinbattle=true;
    iswand=true;
    isrod=true;
  }

  boolean validate(Combatant t){
    return t.hp<=100;
  }

  @Override
  public void filter(Combatant caster,List<Combatant> targets,BattleState s){
    super.filter(caster,targets,s);
    targets.retainAll(targets.stream().filter(this::validate).toList());
  }

  @Override
  public int save(Combatant caster,Combatant target){
    return Integer.MAX_VALUE;
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    if(!validate(target)) return "%s is unnafected.".formatted(target);
    target.damage(target.maxhp+Math.abs(Combatant.DEADATHP),0,s);
    return "%s is killed by a word of power!".formatted(target);
  }
}
