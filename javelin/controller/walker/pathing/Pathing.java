package javelin.controller.walker.pathing;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.walker.Walker;

public interface Pathing {
	List<Point> step(Point from, Walker w);
}