package javelin.model.world.location.dungeon.crawler;

import java.util.Set;

import javelin.controller.Point;

/**
 * Carves a corridor along the y axis.
 * 
 * @author alex
 */
public class VerticalCorridor extends Crawler {
	int step;

	/**
	 * @param step
	 *            Size of each step.
	 * @see Crawler#Crawler(Point, Set)
	 */
	public VerticalCorridor(Point start, Set<Point> used, int step) {
		super(start, used);
		this.step = step;
	}

	@Override
	public void step() {
		walker.y += step;
	}
}