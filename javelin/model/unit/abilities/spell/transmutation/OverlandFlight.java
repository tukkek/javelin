package javelin.model.unit.abilities.spell.transmutation;

import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.condition.Condition;
import javelin.model.unit.feat.Feat;

/**
 * {@link Monster#fly} at 35-feet speed for 1h/level. To reach a 24h duration
 * it's assumed to have the Extend Spell metamagic {@link Feat} (+1 level) and
 * cast at level 12, for a doubled duration of 24 hours.
 *
 * @author alex
 */
public class OverlandFlight extends Spell{
  /** An instance of this spell. Do not modify. */
  public static final OverlandFlight INSTANCE=new OverlandFlight();

  static final int SPEED=35;

  /** Grants or enhances {@link Monster#fly}. */
  public static class Flight extends Condition{
    int delta=0;
    int speed;

    /**
     * @see Condition#Condition(String, Integer, Integer, float, Integer,
     *   Effect)
     */
    public Flight(int speed,Spell s,float expiration,Integer longterm){
      super("Flying",s.level,s.casterlevel,expiration,longterm,Effect.POSITIVE);
      this.speed=speed;
    }

    @Override
    public void start(Combatant c){
      delta=speed-c.source.fly;
      if(delta<0) delta=0;
      c.source.fly+=delta;
    }

    @Override
    public void end(Combatant c){
      c.source.fly-=delta;
    }
  }

  /** Constructor. */
  public OverlandFlight(){
    super("Overland flight",6,ChallengeCalculator.ratespell(6,12));
    casterlevel=12;
    castinbattle=true;
    castonallies=true;
    castoutofbattle=true;
    ispotion=true;
    isscroll=true;
    isritual=true;
  }

  @Override
  public void filter(Combatant cp,List<Combatant> targets,BattleState s){
    super.filter(cp,targets,s);
    var ineffectual=targets.stream().filter(t->t.source.fly>=SPEED).toList();
    targets.removeAll(ineffectual);
  }

  @Override
  public String castpeacefully(Combatant caster,Combatant target){
    target.addcondition(new Flight(SPEED,this,Float.MAX_VALUE,24));
    return Fly.RESULT.formatted(target);
  }

  @Override
  public String cast(Combatant caster,Combatant target,boolean saved,
      BattleState s,ChanceNode cn){
    return castpeacefully(caster,target);
  }
}
