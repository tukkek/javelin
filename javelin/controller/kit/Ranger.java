package javelin.controller.kit;

import java.util.List;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.RangedDamage;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.unit.feat.attack.shot.PointBlankShot;
import javelin.model.unit.feat.attack.shot.PreciseShot;
import javelin.model.unit.feat.attack.shot.RapidShot;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.ecological.ArcheryRange;

public class Ranger extends Kit{
	public static final Kit INSTANCE=new Ranger();

	private Ranger(){
		super("ranger",Warrior.SINGLETON,RaiseDexterity.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.SURVIVAL.getupgrade());
		for(var feat:List.of(PointBlankShot.SINGLETON,PreciseShot.SINGLETON,
				RapidShot.SINGLETON))
			basic.add(new FeatUpgrade(feat));
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return !m.ranged.isEmpty()&&super.allow(bestability,secondbest,m);
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.earth);
		extension.addAll(h.magic);
		extension.add(new FeatUpgrade(ImprovedPreciseShot.SINGLETON));
		extension.add(RangedDamage.INSTANCE);
	}

	@Override
	public Academy createguild(){
		return new ArcheryRange();
	}
}