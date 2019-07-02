package javelin.controller.kit;

import javelin.controller.quality.resistance.DamageReduction;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.ability.RaiseConstitution;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;
import javelin.controller.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.BarbarianRage;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.feat.attack.BullRush;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.feat.attack.GreatCleave;
import javelin.model.unit.feat.attack.PowerAttack;
import javelin.model.unit.feat.attack.focus.MeleeFocus;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.Academy;
import javelin.model.world.location.town.labor.ecological.MeadHall;

public class Barbarian extends Kit{
	public static final Kit INSTANCE=new Barbarian();

	private Barbarian(){
		super("Barbarian",Warrior.SINGLETON,RaiseStrength.SINGLETON,
				RaiseConstitution.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.SURVIVAL.getupgrade());
		basic.add(new BarbarianRage());
		basic.add(MeleeFocus.UPGRADE.toupgrade());
		basic.add(WalkingSpeed.HUMAN);
	}

	@Override
	protected void extend(){
		extension.add(new FeatUpgrade(PowerAttack.SINGLETON));
		extension.add(new FeatUpgrade(BullRush.SINGLETON));
		extension.add(new FeatUpgrade(Cleave.SINGLETON));
		extension.add(new FeatUpgrade(GreatCleave.SINGLETON));
		extension.add(MeleeDamage.INSTANCE);
		extension.add(Toughness.SINGLETON.toupgrade());
		extension.add(GreatFortitude.SINGLETON.toupgrade());
		extension.add(DamageReduction.UPGRADE);
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return !m.alignment.islawful()&&super.allow(bestability,secondbest,m);
	}

	@Override
	public Academy createguild(){
		return new MeadHall();
	}
}