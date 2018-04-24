package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.GatherInformation;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.controller.upgrade.skill.UseMagicDevice;

public class Bard extends Kit {
	public static final Kit INSTANCE = new Bard("bard", Expert.SINGLETON,
			RaiseCharisma.SINGLETON);

	private Bard(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(Diplomacy.SINGLETON);
		basic.add(GatherInformation.SINGLETON);
		basic.add(Knowledge.SINGLETON);
		basic.add(UseMagicDevice.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.wind);
		extension.addAll(h.magic);
		extension.addAll(h.schoolabjuration);
		extension.addAll(h.schooltotem);
		extension.addAll(h.schoolcompulsion);
	}
}