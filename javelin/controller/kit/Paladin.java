package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;

public class Paladin extends Kit {
	public static final Kit INSTANCE = new Paladin("paladin", Warrior.SINGLETON,
			RaiseCharisma.SINGLETON);

	Paladin(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(new CureLightWounds());
		basic.add(new Bless());
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.good);
		extension.addAll(h.magic);
		extension.addAll(h.schoolhealwounds);
		extension.addAll(h.schoolcompulsion);
	}
}