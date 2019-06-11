package javelin.controller.kit;

import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Expert;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.skill.Deceitful;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.criminal.ThievesGuild;

public class Rogue extends Kit{
	public static final Kit INSTANCE=new Rogue();

	private Rogue(){
		super("rogue",Expert.SINGLETON,RaiseDexterity.SINGLETON,
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
	protected void extend(UpgradeHandler h){
		extension.add(new FeatUpgrade(Deceitful.SINGLETON));
		extension.addAll(h.wind);
		extension.addAll(h.evil);
		extension.add(NaturalArmor.LEATHER);
		extension.add(CriticalImmunity.UPGRADE);
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