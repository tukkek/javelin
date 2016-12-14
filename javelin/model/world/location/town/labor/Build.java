package javelin.model.world.location.town.labor;

import javelin.controller.Point;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Construction;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import tyrant.mikera.engine.RPG;

public abstract class Build extends Labor {
	protected Construction site;
	protected Location previous;

	public abstract Location getgoal();

	public Build(String name, int cost, Location previous) {
		super(name, cost);
		this.previous = previous;
		construction = true;
	}

	public Build(String name, int cost) {
		this(name, cost, null);
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

	protected void done(WorldActor goal) {

	}

	protected Point getsitelocation() {
		return RPG.pick(town.getdistrict().getfreespaces());
	}

	@Override
	public void start() {
		super.start();
		site = new Construction(getgoal(), previous, this);
		if (town.ishostile()) {
			site.realm = town.realm;
		}
		site.setlocation(getsitelocation());
		site.place();
	}

	@Override
	public boolean validate(District d) {
		return site == null ? !d.getfreespaces().isEmpty()
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