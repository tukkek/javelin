package javelin.controller.kit;

import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.kit.wizard.Diviner;
import javelin.controller.kit.wizard.Enchanter;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.religious.Sanctuary;

public class Cleric extends Kit{
	/**
	 * TODO might be intesteing to separate into Good and Evil clerc. Evil Cleric
	 * academies could only be buitl on criminal + religious cities.
	 */
	public static final Kit INSTANCE=new Cleric();

	private Cleric(){
		super("cleric",Aristocrat.SINGLETON,RaiseWisdom.SINGLETON,
				RaiseStrength.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(new CureModerateWounds());
		basic.add(Skill.KNOWLEDGE.getupgrade());
		basic.add(Skill.HEAL.getupgrade());
		basic.add(NaturalArmor.SCALES);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(Conjurer.INSTANCE.getupgrades());
		extension.addAll(Enchanter.INSTANCE.filter(Spell.class));
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