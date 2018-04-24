package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.skill.DisableDevice;
import javelin.controller.upgrade.skill.Search;
import javelin.controller.upgrade.skill.Stealth;
import javelin.model.unit.Monster;

public class Rogue extends Kit {
	public static final Kit INSTANCE = new Rogue();

	private Rogue() {
		super("rogue", Expert.SINGLETON, RaiseDexterity.SINGLETON, "Cutpurse",
				"Burglar", "Rogue", "Shadow");
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

	@Override
	public boolean allow(int bestability, int secondbest, Monster m) {
		return super.allow(bestability, secondbest, m)
				&& !Boolean.TRUE.equals(m.good)
				&& !Boolean.TRUE.equals(m.lawful);
	}
}