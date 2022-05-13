package javelin.model.item.trigger;

import javelin.controller.content.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Spell-trigger item. Does not {@link Item#provokesaoo}.
 *
 * @author alex
 */
public class TriggerItem extends Item{
  /** Spell to be triggered. */
  protected Spell spell;

  /** Constructor. */
  public TriggerItem(String name,double price,boolean register,Spell s){
    super(name,price,register);
    spell=s.clone();
    spell.provokeaoo=false;
  }

  /** @see #use(Combatant) */
  static public boolean use(Spell s,Combatant user){
    var minimum=user.ap+.5f;
    if(!CastSpell.SINGLETON.cast(s,user)) return false;
    if(user.ap<minimum) user.ap=minimum;
    return true;
  }

  @Override
  public boolean use(Combatant user){
    return use(spell,user);
  }
}
