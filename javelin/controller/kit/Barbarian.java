package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.skill.Survival;
import javelin.model.unit.abilities.spell.enchantment.compulsion.BarbarianRage;

public class Barbarian extends Kit {
	public static final Kit INSTANCE = new Barbarian("barbarian",
			Warrior.SINGLETON, RaiseStrength.SINGLETON);

	private Barbarian(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(Survival.SINGLETON);
		basic.add(new BarbarianRage());
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.earth);
		extension.addAll(h.fire);
		extension.addAll(h.powerattack);
	}
}