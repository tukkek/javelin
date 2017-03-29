package javelin.model.world.location.town.labor.military;

import java.util.ArrayList;
import java.util.Collections;

import javelin.model.unit.Monster;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Dwelling;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Build;

public class BuildDwelling extends Build {
	public static final int CRMULTIPLIER = 10;

	Dwelling goal = null;

	public BuildDwelling() {
		super("Build dwelling", 0, null, Town.HAMLET);
	}

	@Override
	public Location getgoal() {
		return goal;
	}

	@Override
	protected void define() {
		ArrayList<Monster> candidates = Dwelling.getcandidates(town.x, town.y);
		Collections.shuffle(candidates);
		for (Monster m : candidates) {
			if (m.challengerating <= town.population) {
				goal = new Dwelling(m);
				name += ": " + m.toString().toLowerCase();
				cost = getcost(m);
				return;
			}
		}
	}

	public static int getcost(Monster m) {
		return Math.max(1, Math.round(m.challengerating * CRMULTIPLIER));
	}

	@Override
	public boolean validate(District d) {
		return super.validate(d) && goal != null;
	}
}
