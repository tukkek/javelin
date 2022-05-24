package javelin.model.item.consumable;

import javelin.model.item.Item;
import javelin.model.item.consumable.potion.Vaporizer;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Similar to a {@link Vaporizer} but is a hostile splash weapon instead.
 *
 * @author alex
 */
public class Bomb extends Item{
  /** Constructor. */
  public Bomb(Spell s){
    super("Bomb of "+s.name.toLowerCase(),s.level*s.casterlevel*50,true);
    identified=false;
    usedoutofbattle=false;
  }

  @Override
  public boolean use(Combatant user){
    // TODO
  }
}
