package javelin.controller.kit;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;

public class Monk extends Kit {
	public static final Kit INSTANCE = new Monk("monk", Warrior.SINGLETON,
			RaiseStrength.SINGLETON);

	private Monk(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		super(name, classadvancement, raiseability);
	}

	@Override
	protected void define() {
		basic.add(RaiseDexterity.SINGLETON);
		basic.add(Acrobatics.SINGLETON);
		for (Feat f : new Feat[] { Acrobatic.SINGLETON,
				ImprovedInitiative.SINGLETON, LightningReflexes.SINGLETON }) {
			basic.add(new FeatUpgrade(f));
		}
	}

	@Override
	protected void extend(UpgradeHandler h) {
		extension.addAll(h.water);
		extension.addAll(h.earth);
		extension.addAll(h.combatexpertise);
		for (Discipline d : Discipline.DISCIPLINES) {
			extension.add(d.trainingupgrade);
		}
	}
}