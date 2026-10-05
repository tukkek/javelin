package javelin.controller.content.kit;

import javelin.controller.content.quality.perception.Vision;
import javelin.controller.content.upgrade.NaturalArmor;
import javelin.controller.content.upgrade.ability.RaiseDexterity;
import javelin.controller.content.upgrade.ability.RaiseWisdom;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.controller.content.upgrade.damage.RangedDamage;
import javelin.controller.content.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.attack.focus.RangedFocus;
import javelin.model.unit.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.unit.feat.attack.shot.PointBlankShot;
import javelin.model.unit.feat.attack.shot.PreciseShot;
import javelin.model.unit.feat.attack.shot.RapidShot;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.academy.Academy;
import javelin.model.world.location.town.labor.ecological.ArcheryRange;

public class Ranger extends Kit{
	public static final Kit INSTANCE=new Ranger();

	private Ranger(){
		super("Ranger",Warrior.SINGLETON,RaiseDexterity.SINGLETON,
				RaiseWisdom.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(PointBlankShot.SINGLETON.toupgrade());
		basic.add(PreciseShot.SINGLETON.toupgrade());
		basic.add(RapidShot.SINGLETON.toupgrade());
		basic.add(NaturalArmor.LEATHER);
		basic.add(RangedFocus.SINGLETON.toupgrade());
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return !m.ranged.isEmpty()&&super.allow(bestability,secondbest,m);
	}

	@Override
	protected void extend(){
		extension.add(ImprovedPreciseShot.SINGLETON.toupgrade());
		extension.add(RangedDamage.INSTANCE);
		extension.add(Alertness.SINGLETON.toupgrade());
		extension.add(WalkingSpeed.HUMAN);
		extension.add(Vision.LOWLIGHTVISION);
		extension.add(Skill.PERCEPTION.getupgrade());
	}

	@Override
	public Academy createguild(){
		return new ArcheryRange();
	}
}