package javelin.controller.kit;

import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.damage.effect.DamageEffect;
import javelin.controller.upgrade.damage.effect.EffectUpgrade;
import javelin.controller.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.feat.skill.Deceitful;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.unique.AssassinsGuild;

public class Assassin extends Kit{
	public static final Kit INSTANCE=new Assassin();

	private Assassin(){
		super("assassin",Expert.SINGLETON,RaiseDexterity.SINGLETON,
				RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.DISGUISE.getupgrade());
		basic.add(Skill.STEALTH.getupgrade());
		basic.add(ImprovedInitiative.SINGLETON.toupgrade());
		basic.add(new EffectUpgrade(DamageEffect.POISON));
		basic.add(WalkingSpeed.CHEETAH);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(NaturalArmor.LEATHER);
		extension.add(IronWill.SINGLETON.toupgrade());
		extension.add(GreatFortitude.SINGLETON.toupgrade());
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(LightningReflexes.SINGLETON.toupgrade());
		extension.add(RaiseCharisma.SINGLETON);
		extension.add(Deceitful.SINGLETON.toupgrade());
		extension.add(Acrobatic.SINGLETON.toupgrade());
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return m.alignment.isevil()&&super.allow(bestability,secondbest,m);
	}

	@Override
	public Academy createguild(){
		return new AssassinsGuild();
	}
}