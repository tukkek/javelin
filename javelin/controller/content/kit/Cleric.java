package javelin.controller.content.kit;

import javelin.controller.content.kit.wizard.Conjurer;
import javelin.controller.content.kit.wizard.Diviner;
import javelin.controller.content.upgrade.NaturalArmor;
import javelin.controller.content.upgrade.ability.RaiseStrength;
import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.controller.content.upgrade.classes.Aristocrat;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.town.labor.religious.Sanctuary;

public class Cleric extends Kit{
	/**
	 * TODO might be intesteing to separate into Good and Evil clerc. Evil Cleric
	 * academies could only be buitl on criminal + religious cities.
	 */
	public static final Kit INSTANCE=new Cleric();

	private Cleric(){
		super("Cleric",Aristocrat.SINGLETON,RaiseWisdom.SINGLETON,
				RaiseStrength.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(new CureModerateWounds());
	}

	@Override
	protected void extend(){
		extension.add(Skill.KNOWLEDGE.getupgrade());
		extension.add(Skill.HEAL.getupgrade());
		extension.add(Skill.CONCENTRATION.getupgrade());
		extension.add(NaturalArmor.SCALES);
		extension.addAll(Conjurer.HEALING);
		extension.addAll(Conjurer.RESTORATION);
		extension.addAll(Diviner.INSTANCE.filter(Spell.class));
		extension.add(Toughness.SINGLETON.toupgrade());
		extension.add(CombatCasting.SINGLETON.toupgrade());
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		if(!super.allow(bestability,secondbest,m)) return false;
		return !m.alignment.ischaotic()&&!m.alignment.isevil();
	}

	@Override
	public Academy createguild(){
		return new Sanctuary();
	}
}