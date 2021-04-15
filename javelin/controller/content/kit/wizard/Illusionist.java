package javelin.controller.content.kit.wizard;

import javelin.controller.content.kit.Kit;
import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.upgrade.ability.RaiseCharisma;
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
	protected void extend(){
		super.extend();
		//TODO this is currently only boilerplate! See above.
		extension.add(new Displacement()); // TODO currently registered as Abjuration
		extension.add(MindImmunity.UPGRADE);
	}
}
