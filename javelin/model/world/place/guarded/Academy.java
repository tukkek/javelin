package javelin.model.world.place.guarded;

import java.util.List;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.world.Squad;
import javelin.model.world.place.town.TrainingOrder;
import javelin.view.screen.upgrading.AcademyScreen;
import javelin.view.screen.upgrading.UpgradingScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Allows a player to learn one upgrade set.
 * 
 * @author alex
 */
public class Academy extends GuardedPlace {
	public static final boolean DEBUG = false;
	public TrainingOrder training = null;
	public int stash;
	public List<Upgrade> upgrades;

	public Academy(List<Upgrade> upgrades) {
		super("An academy (" + getname(upgrades) + ")", "An academy", 6, 10);
		this.upgrades = upgrades;
		gossip = true;
		if (DEBUG) {
			garrison.clear();
		}
	}

	static String getname(List<Upgrade> type) {
		if (type == UpgradeHandler.singleton.power) {
			return "power attacks";
		}
		if (type == UpgradeHandler.singleton.shots) {
			return "shooting range";
		}
		if (type == UpgradeHandler.singleton.expertise) {
			return "combat expertise";
		}
		throw new RuntimeException("Unknown academy type #academy");
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
				Game.message(training.trained
						+ "'s training will be ready complete in " + daysleft
						+ " day(s)...", null, Delay.NONE);
				Game.getInput();
			} else {
				UpgradingScreen.completetraining(training, this,
						training.trained).gold += stash;
				stash = 0;
				training = null;
			}
			return true;
		}
		new AcademyScreen(this, "Academy", null).show();
		return true;
	}

	@Override
	public boolean isupgrading() {
		return training != null
				&& Squad.active.hourselapsed >= training.completionat;
	}
}
