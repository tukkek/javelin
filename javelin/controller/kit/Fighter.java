package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.damage.MeleeDamage;

public class Fighter extends Kit {
	public static final Kit INSTANCE = new Fighter();

	private Fighter() {
		super("fighter", Warrior.SINGLETON, RaiseStrength.SINGLETON,
				"Swashbuckler", "Veteran", "Fighter", "Champion");
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