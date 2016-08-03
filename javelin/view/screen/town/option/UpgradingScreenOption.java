package javelin.view.screen.town.option;

import javelin.model.world.location.town.Town;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.upgrading.TownUpgradingScreen;

/**
 * @see UpgradeOption
 * @author alex
 */
public class UpgradingScreenOption extends ScreenOption {
	/** Constructor. */
	public UpgradingScreenOption(String string, Town town, char c) {
		super(string, town, c);
	}

	@Override
	public SelectScreen show() {
		return new TownUpgradingScreen(t);
	}

}
