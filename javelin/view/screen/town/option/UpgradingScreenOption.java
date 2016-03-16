package javelin.view.screen.town.option;

import javelin.model.world.town.Town;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.town.UpgradingScreen;

/**
 * @see UpgradeOption
 * @author alex
 */
public class UpgradingScreenOption extends ScreenOption {

	public UpgradingScreenOption(String name, Town t) {
		super(name, t);
	}

	@Override
	public SelectScreen show() {
		return new UpgradingScreen(t);
	}

}
