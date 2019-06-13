package javelin.controller.kit;

import java.util.stream.Collectors;

import javelin.controller.kit.wizard.Conjurer;
import javelin.controller.quality.FastHealing;
import javelin.controller.quality.resistance.ParalysisImmunity;
import javelin.controller.quality.resistance.PoisonImmunity;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.movement.Burrow;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.ecological.Henge;

/**
 * Summons {@link MonsterType#ANIMAL}S, {@link MonsterType#VERMIN},
 * {@link MonsterType#FEY} and {@link MonsterType#ELEMENTAL}S,
 *
 * @author alex
 */
public class Druid extends Kit{
	public static final Kit INSTANCE=new Druid();

	private Druid(){
		super("Druid",Commoner.SINGLETON,RaiseWisdom.SINGLETON,
				RaiseCharisma.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.SURVIVAL.getupgrade());
	}

	@Override
	protected void extend(){
		extension.addAll(Conjurer.HEALING);
		extension.add(Burrow.BADGER);
		extension.add(FastHealing.UPGRADE);
		extension.add(Skill.HEAL.getupgrade());
		extension.add(ParalysisImmunity.UPGRADE);
		extension.add(PoisonImmunity.UPGRADE);
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