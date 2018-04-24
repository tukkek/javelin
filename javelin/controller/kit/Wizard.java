package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.Aristocrat;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.skill.Concentration;
import javelin.controller.upgrade.skill.Spellcraft;
import javelin.model.unit.abilities.spell.evocation.MagicMissile;

public class Wizard extends Kit {
	public static final Kit INSTANCE = new Wizard("wizard",
			Aristocrat.SINGLETON, RaiseIntelligence.SINGLETON);

	private Wizard(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(new MagicMissile());
		basic.add(Concentration.SINGLETON);
		basic.add(Spellcraft.SINGLETON);
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.magic);
		extension.addAll(h.schoolabjuration);
		extension.addAll(h.schoolconjuration);
		extension.addAll(h.schoolevocation);
		extension.addAll(h.schooltransmutation);
		extension.addAll(h.schoolcompulsion);
		extension.addAll(h.schooltotem);
	}
}