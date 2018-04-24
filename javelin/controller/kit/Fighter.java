package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;

public class Fighter extends Kit {
	public static final Kit INSTANCE = new Fighter("fighter", Warrior.SINGLETON,
			RaiseStrength.SINGLETON);

	private Fighter(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(new MeleeDamage());
		basic.addAll(UpgradeHandler.singleton.powerattack);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.fire);
		extension.addAll(h.earth);
		extension.addAll(h.combatexpertise);
		extension.addAll(h.shots);
	}
}