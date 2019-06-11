package javelin.controller.kit;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.unit.feat.attack.expertise.ImprovedFeint;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;
import javelin.model.unit.feat.attack.expertise.ImprovedTrip;
import javelin.model.unit.skill.Skill;

public class Fighter extends Kit{
	public static final Kit INSTANCE=new Fighter();

	Fighter(){
		super("fighter",Warrior.SINGLETON,RaiseStrength.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(MeleeDamage.INSTANCE);
		basic.add(new FeatUpgrade(CombatExpertise.SINGLETON));
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.add(Skill.SENSEMOTIVE.getupgrade());
		extension.addAll(h.fire);
		extension.addAll(h.earth);
		basic.add(new FeatUpgrade(ImprovedFeint.SINGLETON));
		basic.add(new FeatUpgrade(ImprovedGrapple.SINGLETON));
		basic.add(new FeatUpgrade(ImprovedTrip.SINGLETON));
	}
}