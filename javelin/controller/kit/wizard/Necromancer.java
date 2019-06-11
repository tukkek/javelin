package javelin.controller.kit.wizard;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.model.unit.abilities.spell.necromancy.Doom;
import javelin.model.unit.abilities.spell.necromancy.Poison;
import javelin.model.unit.abilities.spell.necromancy.RayOfExhaustion;
import javelin.model.unit.abilities.spell.necromancy.SlayLiving;
import javelin.model.unit.abilities.spell.necromancy.VampiricTouch;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictCriticalWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictLightWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictModerateWounds;
import javelin.model.unit.abilities.spell.necromancy.wounds.InflictSeriousWounds;

/**
 * Necromancy spells.
 *
 * @author alex
 */
public class Necromancer extends Wizard{
	/** Singleton. */
	public static final Necromancer INSTANCE=new Necromancer();

	/** Constructor. */
	public Necromancer(){
		super("Necromancer",RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(new SlayLiving());
		extension.add(new VampiricTouch());
		extension.add(new Doom());
		extension.add(new Poison());
		extension.add(new RayOfExhaustion());
		extension.add(new InflictLightWounds());
		extension.add(new InflictModerateWounds());
		extension.add(new InflictSeriousWounds());
		extension.add(new InflictCriticalWounds());
	}
}
