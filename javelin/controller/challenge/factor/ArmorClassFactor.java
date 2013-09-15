/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import java.util.ArrayList;

import javelin.controller.upgrade.NaturalArmor;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;

public class ArmorClassFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		return monster.armor * .1f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		ArrayList<Upgrade> list = handler.addset();
		list.add(new NaturalArmor("Natural armor: leather", 2));
		list.add(new NaturalArmor("Natural armor: scales", 4));
		list.add(new NaturalArmor("Natural armor: plates", 8));
		list.add(new NaturalArmor("Natural armor: clay golem", 14));
		list.add(new NaturalArmor("Natural armor: stone golem", 18));
		list.add(new NaturalArmor("Natural armor: iron golem", 22));
	}
}