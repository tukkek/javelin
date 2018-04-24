package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.skill.Survival;
import javelin.model.unit.abilities.spell.conjuration.Summon;

public class Druid extends Kit {
	public static final Kit INSTANCE = new Druid("druid", Commoner.SINGLETON,
			RaiseWisdom.SINGLETON);

	private Druid(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(new Summon("Small monstrous centipede", 1));
		basic.add(new Summon("Dire rat", 1));
		basic.add(new Summon("Eagle", 1));
		basic.add(Survival.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.earth);
		extension.addAll(h.fire);
		extension.addAll(h.water);
		extension.addAll(h.wind);
		extension.addAll(h.schoolhealwounds);
		extension.addAll(h.schooltotem);
		extension.addAll(h.schooltransmutation);
		extension.addAll(h.schooldivination);
		int summons = extension.size();
		for (int i = 0; i < summons; i++) {
			extension.add(Summon.getrandom());
		}
	}
}