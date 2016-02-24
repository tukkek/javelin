package javelin.controller.challenge.factor.quality;

import java.util.HashSet;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.quality.Quality;
import javelin.controller.upgrade.FastHealing;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.Vision;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class QualitiesFactor extends CrFactor {

	@Override
	public float calculate(Monster monster) {
		float rate = 0;
		HashSet<String> calculated = new HashSet<String>();
		for (Quality q : Quality.qualities) {
			if (calculated.add(q.getClass().getTypeName()) && q.has(monster)) {
				rate += q.rate(monster);
			}
		}
		return rate;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.water.add(new FastHealing());
		handler.good.add(new javelin.controller.upgrade.DamageReduction());

		handler.evil.add(new Vision("Low-light vision", 1));
		handler.evil.add(new Vision("Darkvision", 2));

		handler.magic.add(new javelin.controller.upgrade.SpellResistance());
		handler.magic.add(new javelin.controller.upgrade.EnergyResistance());

		handler.good.add(new javelin.controller.upgrade.SpellImmunity());
		handler.good.add(new javelin.controller.upgrade.EnergyImmunity());
	}
}
