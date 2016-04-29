package javelin.view.screen.upgrading;

import java.util.List;

import javelin.controller.upgrade.Upgrade;
import javelin.model.world.Squad;
import javelin.model.world.place.guarded.Academy;
import javelin.model.world.place.town.Order;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.TrainingOrder;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;

/**
 * @see Academy
 * @author alex
 */
public class AcademyScreen extends UpgradingScreen {
	final Academy academy;
	Option pillage;

	/** Constructor. */
	public AcademyScreen(Academy academy, String name, Town t) {
		super(name, t);
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
			this.academy.stash = s.gold;
		}
	}

	@Override
	protected List<Upgrade> getupgrades() {
		return academy.upgrades;
	}

	@Override
	public List<Option> getoptions() {
		List<Option> options = super.getoptions();
		options.add(pillage);
		return options;
	}

	@Override
	protected void sort(List<Option> options) {
		// don't sort
	}

	@Override
	protected boolean select(char feedback, List<Option> options) {
		if (feedback == pillage.key) {
			academy.pillage();
			return true;
		}
		options.remove(pillage);
		return super.select(feedback, options);
	}
}