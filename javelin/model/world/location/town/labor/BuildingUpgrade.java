package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.Realm;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.District;

public abstract class BuildingUpgrade extends Build {
	protected int upgradelevel;

	public BuildingUpgrade(String newname, int cost, int upgradelevel,
			Location previous) {
		super("Upgrade " + previous.toString().toLowerCase() + " to "
				+ newname.toLowerCase(), cost, previous);
		this.upgradelevel = upgradelevel;
	}

	@Override
	abstract public Location getgoal();

	@Override
	public void start() {
		previous.remove();
		super.start();
	}

	@Override
	public boolean validate(District d) {
		if (site != null && site.ishostile() && d.town.ishostile()) {
			Realm.equals(site.realm, d.town.realm);
		}
		// if (town.ishostile()) {
		// Realm r = d.town.realm;
		// if (!previous.realm.equals(r)
		// || site != null && !site.realm.equals(r)) {
		// return false;
		// }
		// } else {
		// if (previous.ishostile() || site != null && site.ishostile()) {
		// return false;
		// }
		// }
		return site != null || d.getlocations().contains(previous);
	}

	@Override
	protected Point getsitelocation() {
		return previous.getlocation();
	}

	@Override
	protected void done(WorldActor goal) {
		/* TODO goal should be a Location and #raiselevel a Location method */
		if (goal instanceof Fortification) {
			((Fortification) goal).raiselevel(upgradelevel);
		}
	}
}
