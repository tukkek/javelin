package javelin.model.world.location.dungeon.crawler;

import java.util.Set;

import javelin.controller.Point;
import tyrant.mikera.engine.RPG;

/**
 * Carves a corridor along the x ais.
 * 
 * @author alex
 */
public class HorizontalCorridor extends Crawler {
	protected int step = RPG.r(1, 2) == 1 ? -1 : +1;

	/**
	 * @param step
	 *            Size of each {@link #step()}.
	 * @see Crawler#Crawler(Point, Set)
	 */
	public HorizontalCorridor(Point start, Set<Point> used) {
		super(start, used, RPG.r(5, 7));
	}

	@Override
	public void step() {
		walker.x += step;
	}
}