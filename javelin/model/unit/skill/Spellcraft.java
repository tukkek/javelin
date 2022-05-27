package javelin.model.unit.skill;

import javelin.model.item.consumable.Scroll;
import javelin.model.unit.abilities.spell.Spell;

/** TODO can be used to learn {@link Spell}s from a {@link Scroll} */
public class Spellcraft extends Skill{
  static final String[] NAMES={"Spellcraft","Scry"};

  /** Constructor. */
  public Spellcraft(){
    super(NAMES,Ability.INTELLIGENCE);
    intelligent=true;
  }
}
