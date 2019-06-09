package javelin.controller.kit.wizard;

import javelin.controller.kit.Kit;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.skill.Skill;

public abstract class Wizard extends Kit{
	protected Wizard(String name,RaiseAbility ability,String title1){
		super(name,Aristocrat.SINGLETON,ability,title1,
				"Apprentice "+name.toLowerCase(),name,"Grand "+name.toLowerCase());
		extension.stream().filter(u->u instanceof Spell).map(u->(Spell)u)
				.filter(s->s.casterlevel==1).forEach(s->basic.add(s));
	}

	@Override
	protected void define(){
		basic.add(Skill.CONCENTRATION.getupgrade());
		basic.add(Skill.SPELLCRAFT.getupgrade());
	}
}