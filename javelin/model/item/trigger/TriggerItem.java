package javelin.model.item.trigger;

import javelin.Javelin;
import javelin.controller.content.action.CastSpell;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;
import javelin.model.unit.skill.UseMagicDevice;

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
    identified=false;
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

  /**
   * As {@link Javelin} doesn't have {@link Spell}-lists, this is also used to
   * replace that particular requirement for some magic-{@link Item} types.
   *
   * @return <code>true</code> if has {@link Spell#getability(Monster)} to learn
   *   this or enough {@link UseMagicDevice} to emulate it .
   */
  public static boolean enable(Spell s,Combatant c){
    var a=Math.max(Spell.getability(c.source),UseMagicDevice.getability(c));
    return a>=10+s.level;
  }

  @Override
  public String canuse(Combatant c){
    if(!identified) return "can't determine spell";
    if(enable(spell,c)||c.taketen(Skill.USEMAGICDEVICE)>=20) return null;
    return "can't activate";
  }
}
