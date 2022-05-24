package javelin.model.item.consumable.potion;

import java.util.HashMap;
import java.util.Map;

import javelin.Javelin;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;

/**
 * A {@link Item#consumable} that affects everyone in a 10-foot cloud.
 *
 * @author alex
 */
public class Vaporizer extends Potion{
  static final Map<Integer,Integer> PRICES=new HashMap<>(3);

  static{
    PRICES.put(1,2200);
    PRICES.put(2,3000);
    PRICES.put(3,3800);
  }

  /** @see Potion#Potion(Spell) */
  public Vaporizer(Spell s){
    super(s);
    price+=PRICES.get(s.level);
    name="Vaporizer of %s".formatted(s.name.toLowerCase());
  }

  @Override
  public boolean use(Combatant user){
    // TODO
  }

  @Override
  public boolean usepeacefully(Combatant user){
    if(Squad.active.members.size()>12){
      Javelin.message("Squad is too big to be affected by the vapors...",true);
      return false;
    }
    // TODO
  }

  /** @return <code>true</code> if this is a valid Vaporizer. */
  public static boolean validate(Spell s){
    return s.ispotion&&PRICES.get(s.level)!=null;
  }
}
