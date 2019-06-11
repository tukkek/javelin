package javelin.controller.kit.wizard;

import javelin.controller.kit.Kit;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.cultural.MagesGuild;

public abstract class Wizard extends Kit{
	/** Constructor. */
	protected Wizard(String name,RaiseAbility ability){
		super(name,Aristocrat.SINGLETON,ability);
		extension.stream().filter(u->u instanceof Spell).map(u->(Spell)u)
				.filter(s->s.casterlevel==1).forEach(s->basic.add(s));
		var lower=name.toLowerCase();
		titles=new String[]{"Fledgling $ "+lower,"Apprentice $ "+lower,"$ "+lower,
				"$ grand-"+lower};
	}

	@Override
	protected void define(){
		basic.add(Skill.CONCENTRATION.getupgrade());
		basic.add(Skill.SPELLCRAFT.getupgrade());
		basic.add(CombatCasting.SINGLETON.getupgrade());
	}

	@Override
	public Academy createguild(){
		return new MagesGuild(this);
	}
}