package javelin.controller.content.kit.wizard;

import javelin.controller.content.quality.resistance.DamageReduction;
import javelin.controller.content.quality.resistance.EnergyImmunity;
import javelin.controller.content.quality.resistance.EnergyResistance;
import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.quality.resistance.ParalysisImmunity;
import javelin.controller.content.quality.resistance.PoisonImmunity;
import javelin.controller.content.upgrade.ability.RaiseIntelligence;
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
	protected void define(){
		super.define();
		basic.add(EnergyResistance.UPGRADE);
	}

	@Override
	protected void extend(){
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
	}
}
