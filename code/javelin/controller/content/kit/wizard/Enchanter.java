package javelin.controller.content.kit.wizard;

import java.util.List;

import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.upgrade.ability.RaiseCharisma;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bane;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.abilities.spell.enchantment.compulsion.DominateMonster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Kill;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Rage;

/** Kit for enchantments and compuplsion magic. */
public class Enchanter extends Wizard{
  /** Singleton. */
  public static final Enchanter INSTANCE=new Enchanter();

  /** Constructor. */
  Enchanter(){
    super("Enchanter",RaiseCharisma.SINGLETON);
  }

  @Override
  protected void extend(){
    super.extend();
    extension.add(MindImmunity.UPGRADE);
    var compulsion=List.of(new Heroism(),new HoldMonster(),
        new DominateMonster(),new Bless(),new Bane(),new Rage(),new Kill());
    extension.addAll(compulsion);
  }
}
