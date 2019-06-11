/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class ArmorClassFactor extends CrFactor{
	@Override
	public float calculate(final Monster monster){
		return monster.armor*.1f;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler){
	}
}