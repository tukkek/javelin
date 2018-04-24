package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.skill.DisableDevice;
import javelin.controller.upgrade.skill.Search;
import javelin.controller.upgrade.skill.Stealth;

public class Rogue extends Kit {
	public static final Kit INSTANCE = new Rogue("rogue", Expert.SINGLETON,
			RaiseDexterity.SINGLETON);

	private Rogue(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(DisableDevice.SINGLETON);
		basic.add(Stealth.SINGLETON);
		basic.add(Search.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.wind);
		extension.addAll(h.evil);
		extension.addAll(h.combatexpertise);
		extension.addAll(h.shots);
	}
}