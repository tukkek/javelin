package javelin.model.unit.abilities.spell.divination;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.action.world.meta.help.Guide;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Spellcraft;

/**
 * Bumped to {@link Spell#level} 1 to circumvent the "once a day" limit;
 *
 * @see Guide#SPELLS
 * @author alex
 */
public class DetectMagic extends Spell{
  /** @see Spellcraft */
  public static final int DC=15;

  /** Constructor. */
  public DetectMagic(){
    super("Detect magic",0,ChallengeCalculator.ratespell(1,3));
  }

  @Override
  public boolean apply(Combatant c){
    return c.spells.get(this)!=null&&super.apply(c);
  }
}
