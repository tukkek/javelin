package javelin.controller.kit;

import javelin.controller.DamageEffect;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.damage.EffectUpgrade;
import javelin.controller.upgrade.skill.Disguise;
import javelin.controller.upgrade.skill.Stealth;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.skill.Deceitful;

public class Assassin extends Kit {
	public static final Kit INSTANCE = new Assassin();

	private Assassin() {
		super("assassin", Expert.SINGLETON, RaiseDexterity.SINGLETON, "Thug",
				"Cutthroat", "Assassin", "Ninja");
	}

	@Override
	protected void define() {
		basic.add(Disguise.SINGLETON);
		basic.add(Stealth.SINGLETON);
		basic.add(RaiseCharisma.SINGLETON);
		basic.add(new FeatUpgrade(Deceitful.SINGLETON));
		basic.add(new EffectUpgrade(DamageEffect.POISON));

	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.evil);
		extension.addAll(h.wind);
		extension.addAll(h.combatexpertise);
	}

	@Override
	public boolean allow(int bestability, int secondbest, Monster m) {
		return Boolean.FALSE.equals(m.good)
				&& super.allow(bestability, secondbest, m);
	}
}