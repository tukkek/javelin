package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.TrainingOrder;
import javelin.view.screen.upgrading.AcademyScreen;
import javelin.view.screen.upgrading.UpgradingScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * A place where units can go to learn about a general topic - be it physical
 * feats or intellectual or magical prowess.
 * 
 * @author alex
 */
public abstract class Academy extends Fortification {
	/** Currently training unit. */
	public TrainingOrder training = null;
	/** Money {@link #training} unit had before entering here (if alone). */
	public int stash;
	/** Upgrades that can be learned here. */
	public ArrayList<Upgrade> upgrades;
	public boolean pillage = true;

	/**
	 * See {@link Fortification#Fortification(String, String, int, int)}.
	 * 
	 * @param upgradesp
	 */
	public Academy(String descriptionknown, String descriptionunknown,
			int minlevel, int maxlevel, HashSet<Upgrade> upgradesp) {
		super(descriptionknown, descriptionunknown, minlevel, maxlevel);
		upgrades = new ArrayList<Upgrade>(upgradesp);
		sort(upgrades);
	}

	/**
	 * @param upgrades
	 *            {@link #upgrades}, to be sorted.
	 */
	protected void sort(ArrayList<Upgrade> upgrades) {
		upgrades.sort(new Comparator<Upgrade>() {
			@Override
			public int compare(Upgrade o1, Upgrade o2) {
				return o1.name.compareTo(o2.name);
			}
		});
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		if (training != null) {
			if (Squad.active.hourselapsed < training.completionat) {
				long daysleft = Math.round(Math.ceil(
						(training.completionat - Squad.active.hourselapsed)
								/ 24f));
				Game.messagepanel.clear();
				Game.message(training.trained + "will complete training in "
						+ daysleft + " day(s)...", null, Delay.NONE);
				Game.getInput();
			} else {
				UpgradingScreen.completetraining(training, this,
						training.trained).gold += stash;
				stash = 0;
				training = null;
			}
			return true;
		}
		new AcademyScreen(this, null).show();
		return true;
	}

	@Override
	public boolean hasupgraded() {
		return training != null
				&& Squad.active.hourselapsed >= training.completionat;
	}

}