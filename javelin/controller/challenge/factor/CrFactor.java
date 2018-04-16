/**
 * 
 */
package javelin.controller.challenge.factor;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;

/**
 * Describes one step in the calculation of a {@link Monster}'s challenge
 * rating.
 * 
 * @se {@link ChallengeCalculator}
 * 
 * @author alex
 */
public abstract class CrFactor implements Comparable<CrFactor> {
	public abstract float calculate(Monster m);

	@Override
	public int compareTo(final CrFactor f) {
		return Integer.compare(getClass().hashCode(), f.getClass().hashCode());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public void registerupgrades(UpgradeHandler handler) {
		return;
	}

	public String log(Monster m) {
		return "";
	}
}