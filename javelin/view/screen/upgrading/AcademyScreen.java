package javelin.view.screen.upgrading;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Squad;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.military.MartialAcademy;
import javelin.view.screen.Option;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * @see MartialAcademy
 * @author alex
 */
public class AcademyScreen extends UpgradingScreen {
	protected Academy academy;
	Option pillage = null;

	/** Constructor. */
	public AcademyScreen(Academy academy, Town t) {
		super(academy.descriptionknown, t);
		this.academy = academy;
		stayopen = true;
		pillage = new Option("Pillage ($"
				+ SelectScreen.formatcost(academy.getspoils()) + ")", 0, 'p');
	}

	@Override
	protected void registertrainee(Order trainee) {
		academy.training.add(trainee);
	}

	@Override
	protected void onexit(Squad s, ArrayList<TrainingOrder> trainees) {
		if (s.members.size() == trainees.size()) {
			academy.stash += s.gold;
			if (academy.parking == null
					|| s.transport.price > academy.parking.price) {
				academy.parking = s.transport;
			}
		}
	}

	@Override
	protected ArrayList<Upgrade> getupgrades() {
		ArrayList<Upgrade> list = new ArrayList<Upgrade>(academy.upgrades);
		academy.sort(list);
		return list;
	}

	@Override
	public List<Option> getoptions() {
		List<Option> options = super.getoptions();
		if (academy.pillage && CrCalculator
				.calculateel(Squad.active.members) > academy.targetel) {
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

	@Override
	public String printinfo() {
		String training = academy.training.queue.isEmpty() ? ""
				: "Currently training: " + academy.training;
		return "Your squad currently has $"
				+ ShoppingScreen.formatcost(Squad.active.gold) + ". "
				+ training;
	}
}