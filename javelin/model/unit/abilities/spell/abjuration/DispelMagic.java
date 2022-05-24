package javelin.model.unit.abilities.spell.abjuration;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.condition.Condition;

/**
 * http://www.d20srd.org/srd/spells/dispelMagicGreater.htm
 *
 * @author alex
 */
public class DispelMagic extends Spell{
  /** Constructor. */
  public DispelMagic(){
    super("Greater dispel magic",6,ChallengeCalculator.ratespell(6));
    castoutofbattle=true;
    castinbattle=true;
    isritual=true;
    ispotion=true;
    iswand=true;
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    if(target.summoned){
      var name=target.source.name;
      var summon=Summon.SUMMONS.stream()
          .filter(spell->spell.monstername.equalsIgnoreCase(name)).findAny()
          .orElse(null);
      if(Javelin.DEBUG&&summon==null)
        throw new RuntimeException("No summon for: "+name);
      if(summon!=null&&casterlevel>summon.casterlevel){
        s.remove(target);
        return target+" goes back to its plane of existence!";
      }
    }
    var dispelled=new ArrayList<Condition>();
    for(var c:target.getconditions())
      if(c.casterlevel!=null&&casterlevel>c.casterlevel){
        c.dispel();
        target.removecondition(c);
        dispelled.add(c);
      }
    return print(dispelled,target);
  }

  /**
   * @return A formatted message informing dispelled conditions, or a proper
   *   message if given list is empty.
   */
  static public String print(List<Condition> dispelled,Combatant c){
    if(dispelled.isEmpty())
      return "No conditions were dispelled for %s...".formatted(c);
    var result="";
    for(Condition d:dispelled) result+=d.toString()+", ";
    return "The following conditions are dispelled for %s: %s!".formatted(c,
        result.substring(0,result.length()-2));
  }

  @Override
  public String castpeacefully(Combatant caster,Combatant target){
    return cast(caster,target,true,null,null);
  }

  @Override
  public void filter(Combatant combatant,List<Combatant> targets,BattleState s){
    // cast on all targets
  }
}
