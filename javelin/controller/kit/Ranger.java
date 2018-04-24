package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.skill.Survival;
import javelin.model.unit.Monster;

public class Ranger extends Kit {
	public static final Kit INSTANCE = new Ranger("ranger", Warrior.SINGLETON,
			RaiseDexterity.SINGLETON);

	private Ranger(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(Survival.SINGLETON);
		basic.addAll(UpgradeHandler.singleton.shots);
	}

	@Override
	public boolean allow(int bestability, int secondbest, Monster m) {
		return !m.ranged.isEmpty() && super.allow(bestability, secondbest, m);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.earth);
		extension.addAll(h.magic);
		extension.addAll(h.shots);
		extension.addAll(h.schoolabjuration);
		extension.addAll(h.schoolevocation);
	}
}