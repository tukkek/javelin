package javelin.view.screen.upgrading;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Order;
import javelin.model.world.location.town.Town;

/** {@link Town} version of an upgrading screen. */
public class TownUpgradingScreen extends UpgradingScreen {

	/** Initializes but doesn't show screen. */
	public TownUpgradingScreen(final Town town) {
		super("Upgrade:", town);
	}

	@Override
	protected void onexit(Squad s) {
		Collections.sort(town.training.queue, new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				return Math.round(o1.completionat - o2.completionat);
			}
		});
		if (Squad.getall(Squad.class).isEmpty()) {
			town.stash = s.gold;
		}
	}

	@Override
	protected void registertrainee(Order trainee) {
		town.training.add(trainee);
	}

	@Override
	protected HashSet<Upgrade> getupgrades() {
		return town.upgrades;
	}

}
