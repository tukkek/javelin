package javelin.model.world.location.town.research;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Academy;
import javelin.model.world.location.town.Town;

public class ResearchUpgrade extends Research {

	private Upgrade u = null;

	public ResearchUpgrade(Upgrade u) {
		super("Upgrade: " + u.name,
				"Adds this upgrade to the town's academies", 1);
		this.u = u;
	}

	public ResearchUpgrade() {
		super(null, null, 0);
		factory = true;
	}

	@Override
	public Research generate(Town t) {
		ArrayList<Academy> targets = gettargets(t);
		if (targets.isEmpty()) {
			return null;
		}
		ArrayList<Upgrade> upgrades =
				new ArrayList<Upgrade>(
						UpgradeHandler.singleton.getupgrades(t.realm));
		Collections.shuffle(upgrades);
		for (Upgrade u : upgrades) {
			if (!targets.get(0).upgrades.contains(u)) {
				return new ResearchUpgrade(u);
			}
		}
		return null;
	}

	@Override
	public void play(Town t) {
		for (Academy a : gettargets(t)) {
			a.upgrades.add(u);
		}
	}

	private ArrayList<Academy> gettargets(Town t) {
		ArrayList<Academy> targets = new ArrayList<Academy>();
		for (Location l : t.getdistrict()) {
			Academy a = l.getClass().equals(Academy.class) ? (Academy) l : null;
			if (a != null && !a.full()) {
				targets.add(a);
			}
		}
		return targets;
	}

	@Override
	public boolean validate(Town t) {
		return !gettargets(t).isEmpty();
	}
}
