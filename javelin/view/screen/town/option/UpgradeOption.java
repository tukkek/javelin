package javelin.view.screen.town.option;

import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.view.screen.town.UpgradingScreen;

/**
 * @see UpgradingScreen
 * @author alex
 */
public class UpgradeOption extends Option {
	public final Upgrade u;

	public UpgradeOption(final Upgrade u) {
		super(u.name, 0);
		this.u = u;
		if (u instanceof Spell) {
			name = "Spell: " + name.toLowerCase();
		}
	}
}