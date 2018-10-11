package javelin.controller.kit;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Expert;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.skill.Deceitful;
import javelin.model.unit.skill.Skill;

public class Rogue extends Kit{
	public static final Kit INSTANCE=new Rogue();

	private Rogue(){
		super("rogue",Expert.SINGLETON,RaiseDexterity.SINGLETON,"Cutpurse",
				"Burglar","Rogue","Shadow");
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
		basic.add(new FeatUpgrade(Deceitful.SINGLETON));
		extension.addAll(h.wind);
		extension.addAll(h.evil);
		extension.addAll(h.combatexpertise);
		extension.addAll(h.shots);
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return super.allow(bestability,secondbest,m)&&!Boolean.TRUE.equals(m.good)
				&&!Boolean.TRUE.equals(m.lawful);
	}
}