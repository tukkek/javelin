package javelin.controller.kit;

import javelin.controller.quality.perception.Vision;
import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.feat.skill.Deceitful;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.Academy;
import javelin.model.world.location.town.labor.criminal.ThievesGuild;

public class Rogue extends Kit{
	public static final Kit INSTANCE=new Rogue();

	private Rogue(){
		super("Rogue",Expert.SINGLETON,RaiseDexterity.SINGLETON,
				RaiseIntelligence.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.BLUFF.getupgrade());
		basic.add(Skill.DISABLEDEVICE.getupgrade());
		basic.add(Skill.SENSEMOTIVE.getupgrade());
		basic.add(Skill.STEALTH.getupgrade());
		basic.add(Skill.PERCEPTION.getupgrade());
	}

	@Override
	protected void extend(){
		extension.add(WalkingSpeed.HUMAN);
		extension.add(new FeatUpgrade(Deceitful.SINGLETON));
		extension.add(NaturalArmor.LEATHER);
		extension.add(CriticalImmunity.UPGRADE);
		extension.add(Deceitful.SINGLETON.toupgrade());
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(LightningReflexes.SINGLETON.toupgrade());
		extension.add(Vision.LOWLIGHTVISION);
		extension.add(Skill.USEMAGICDEVICE.getupgrade());
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return super.allow(bestability,secondbest,m)&&!m.alignment.isgood()
				&&!m.alignment.islawful();
	}

	@Override
	public Academy createguild(){
		return new ThievesGuild();
	}
}