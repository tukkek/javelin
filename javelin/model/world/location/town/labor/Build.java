package javelin.model.world.location.town.labor;

import javelin.model.world.location.Construction;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Inn;
import tyrant.mikera.engine.RPG;

public abstract class Build extends Labor {
	Construction site;
	Location previous;

	public abstract Inn getgoal();

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
		site.goal.x = site.x;
		site.goal.y = site.y;
		site.goal.place();
		site.goal.capture();
	}

	@Override
	public void start() {
		super.start();
		site = new Construction(getgoal(), previous, this);
		site.setlocation(RPG.pick(town.getdistrict().findbuildingarea()));
		site.place();
	}

	@Override
	public boolean validate(District d) {
		return site == null ? !d.findbuildingarea().isEmpty() : d.getlocations().contains(site);
	}
}