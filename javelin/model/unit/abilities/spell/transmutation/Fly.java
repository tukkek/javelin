package javelin.model.unit.abilities.spell.transmutation;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.abilities.spell.transmutation.OverlandFlight.Flight;

/**
 * See the d20 SRD for more info.
 */
public class Fly extends Touch{
  /** Effect description. */
  public static final String RESULT="%s floats above the ground!";

  static class Flying extends Flight{
    public Flying(Spell s){
      super(60,s,Float.MAX_VALUE,null);
    }
  }

  /** Constructor. */
  public Fly(){
    super("Fly",3,ChallengeCalculator.ratespell(3));
    castinbattle=true;
    castonallies=true;
    ispotion=true;
    isrune=new Flying(this);
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    target.addcondition(new Flying(this));
    return RESULT.formatted(target);
  }
}
