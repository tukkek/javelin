package javelin.controller.challenge.factor.quality;

import java.util.HashSet;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.quality.Quality;
import javelin.controller.upgrade.UpgradeHandler;
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
		for (Quality q : Quality.qualities) {
			q.listupgrades(handler);
		}
	}
}
