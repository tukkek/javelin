package javelin.controller.kit.wizard;

import javelin.controller.kit.Kit;
import javelin.controller.quality.resistance.SpellImmunity;
import javelin.controller.quality.resistance.SpellResistance;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.cultural.MagesGuild;

/**
 * Ideally, a kit that represents one of the eight schools of magic.
 *
 * @author alex
 */
public abstract class Wizard extends Kit{
	/** Constructor. */
	protected Wizard(String name,RaiseAbility ability){
		super(name,Aristocrat.SINGLETON,ability,RaiseIntelligence.SINGLETON);
		var lower=name.toLowerCase();
		titles=new String[]{"Fledgling $ "+lower,"Apprentice $ "+lower,"$ "+lower,
				"$ grand-"+lower};
	}

	@Override
	protected void define(){
		//let the priority fall with any level 1 spells
		//see Kit#finish
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(Skill.CONCENTRATION.getupgrade());
		extension.add(Skill.SPELLCRAFT.getupgrade());
		extension.add(CombatCasting.SINGLETON.toupgrade());
		extension.add(SpellImmunity.UPGRADE);
		extension.add(SpellResistance.UPGRADE);
	}

	@Override
	public Academy createguild(){
		return new MagesGuild(this);
	}
}