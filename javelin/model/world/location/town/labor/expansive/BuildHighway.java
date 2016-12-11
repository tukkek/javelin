package javelin.model.world.location.town.labor.expansive;

import javelin.controller.Point;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.Town.Rank;

public class BuildHighway
		extends javelin.model.world.location.town.labor.expansive.BuildRoad {

	public BuildHighway() {
		super("Build highway");
	}

	@Override
	protected boolean hasroad(Point p) {
		return World.highways[p.x][p.y];
	}

	@Override
	protected String name(Town target) {
		return "Build highway to " + target;
	}

	@Override
	protected void build(Point p) {
		World.highways[p.x][p.y] = true;
	}

	@Override
	protected float getcost(Point p) {
		float cost = super.getcost(p);
		if (!super.hasroad(p)) {
			cost += cost;
		}
		return cost;
	}

	@Override
	protected void define() {
		if (town.getrank() < Rank.TOWN.ordinal() + 1) {
			/* requires town or city */
			return;
		}
		super.define();
	}
}
