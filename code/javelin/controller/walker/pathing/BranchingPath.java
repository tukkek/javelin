package javelin.controller.walker.pathing;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.walker.Walker;
import javelin.view.mappanel.overlay.MoveOverlay;

/**
 * TODO This probably can be overloaded by {@link MoveOverlay} to take more
 * steps and try to find the best solution instead of the fastest one.
 */
public class BranchingPath implements Pathing{
	public static final BranchingPath INSTANCE=new BranchingPath();

	private BranchingPath(){
		// prevent instantiation
	}

	@Override
	public ArrayList<Point> step(Point from,Walker w){
		final int stepx=from.x+(w.to.x>from.x?+1:-1);
		final int stepy=from.y+(w.to.y>from.y?+1:-1);
		ArrayList<Point> steps=new ArrayList<>(3);
		if(from.y!=w.to.y) steps.add(new Point(from.x,stepy));
		if(from.x!=w.to.x){
			steps.add(new Point(stepx,from.y));
			if(from.y!=w.to.y) steps.add(new Point(stepx,stepy));
		}
		return steps;
	}
}