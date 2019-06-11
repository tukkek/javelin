package javelin.controller.kit;

import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.kit.wizard.Diviner;
import javelin.controller.kit.wizard.Transmuter;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.ecological.Henge;
import javelin.old.RPG;

public class Druid extends Kit{
	public static final Kit INSTANCE=new Druid();

	private Druid(){
		super("druid",Commoner.SINGLETON,RaiseWisdom.SINGLETON,
				RaiseCharisma.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(new Summon("Small monstrous centipede",1));
		basic.add(new Summon("Dire rat",1));
		basic.add(new Summon("Eagle",1));
		basic.add(Skill.SURVIVAL.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.earth);
		extension.addAll(h.water);
		extension.addAll(h.wind);
		extension.addAll(Conjurer.HEALING);
		extension.addAll(Transmuter.INSTANCE.filter(Spell.class));
		extension.addAll(Diviner.INSTANCE.filter(Spell.class));
		addsummons(extension.size());
		extension.add(NaturalArmor.LEATHER);
	}

	void addsummons(int nsummons){
		LinkedList<Monster> summons=new LinkedList<>();
		summons.addAll(Javelin.getmonsterbytype(MonsterType.VERMIN));
		summons.addAll(Javelin.getmonsterbytype(MonsterType.ANIMAL));
		summons.addAll(Javelin.getmonsterbytype(MonsterType.FEY));
		summons.addAll(Javelin.getmonsterbytype(MonsterType.ELEMENTAL));
		for(int i=0;i<nsummons&&!summons.isEmpty();i++){
			Monster m=RPG.pick(summons);
			extension.add(new Summon(m.name));
		}
	}

	@Override
	public Academy createguild(){
		return new Henge();
	}
}