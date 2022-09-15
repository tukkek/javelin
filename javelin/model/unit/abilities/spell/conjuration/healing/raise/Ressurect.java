package javelin.model.unit.abilities.spell.conjuration.healing.raise;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;

/**
 * As {@link RaiseDead} but at full {@link Combatant#hp};
 *
 * @author alex
 */
public class Ressurect extends RaiseDead{
  static final float CR=ChallengeCalculator.ratespell(7)+RESTORATIONCR;

  /** Constructor. */
  public Ressurect(){
    super("Ressurection",7,CR);
    components=10000;
    castinbattle=false;
  }

  @Override
  public String castpeacefully(Combatant caster,Combatant target){
    target.hp=target.maxhp;
    return null;
  }
}
