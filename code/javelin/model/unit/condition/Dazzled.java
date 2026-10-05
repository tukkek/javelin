package javelin.model.unit.condition;

import javelin.controller.content.quality.perception.Perception;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Minimal visual impairment.
 *
 * @author alex
 */
public class Dazzled extends Condition{
  /** –1 penalty on attack rolls and {@link Perception}. */
  public static final int PENALTY=-1;

  /** Constructor. */
  public Dazzled(Spell s,float expireat){
    super("dazzled",s.level,s.casterlevel,expireat,null,Effect.NEGATIVE);
  }

  @Override
  public void start(Combatant c){
    raiseallattacks(c.source,-1,0);
  }

  @Override
  public void end(Combatant c){
    raiseallattacks(c.source,+1,0);
  }
}
