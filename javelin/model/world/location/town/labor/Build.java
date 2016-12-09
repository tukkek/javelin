package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.world.location.Construction;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import tyrant.mikera.engine.RPG;

public abstract class Build extends Labor {
	Construction site;
	Location previous;

	public abstract Location getgoal();

	public Build(String name, int cost, Location previous) {
		super(name, cost);
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
		site.goal.capture();
	}

	protected Point getsitelocation() {
		return RPG.pick(town.getdistrict().findbuildingarea());
	}

	@Override
	public void start() {
		super.start();
		site = new Construction(getgoal(), previous, this);
		site.setlocation(getsitelocation());
		site.place();
	}

	@Override
	public boolean validate(District d) {
		return site == null ? !d.findbuildingarea().isEmpty() : d.getlocations().contains(site);
	}

	@Override
	public void cancel() {
		super.cancel();
		if (previous != null) {
			previous.place();
		}
	}
}