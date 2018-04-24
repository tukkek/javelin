package javelin.controller.kit;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseStrength;
import javelin.controller.upgrade.ability.RaiseWisdom;
import javelin.controller.upgrade.classes.Warrior;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;

public class Monk extends Kit {
	public static final Kit INSTANCE = new Monk();

	private Monk() {
		super("monk", Warrior.SINGLETON, RaiseStrength.SINGLETON, "Novice",
				"Disciple", "Monk", "Master");
	}

	@Override
	protected void define() {
		basic.add(RaiseWisdom.SINGLETON);
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

	@Override
	public boolean allow(int bestability, int secondbest, Monster m) {
		return !Boolean.FALSE.equals(m.lawful)
				&& super.allow(bestability, secondbest, m);
	}
}