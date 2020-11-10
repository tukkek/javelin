package javelin.controller.kit.wizard;

import javelin.controller.quality.perception.Vision;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.model.unit.abilities.spell.divination.DiscernLocation;
import javelin.model.unit.abilities.spell.divination.FindTraps;
import javelin.model.unit.abilities.spell.divination.Identify;
import javelin.model.unit.abilities.spell.divination.LocateObject;
import javelin.model.unit.abilities.spell.divination.PryingEyes;

/**
 * Divination magic.
 *
 * @author alex
 */
public class Diviner extends Wizard{
	/** Singleton. */
	public static final Diviner INSTANCE=new Diviner();

	/** Constructor. */
	public Diviner(){
		super("Diviner",RaiseWisdom.SINGLETON);
		basic.add(Vision.LOWLIGHTVISION);
	}

	@Override
	protected void extend(){
		extension.add(new LocateObject());
		extension.add(new PryingEyes());
		extension.add(new DiscernLocation());
		extension.add(new FindTraps());
		extension.add(new Identify());
	}
}
