package javelin.controller.content.kit.wizard;

import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.upgrade.ability.RaiseCharisma;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bane;
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
	protected void extend(){
		//compulsion magic
		extension.add(new Heroism());// enchantment
		extension.add(new HoldMonster());
		extension.add(new DominateMonster());
		extension.add(new Bless());
		extension.add(new Bane());
		extension.add(new Rage());
		extension.add(MindImmunity.UPGRADE);
	}
}
