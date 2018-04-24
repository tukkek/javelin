package javelin.controller.kit;

import javelin.controller.DamageEffect;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.damage.EffectUpgrade;
import javelin.controller.upgrade.skill.Disguise;
import javelin.controller.upgrade.skill.Stealth;
import javelin.model.unit.feat.skill.Deceitful;

public class Assassin extends Kit {
	public static final Kit INSTANCE = new Assassin("assassin",
			Expert.SINGLETON, RaiseDexterity.SINGLETON);

	private Assassin(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
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
}