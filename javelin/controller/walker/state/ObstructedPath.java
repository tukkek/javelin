package javelin.controller.walker.state;

import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.state.BattleState;

/**
 * Finds any path, clear or obstructed but not blocked.
 * 
 * @author alex
 */
public class ObstructedPath extends StateWalker {
    public ObstructedPath(Point from, Point to, BattleState s) {
	super(from, to, s);
	pathing = new DirectPath();
    }

    @Override
    public boolean validate(Point p, LinkedList<Point> previous) {
	try {
	    return !state.map[p.x][p.y].blocked;
	} catch (ArrayIndexOutOfBoundsException e) {
	    return false;
	}
    }
}