package javelin.controller.content.kit;

import javelin.controller.content.upgrade.FeatUpgrade;
import javelin.controller.content.upgrade.NaturalArmor;
import javelin.controller.content.upgrade.ability.RaiseConstitution;
import javelin.controller.content.upgrade.ability.RaiseStrength;
import javelin.controller.content.upgrade.classes.Warrior;
import javelin.controller.content.upgrade.damage.MeleeDamage;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.unit.feat.attack.expertise.ImprovedFeint;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;
import javelin.model.unit.feat.attack.expertise.ImprovedTrip;
import javelin.model.unit.feat.attack.focus.MeleeFocus;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.skill.Skill;

public class Fighter extends Kit{
	public static final Kit INSTANCE=new Fighter();

	Fighter(){
		super("Fighter",Warrior.SINGLETON,RaiseStrength.SINGLETON,
				RaiseConstitution.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(MeleeDamage.INSTANCE);
		basic.add(new FeatUpgrade(CombatExpertise.SINGLETON));
		basic.add(NaturalArmor.SCALES);
	}

	@Override
	protected void extend(){
		extension.add(Skill.SENSEMOTIVE.getupgrade());
		extension.add(new FeatUpgrade(ImprovedFeint.SINGLETON));
		extension.add(new FeatUpgrade(ImprovedGrapple.SINGLETON));
		extension.add(new FeatUpgrade(ImprovedTrip.SINGLETON));
		extension.add(NaturalArmor.PLATES);
		extension.add(MeleeFocus.UPGRADE.toupgrade());
		extension.add(Toughness.SINGLETON.toupgrade());
		extension.add(Alertness.SINGLETON.toupgrade());
	}
}