package javelin.model.world.location.town.labor.industrious;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Mine;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Build;

public class BuildMine extends Build {
	public BuildMine() {
		super("Build mine", 10, null, Rank.HAMLET);
	}

	@Override
	public Location getgoal() {
		return new Mine();
	}

	@Override
	public boolean validate(District d) {
		return super.validate(d) && d.getlocationtype(Mine.class).isEmpty()
				&& getsitelocation() != null;
	}

	@Override
	protected Point getsitelocation() {
		ArrayList<Point> free = town.getdistrict().getfreespaces();
		for (Point p : free) {
			if (Terrain.get(p.x, p.y).equals(Terrain.MOUNTAINS)) {
				return p;
			}
		}
		return null;
	}
}
