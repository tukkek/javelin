package javelin.model.world.town.research;

import javelin.controller.upgrade.Upgrade;
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen;
import javelin.view.screen.town.option.UpgradeOption;

/**
 * Allows a new native (1 labor) or foreign (2 labor) {@link Upgrade} to be
 * trained in this town.
 * 
 * @author alex
 */
public class UpgradeResearch extends Research {

	private Upgrade u;

	public UpgradeResearch(Upgrade u, int cost) {
		super(u.name, cost);
		this.u = u;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.upgrades.add(new UpgradeOption(u));
	}

	@Override
	protected boolean isrepeated(Town t) {
		for (Option o : t.upgrades) {
			UpgradeOption uo = (UpgradeOption) o;
			if (uo.u.equals(u)) {
				return true;
			}
		}
		return false;
	}
}
