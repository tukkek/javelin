package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;

public class Ranger extends Kit {
	public static final Kit INSTANCE = new Ranger();

	private Ranger() {
		super("ranger", Warrior.SINGLETON, RaiseDexterity.SINGLETON, "Wanderer",
				"Hunter", "Ranger", "Warden");
	}

	@Override
	protected void define() {
		basic.add(Skill.SURVIVAL.getupgrade());
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