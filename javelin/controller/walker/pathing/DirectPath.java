package javelin.controller.walker.pathing;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;

public class DirectPath implements Pathing {
	Float stepx = null;
	Float stepy = null;
	Float partialx = null;
	Float partialy = null;
	boolean first = true;

	@Override
	public ArrayList<Step> step(int x, int y, ArrayList<Step> steps, Walker w) {
		if (first) {
			first = false;
			Point first = takefirststep(w);
			if (first != null) {
				steps.add(new Step(first.x, first.y));
				calculatepath(first, w);
			}
			return steps;
		}
		partialx += stepx;
		partialy += stepy;
		steps.add(new Step(Math.round(partialx), Math.round(partialy)));
		return steps;
	}

	void calculatepath(Point first, Walker w) {
		float distancex = w.tox - first.x;
		float distancey = w.toy - first.y;
		float distance = Math.max(Math.abs(distancey), Math.abs(distancex));
		stepx = distancex / distance;
		stepy = distancey / distance;
		partialx = new Float(first.x);
		partialy = new Float(first.y);
	}

	Point takefirststep(Walker w) {
		Point to = new Point(w.tox, w.toy);
		Point closest = null;
		for (Point p : Point.getadjacent()) {
			p.x += w.fromx;
			p.y += w.fromy;
			if (!w.valid(p.x, p.y)) {
				continue;
			}
			if (closest == null || p.distance(to) < closest.distance(to)) {
				closest = p;
			}
		}
		return closest;
	}
}
