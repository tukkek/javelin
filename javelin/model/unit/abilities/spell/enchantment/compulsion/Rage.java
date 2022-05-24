package javelin.model.unit.abilities.spell.enchantment.compulsion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;

/**
 * +2 to strengh, constitution, AC and +1 to will.
 *
 * @author alex
 */
public class Rage extends Spell{
  class Raging extends Condition{
    Raging(float expireatp,Spell s){
      super("raging",s,expireatp,Effect.POSITIVE);
    }

    @Override
    public void start(Combatant c){
      var m=c.source;
      m.changestrengthmodifier(+1);
      m.changeconstitutionmodifier(c,+1);
      m.addwill(+1);
      c.acmodifier-=2;
    }

    @Override
    public void end(Combatant c){
      var m=c.source;
      m.changestrengthmodifier(-1);
      m.changeconstitutionmodifier(c,-1);
      m.addwill(-1);
      c.acmodifier+=2;
    }
  }

  /** Constructor. */
  public Rage(){
    this("Rage",3,ChallengeCalculator.ratespell(3));
    ispotion=true;
    iswand=true;
    isrune=new Raging(Float.MAX_VALUE,this);
  }

  /** Subclass constructor. */
  protected Rage(String name,int levelp,float incrementcost){
    super(name,levelp,incrementcost);
    castinbattle=true;
    castonallies=true;
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    var ap=caster==null?target.ap:caster.ap;
    target.addcondition(new Raging(ap+getduration(target),this));
    return target+" is raging!";
  }

  float getduration(Combatant target){
    return Math.max(1,4+Monster.getbonus(target.source.constitution));
  }
}
