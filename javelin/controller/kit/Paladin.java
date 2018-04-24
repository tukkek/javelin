package javelin.controller.kit;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Bless;

public class Paladin extends Kit {
	public static final Kit INSTANCE = new Paladin();

	Paladin() {
		super("paladin", Warrior.SINGLETON, RaiseCharisma.SINGLETON, "Keeper",
				"Guardian", "Paladin", "Justicar");
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

	@Override
	public boolean allow(int bestability, int secondbest, Monster m) {
		return Boolean.TRUE.equals(m.good)
				&& Cleric.INSTANCE.allow(bestability, secondbest, m);
	}
}