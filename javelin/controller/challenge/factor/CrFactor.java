/**
 * 
 */
package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;

public abstract class CrFactor implements Comparable<CrFactor> {
	public abstract float calculate(Monster monster);

	@Override
	public int compareTo(final CrFactor arg0) {
		return getClass().toString().compareTo(arg0.getClass().toString());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public void listupgrades(UpgradeHandler handler) {
		return;
	}
}