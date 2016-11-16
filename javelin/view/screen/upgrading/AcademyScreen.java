package javelin.view.screen.upgrading;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Squad;
import javelin.model.world.location.fortification.MartialAcademy;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Academy;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;

/**
 * @see MartialAcademy
 * @author alex
 */
public class AcademyScreen extends UpgradingScreen {
	final Academy academy;
	Option pillage = null;

	/** Constructor. */
	public AcademyScreen(Academy academy, Town t) {
		super(academy.descriptionknown, t);
		this.academy = academy;
		stayopen = false;
		pillage = new Option("Pillage ($"
				+ PurchaseScreen.formatcost(academy.getspoils()) + ")", 0, 'p');
	}

	@Override
	protected void registertrainee(Order trainee) {
		this.academy.training = (TrainingOrder) trainee;
	}

	@Override
	protected void onexit(Squad s) {
		if (s.members.isEmpty()) {
			academy.stash += s.gold;
			academy.parking = s.transport;
		}
	}

	@Override
	protected ArrayList<Upgrade> getupgrades() {
		return academy.upgrades;
	}

	@Override
	public List<Option> getoptions() {
		List<Option> options = super.getoptions();
		if (academy.pillage) {
			options.add(pillage);
		}
		return options;
	}

	@Override
	protected void sort(List<Option> options) {
		// don't sort
	}

	@Override
	protected boolean select(char feedback, List<Option> options) {
		if (academy.pillage && feedback == pillage.key) {
			academy.pillage();
			return true;
		}
		options.remove(pillage);
		return super.select(feedback, options);
	}
}