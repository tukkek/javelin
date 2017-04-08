package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.world.location.ConstructionSite;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;

public abstract class Build extends Labor {
	protected ConstructionSite site;
	protected Location previous;

	public abstract Location getgoal();

	public Build(String name, int cost, Location previous, Rank minimumrank) {
		super(name, cost, minimumrank);
		this.previous = previous;
		construction = true;
	}

	@Override
	protected void define() {
		// nothing
	}

	@Override
	public void done() {
		site.remove();
		site.goal.setlocation(site.getlocation());
		site.goal.place();
		if (!town.ishostile()) {
			site.goal.capture();
		}
		done(site.goal);
	}

	protected void done(Location goal) {

	}

	protected Point getsitelocation() {
		return town.getdistrict().getfreespaces().get(0);
	}

	@Override
	public void start() {
		super.start();
		site = new ConstructionSite(getgoal(), previous, this);
		if (town.ishostile()) {
			site.realm = town.realm;
		}
		site.setlocation(getsitelocation());
		site.place();
	}

	@Override
	public boolean validate(District d) {
		return super.validate(d) && site == null ? !d.getfreespaces().isEmpty()
				: d.getlocations().contains(site);
	}

	@Override
	public void cancel() {
		super.cancel();
		if (previous != null) {
			previous.place();
		}
	}
}