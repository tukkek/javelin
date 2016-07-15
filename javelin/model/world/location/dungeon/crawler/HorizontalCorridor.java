package javelin.model.world.location.dungeon.crawler;

import java.util.Set;

import javelin.controller.Point;

/**
 * Carves a corridor along the x ais.
 * 
 * @author alex
 */
public class HorizontalCorridor extends Crawler {
	int step;

	/**
	 * @param step
	 *            Size of each {@link #step()}.
	 * @see Crawler#Crawler(Point, Set)
	 */
	public HorizontalCorridor(Point start, Set<Point> used, int step) {
		super(start, used);
		this.step = step;
	}

	@Override
	public void step() {
		walker.x += step;
	}
}