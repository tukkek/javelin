package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.skill.Heal;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureModerateWounds;

public class Cleric extends Kit {
	/**
	 * TODO might be intesteing to separate into Good and Evil clerc. Evil
	 * Cleric academies could only be buitl on criminal + religious cities.
	 */
	public static final Kit INSTANCE = new Cleric("cleric",
			Aristocrat.SINGLETON, RaiseWisdom.SINGLETON);

	private Cleric(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(new CureModerateWounds());
		basic.add(Knowledge.SINGLETON);
		basic.add(Heal.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.evil);
		extension.addAll(h.good);
		extension.addAll(h.schoolhealwounds);
		extension.addAll(h.schoolwounding);
		extension.addAll(h.schoolrestoration);
		extension.addAll(h.schooldivination);
		extension.addAll(h.schoolcompulsion);
		extension.addAll(h.schoolnecromancy);
	}
}