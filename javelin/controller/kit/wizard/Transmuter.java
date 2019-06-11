package javelin.controller.kit.wizard;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.abilities.spell.totem.BearsEndurance;
import javelin.model.unit.abilities.spell.totem.BullsStrength;
import javelin.model.unit.abilities.spell.totem.CatsGrace;
import javelin.model.unit.abilities.spell.totem.EaglesSplendor;
import javelin.model.unit.abilities.spell.totem.FoxsCunning;
import javelin.model.unit.abilities.spell.totem.OwlsWisdom;
import javelin.model.unit.abilities.spell.transmutation.ControlWeather;
import javelin.model.unit.abilities.spell.transmutation.Darkvision;
import javelin.model.unit.abilities.spell.transmutation.Fly;
import javelin.model.unit.abilities.spell.transmutation.Longstrider;

/**
 * Transmutation wizard.
 *
 * @author alex
 */
public class Transmuter extends Wizard{
	/** Singleton instance. */
	public static final Transmuter INSTANCE=new Transmuter();

	Transmuter(){
		super("Transmuter",RaiseWisdom.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(new BearsEndurance());
		extension.add(new BullsStrength());
		extension.add(new CatsGrace());
		extension.add(new EaglesSplendor());
		extension.add(new FoxsCunning());
		extension.add(new OwlsWisdom());
		extension.add(new Darkvision());
		extension.add(new ControlWeather());
		extension.add(new Fly());
		extension.add(new Longstrider());
	}
}
