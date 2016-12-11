package javelin.controller;

import java.util.Comparator;

import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;

public class DistanceComparator implements Comparator<Location> {
	Town reference;

	public DistanceComparator(Town reference) {
		this.reference = reference;
	}

	@Override
	public int compare(Location o1, Location o2) {
		return new Double(o1.distance(reference.x, reference.y))
				.compareTo(o2.distance(reference.x, reference.y));
	}
}
