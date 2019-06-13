package javelin.controller.kit.wizard;

import javelin.controller.kit.Kit;
import javelin.controller.quality.resistance.MindImmunity;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.model.unit.abilities.spell.illusion.Displacement;

/**
 * Illusion wizard.
 *
 * TODO this is currently only boilerplate and not registered on
 * {@link Kit#KITS}!
 *
 * @author alex
 */
public class Illusionist extends Wizard{
	/** Singleton. */
	public static final Illusionist INSTANCE=new Illusionist();

	/** Constructor. */
	public Illusionist(){
		super("Illusionist",RaiseCharisma.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h){
		//TODO this is currently only boilerplate! See above.
		extension.add(new Displacement()); // TODO currently registered as Abjuration
		extension.add(MindImmunity.UPGRADE);
	}
}
