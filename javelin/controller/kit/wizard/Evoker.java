package javelin.controller.kit.wizard;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseIntelligence;
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
	protected void extend(UpgradeHandler h){
		extension.add(new ScorchingRay());
		extension.add(new MagicMissile());
		extension.add(new PolarRay());
		extension.add(new SoundBurst());
		extension.add(new FlameStrike());
	}
}
