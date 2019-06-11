package javelin.controller.kit;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.BarbarianRage;
import javelin.model.unit.feat.attack.BullRush;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.feat.attack.GreatCleave;
import javelin.model.unit.feat.attack.PowerAttack;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.town.labor.ecological.MeadHall;

public class Barbarian extends Kit{
	public static final Kit INSTANCE=new Barbarian();

	private Barbarian(){
		super("barbarian",Warrior.SINGLETON,RaiseStrength.SINGLETON);
	}

	@Override
	protected void define(){
		basic.add(Skill.SURVIVAL.getupgrade());
		basic.add(new BarbarianRage());
	}

	@Override
	protected void extend(UpgradeHandler h){
		extension.addAll(h.earth);
		extension.addAll(h.fire);
		extension.add(new FeatUpgrade(PowerAttack.SINGLETON));
		extension.add(new FeatUpgrade(BullRush.SINGLETON));
		extension.add(new FeatUpgrade(Cleave.SINGLETON));
		extension.add(new FeatUpgrade(GreatCleave.SINGLETON));
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