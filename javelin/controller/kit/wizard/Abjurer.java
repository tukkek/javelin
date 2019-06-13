package javelin.controller.kit.wizard;

import javelin.controller.quality.resistance.DamageReduction;
import javelin.controller.quality.resistance.EnergyImmunity;
import javelin.controller.quality.resistance.EnergyResistance;
import javelin.controller.quality.resistance.MindImmunity;
import javelin.controller.quality.resistance.ParalysisImmunity;
import javelin.controller.quality.resistance.PoisonImmunity;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.model.unit.abilities.spell.abjuration.Barkskin;
import javelin.model.unit.abilities.spell.abjuration.Blink;
import javelin.model.unit.abilities.spell.abjuration.DispelMagic;
import javelin.model.unit.abilities.spell.abjuration.ResistEnergy;
import javelin.model.unit.abilities.spell.illusion.Displacement;

/**
 * Abjuration wizard.
 *
 * @author alex
 */
public class Abjurer extends Wizard{
	/** Singleton. */
	public static final Abjurer INSTANCE=new Abjurer();

	/** Constructor. */
	Abjurer(){
		super("Abjurer",RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(new Blink());
		extension.add(new Barkskin());
		extension.add(new ResistEnergy());
		extension.add(new DispelMagic());
		extension.add(new Displacement()); // TODO illusion
		extension.add(DamageReduction.UPGRADE);
		extension.add(EnergyImmunity.UPGRADE);
		extension.add(MindImmunity.UPGRADE);
		extension.add(ParalysisImmunity.UPGRADE);
		extension.add(PoisonImmunity.UPGRADE);
		extension.add(EnergyResistance.UPGRADE);
	}
}
