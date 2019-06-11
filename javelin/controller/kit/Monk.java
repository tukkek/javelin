package javelin.controller.kit;

import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.military.Monastery;

public class Monk extends Kit{
	public static final Kit INSTANCE=new Monk();

	private Monk(){
		super("monk",Warrior.SINGLETON,RaiseStrength.SINGLETON,
				RaiseDexterity.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(RaiseWisdom.SINGLETON);
		basic.add(Skill.ACROBATICS.getupgrade());
		for(Feat f:new Feat[]{Acrobatic.SINGLETON,ImprovedInitiative.SINGLETON,
				LightningReflexes.SINGLETON})
			basic.add(new FeatUpgrade(f));
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(Skill.BLUFF.getupgrade());
		extension.add(Skill.SENSEMOTIVE.getupgrade());
		extension.add(RaiseWisdom.SINGLETON);
		extension.addAll(h.water);
		extension.addAll(h.earth);
		extension.add(IronWill.SINGLETON.getupgrade());
		extension.add(MeleeDamage.INSTANCE);
		extension.add(CriticalImmunity.UPGRADE);
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