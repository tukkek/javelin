package javelin.model.world.place.town.research;

import javelin.controller.upgrade.Upgrade;
import javelin.model.world.place.town.Town;
import javelin.view.screen.town.ResearchScreen;

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
		t.upgrades.add(u);
	}

	@Override
	protected boolean isrepeated(Town t) {
		for (Upgrade o : t.upgrades) {
			if (o.equals(u)) {
				return true;
			}
		}
		return false;
	}
}
