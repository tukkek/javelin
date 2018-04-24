package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.GatherInformation;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.controller.upgrade.skill.UseMagicDevice;

public class Bard extends Kit {
	public static final Kit INSTANCE = new Bard();

	private Bard() {
		super("bard", Expert.SINGLETON, RaiseCharisma.SINGLETON, "Joker",
				"Minstrel", "Bard", "Maestro");
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