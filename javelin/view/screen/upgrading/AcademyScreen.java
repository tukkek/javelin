package javelin.view.screen.upgrading;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
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
		Combatant c = ((TrainingOrder) trainee).trained;
		Squad.active.equipment.remove(c.toString());
		Squad.active.remove(c);
	}

	@Override
	protected void onexit(ArrayList<TrainingOrder> trainees) {
		if (Squad.active.members.size() == trainees.size()) {
			academy.stash += Squad.active.gold;
			if (academy.parking == null
					|| Squad.active.transport.price > academy.parking.price) {
				academy.parking = Squad.active.transport;
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
		if (academy.pillage && ChallengeCalculator
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

	@Override
	public TrainingOrder createorder(Combatant c, Combatant original,
			float xpcost) {
		return new TrainingOrder(c, Squad.active.equipment.get(c.id),
				c.toString(), xpcost, original);
	}

	@Override
	public ArrayList<Combatant> gettrainees() {
		return Squad.active.members;
	}

	@Override
	public int getgold() {
		return Squad.active.gold;
	}

	@Override
	public void pay(int goldpieces) {
		Squad.active.gold -= goldpieces;
	}
}