package javelin.model.unit.abilities.spell.divination;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.action.world.meta.help.Guide;
import javelin.model.unit.abilities.spell.Spell;

/**
 * @see Guide#SPELLS
 * @author alex
 */
public class ReadMagic extends Spell{
  /** Constructor. */
  public ReadMagic(){
    super("Read magic",0,ChallengeCalculator.ratespell(0));
  }
}
