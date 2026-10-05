package javelin.controller.content.kit.wizard;

import javelin.controller.content.quality.resistance.EnergyImmunity;
import javelin.controller.content.quality.resistance.EnergyResistance;
import javelin.controller.content.upgrade.ability.RaiseIntelligence;
import javelin.model.unit.abilities.spell.evocation.FlameStrike;
import javelin.model.unit.abilities.spell.evocation.MagicMissile;
import javelin.model.unit.abilities.spell.evocation.PolarRay;
import javelin.model.unit.abilities.spell.evocation.ScorchingRay;
import javelin.model.unit.abilities.spell.evocation.SoundBurst;

/**
 * Evocation wizard.
 *
 * @author alex
 */
public class Evoker extends Wizard{
	/** Singleton. */
	public static final Evoker INSTANCE=new Evoker();

	Evoker(){
		super("Evoker",RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void extend(){
		super.extend();
		extension.add(new ScorchingRay());
		extension.add(new MagicMissile());
		extension.add(new PolarRay());
		extension.add(new SoundBurst());
		extension.add(new FlameStrike());
		extension.add(EnergyImmunity.UPGRADE);
		extension.add(EnergyResistance.UPGRADE);
	}
}
