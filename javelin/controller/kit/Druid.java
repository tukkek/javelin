package javelin.controller.kit;

import java.util.stream.Collectors;

import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.kit.wizard.Diviner;
import javelin.controller.kit.wizard.Transmuter;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.movement.Burrow;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.ecological.Henge;

public class Druid extends Kit{
	public static final Kit INSTANCE=new Druid();

	private Druid(){
		super("druid",Commoner.SINGLETON,RaiseWisdom.SINGLETON,
				RaiseCharisma.SINGLETON);
	}

	@Override
	protected void define(){
		//		basic.add(new Summon("Small monstrous centipede",1));
		//		basic.add(new Summon("Dire rat",1));
		//		basic.add(new Summon("Eagle",1));
		basic.add(Skill.SURVIVAL.getupgrade());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(Conjurer.HEALING);
		extension.addAll(Transmuter.INSTANCE.filter(Spell.class));
		extension.addAll(Diviner.INSTANCE.filter(Spell.class));
		extension.add(NaturalArmor.LEATHER);
		extension.add(Burrow.BADGER);
	}

	boolean filtersummon(String monstername){
		var m=Monster.get(monstername);
		return MonsterType.VERMIN.equals(m.type)||MonsterType.ANIMAL.equals(m.type)
				||MonsterType.FEY.equals(m.type)||MonsterType.ELEMENTAL.equals(m.type);
	}

	@Override
	public void finish(){
		extension.addAll(findsummons(Summon.SUMMONS.stream()
				.filter(s->filtersummon(s.monstername)).collect(Collectors.toList())));
		super.finish();
	}

	@Override
	public Academy createguild(){
		return new Henge();
	}
}