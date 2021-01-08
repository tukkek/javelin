package javelin.controller.content.kit;

import javelin.controller.content.quality.perception.Vision;
import javelin.controller.content.quality.resistance.CriticalImmunity;
import javelin.controller.content.quality.resistance.EnergyImmunity;
import javelin.controller.content.quality.resistance.EnergyResistance;
import javelin.controller.content.quality.resistance.MindImmunity;
import javelin.controller.content.quality.resistance.ParalysisImmunity;
import javelin.controller.content.quality.resistance.PoisonImmunity;
import javelin.controller.content.upgrade.ability.RaiseDexterity;
import javelin.controller.content.upgrade.ability.RaiseStrength;
import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.controller.content.upgrade.movement.Flying;
import javelin.controller.content.upgrade.movement.SpeedUpgrade;
import javelin.controller.content.upgrade.movement.Swimming;
import javelin.controller.content.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.Academy;
import javelin.model.world.location.town.labor.military.Monastery;

/**
 * A warrior that focuses on high mobility and close combat - liek in Crouching
 * Tiger, Hidden Dragon.
 *
 * TODO instead of flying, a Jump {@link SpeedUpgrade} could be introduced that
 * would allow you moving over obstacles (move AP cost proprtional to skill).
 *
 * @author alex
 */
public class Monk extends Kit{
	/** Singleton. */
	public static final Kit INSTANCE=new Monk();

	private Monk(){
		super("Monk",Warrior.SINGLETON,RaiseStrength.SINGLETON,
				RaiseDexterity.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.ACROBATICS.getupgrade());
		basic.add(Acrobatic.SINGLETON.toupgrade());
		basic.add(ImprovedInitiative.SINGLETON.toupgrade());
		basic.add(WalkingSpeed.CHEETAH);
		basic.add(Swimming.SNAKE);
	}

	@Override
	protected void extend(){
		extension.add(Skill.BLUFF.getupgrade());
		extension.add(Skill.SENSEMOTIVE.getupgrade());
		extension.add(RaiseWisdom.SINGLETON);
		extension.add(Flying.RAVEN);
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(IronWill.SINGLETON.toupgrade());
		extension.add(LightningReflexes.SINGLETON.toupgrade());
		extension.add(GreatFortitude.SINGLETON.toupgrade());
		extension.add(Vision.DARKVISION);
		extension.add(CriticalImmunity.UPGRADE);
		extension.add(EnergyImmunity.UPGRADE);
		extension.add(MindImmunity.UPGRADE);
		extension.add(ParalysisImmunity.UPGRADE);
		extension.add(PoisonImmunity.UPGRADE);
		extension.add(EnergyResistance.UPGRADE);
		extension.add(Skill.PERCEPTION.getupgrade());
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return !m.alignment.ischaotic()&&super.allow(bestability,secondbest,m);
	}

	@Override
	public Academy createguild(){
		return new Monastery();
	}
}