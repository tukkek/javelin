package javelin.view.screen.town.option;

import javelin.controller.upgrade.Upgrade;

public class UpgradeOption extends Option {
	public final Upgrade u;

	public UpgradeOption(final Upgrade u) {
		super(u.name, 0);
		this.u = u;
	}
}