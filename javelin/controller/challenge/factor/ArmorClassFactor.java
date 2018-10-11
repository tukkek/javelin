/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.controller.upgrade.NaturalArmor;
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
		handler.earth.add(new NaturalArmor("Natural armor: leather",2));
		handler.earth.add(new NaturalArmor("Natural armor: scales",4));

		handler.fire.add(new NaturalArmor("Natural armor: plates",8));
		handler.fire.add(new NaturalArmor("Natural armor: iron golem",22));

		handler.earth.add(new NaturalArmor("Natural armor: clay golem",14));
		handler.earth.add(new NaturalArmor("Natural armor: stone golem",18));
	}
}