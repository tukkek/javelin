package javelin.controller.kit.dragoon;

import javelin.controller.kit.Fighter;
import javelin.controller.kit.Kit;
import javelin.controller.kit.Monk;
import javelin.controller.quality.resistance.ParalysisImmunity;
import javelin.controller.upgrade.BreathUpgrade;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.controller.upgrade.damage.effect.DamageEffect;
import javelin.controller.upgrade.damage.effect.EffectUpgrade;
import javelin.controller.upgrade.movement.Flying;
import javelin.controller.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Body;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.feat.attack.focus.MeleeFocus;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.Guild;
import javelin.model.world.location.town.Town;

/**
 * A supernatural {@link Warrior} with abilities akin to a
 * {@link MonsterType#DRAGON}, including a {@link BreathWeapon}. Abilities
 * aside, they are basically standard mêléé {@link Fighter}s with some of the
 * {@link Monk}'s mobility.
 *
 * TODO add metallic dragoons at some point
 *
 * TODO should only a single Dragoon {@link Guild} be allowed per {@link Town}?
 * See {@link #buildguild()}.
 *
 * @author alex
 */
public abstract class Dragoon extends Kit{
	/** Constructor. */
	protected Dragoon(String subtype,BreathWeapon breath,int naturalarmor,
			int walk,int flight,DamageEffect effect){
		super(subtype+" dragoon",Warrior.SINGLETON,RaiseStrength.SINGLETON,
				RaiseDexterity.SINGLETON);
		basic.add(new WalkingSpeed(subtype+" dragon speed",walk));
		extension.add(
				new NaturalArmor(subtype.toLowerCase()+" dragon scales",naturalarmor));
		extension.add(new BreathUpgrade(breath));
		extension.add(new Flying(subtype+" dragon flight",flight));
		if(effect!=null) extension.add(new EffectUpgrade(effect));
		prestige=true;
	}

	@Override
	protected void define(){
		basic.add(MeleeFocus.UPGRADE.toupgrade());
		basic.add(Skill.ACROBATICS.getupgrade());
		basic.add(ImprovedInitiative.SINGLETON.toupgrade());
	}

	@Override
	protected void extend(){
		extension.add(Toughness.SINGLETON.toupgrade());
		extension.add(MeleeDamage.INSTANCE);
		extension.add(LightningReflexes.SINGLETON.toupgrade());
		extension.add(Acrobatic.SINGLETON.toupgrade());
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(ParalysisImmunity.UPGRADE);
		extension.add(Skill.PERCEPTION.getupgrade());
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return super.allow(bestability,secondbest,m)&&m.body.equals(Body.HUMANOID);
	}
}
