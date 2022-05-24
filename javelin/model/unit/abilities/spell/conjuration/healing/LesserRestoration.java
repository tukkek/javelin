package javelin.model.unit.abilities.spell.conjuration.healing;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.abilities.spell.abjuration.DispelMagic;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.condition.Exhausted;
import javelin.model.unit.condition.Fatigued;
import javelin.model.unit.condition.Poisoned;
import javelin.view.mappanel.battle.overlay.AiOverlay;

/**
 * http://www.d20srd.org/srd/spells/restorationLesser.htm
 *
 * @author alex
 */
public class LesserRestoration extends Touch{
  static final ArrayList<Class<? extends Condition>> CONDITIONS=new ArrayList<>();

  static{
    CONDITIONS.add(Poisoned.class);
    CONDITIONS.add(Fatigued.class);
  }

  /** Constructor. */
  public LesserRestoration(){
    super("Lesser restoration",2,ChallengeCalculator.ratespell(2));
    ispotion=true;
    castinbattle=true;
    castoutofbattle=true;
    isritual=true;
    castonallies=true;
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    if(cn!=null) cn.overlay=new AiOverlay(target);
    return castpeacefully(caster,target);
  }

  @Override
  public String castpeacefully(Combatant caster,Combatant target){
    var dispelled=new ArrayList<Condition>();
    remove(Fatigued.class,target,dispelled);
    var exhausted=remove(Exhausted.class,target,dispelled);
    if(exhausted!=null)
      target.addcondition(new Fatigued(null,exhausted.longterm));
    if(target.source.poison>0){
      target.detox(1);
      return target+" heals 2 constitution damage!";
    }
    for(Class<? extends Condition> c:CONDITIONS) remove(c,target,dispelled);
    return DispelMagic.print(dispelled,target);
  }

  Condition remove(Class<? extends Condition> c,Combatant target,
      ArrayList<Condition> dispelled){
    Condition has=target.hascondition(c);
    if(has==null) return null;
    target.removecondition(has);
    dispelled.add(has);
    return has;
  }

  @Override
  public void filter(Combatant combatant,List<Combatant> targets,BattleState s){
    super.filter(combatant,targets,s);
    for(Combatant c:new ArrayList<>(targets))
      if(!combatant.isally(c,s)) targets.remove(c);
  }

  @Override
  public boolean canheal(Combatant c){
    var conditions=new ArrayList<>(CONDITIONS);
    conditions.add(Fatigued.class);
    for(Class<? extends Condition> condition:conditions)
      if(c.hascondition(condition)!=null) return true;
    return c.source.poison>0;
  }
}
