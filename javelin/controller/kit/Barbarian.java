package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.enchantment.compulsion.BarbarianRage;
import javelin.model.unit.skill.Skill;

public class Barbarian extends Kit{
	public static final Kit INSTANCE=new Barbarian();

	private Barbarian(){
		super("barbarian",Warrior.SINGLETON,RaiseStrength.SINGLETON,"Whelp",
				"Savage","Barbarian","Chieftain");
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
		extension.addAll(h.powerattack);
	}

	@Override
	public boolean allow(int bestability,int secondbest,Monster m){
		return !m.alignment.islawful()&&super.allow(bestability,secondbest,m);
	}
}