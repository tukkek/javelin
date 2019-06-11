package javelin.controller.kit.wizard;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bane;
import javelin.model.unit.abilities.spell.enchantment.compulsion.BarbarianRage;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;
import javelin.model.unit.abilities.spell.enchantment.compulsion.DominateMonster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Rage;

/**
 * Kit for tnchantments and compuplsion magic.
 *
 * @author alex
 */
public class Enchanter extends Wizard{
	/** Singleton. */
	public static final Enchanter INSTANCE=new Enchanter();

	/** Constructor. */
	Enchanter(){
		super("Enchanter",RaiseCharisma.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h){
		//compulsion magic
		extension.add(new Heroism());// enchantment
		extension.add(new HoldMonster());
		extension.add(new DominateMonster());
		extension.add(new Bless());
		extension.add(new Bane());
		extension.add(new Rage());
		extension.add(new BarbarianRage());
	}
}
