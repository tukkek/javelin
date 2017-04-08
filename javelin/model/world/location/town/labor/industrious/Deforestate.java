package javelin.model.world.location.town.labor.industrious;

import java.util.ArrayList;
import java.util.Collections;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.world.World;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.Growth;
import javelin.model.world.location.town.labor.Labor;
import tyrant.mikera.engine.RPG;

public class Deforestate extends Labor {
	public Deforestate() {
		super("Deforestate", 7, Rank.HAMLET);
	}

	@Override
	protected void define() {
		// nothing
	}

	@Override
	public void done() {
		Point forest = getforest(town.getdistrict());
		if (forest == null) {
			return;
		}
		final int adjacentmountains = Terrain.search(forest, Terrain.MOUNTAINS,
				1, World.getseed());
		final int adjacenthills = Terrain.search(forest, Terrain.HILL, 1,
				World.getseed());
		World.getseed().map[forest.x][forest.y] = adjacentmountains > 0
				|| RPG.r(1, 8) <= adjacenthills ? Terrain.HILL : Terrain.PLAIN;
	}

	@Override
	public void work(float step) {
		float work = Math.min(cost - progress, step) * 1.5f;
		ArrayList<Labor> projects = town.governor.getprojects();
		if (projects.size() == 1) {
			Labor growth = new Growth().generate(town);
			growth.start();
			growth.work(work);
		} else {
			work = work / (projects.size() - 1);
			for (Labor l : new ArrayList<Labor>(projects)) {
				if (l != this) {
					l.work(work);
				}
			}
		}
		super.work(step);
	}

	@Override
	public boolean validate(District d) {
		return super.validate(d) && getforest(d) != null;
	}

	Point getforest(District d) {
		ArrayList<Point> area = new ArrayList<Point>(d.getarea());
		Collections.shuffle(area);
		for (Point p : area) {
			if (Terrain.get(p.x, p.y).equals(Terrain.FOREST)) {
				return p;
			}
		}
		return null;
	}

}
