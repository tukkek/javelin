package javelin.controller.kit;

import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.controller.upgrade.movement.Flying;
import javelin.controller.upgrade.movement.SpeedUpgrade;
import javelin.controller.upgrade.movement.Swimming;
import javelin.controller.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
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
		super("monk",Warrior.SINGLETON,RaiseStrength.SINGLETON,
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
	protected void extend(UpgradeHandler h){
		extension.add(Skill.BLUFF.getupgrade());
		extension.add(Skill.SENSEMOTIVE.getupgrade());
		extension.add(RaiseWisdom.SINGLETON);
		extension.add(MeleeDamage.INSTANCE);
		extension.add(CriticalImmunity.UPGRADE);
		extension.add(RaiseWisdom.SINGLETON);
		extension.add(Flying.RAVEN);
		extension.add(IronWill.SINGLETON.toupgrade());
		extension.add(LightningReflexes.SINGLETON.toupgrade());
		extension.add(GreatFortitude.SINGLETON.toupgrade());
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