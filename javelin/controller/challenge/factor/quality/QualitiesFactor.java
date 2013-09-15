package javelin.controller.challenge.factor.quality;

import java.util.ArrayList;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.upgrade.FastHealing;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.Vision;
import javelin.model.unit.Monster;

public class QualitiesFactor extends CrFactor {

	@Override
	public float calculate(Monster monster) {
		if (monster.vision == 2) {
			return .2f;
		}
		if (monster.vision == 1) {
			return .1f;
		}
		return 0;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.defensive.add(new FastHealing());
		handler.defensive
				.add(new javelin.controller.upgrade.ability.DamageReduction());
		ArrayList<Upgrade> vision = handler.addset();
		vision.add(new Vision("Low-light vision", 1));
		vision.add(new Vision("Darkvision", 2));
		ArrayList<Upgrade> resistance = handler.addset();
		resistance
				.add(new javelin.controller.upgrade.ability.SpellResistance());
		resistance.add(new javelin.controller.upgrade.ability.SpellImmunity());
		resistance
				.add(new javelin.controller.upgrade.ability.EnergyResistance());
		resistance.add(new javelin.controller.upgrade.ability.EnergyImmunity());
	}
}
