package javelin.controller.walker.pathing;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;

public class DirectPath implements Pathing {
	float stepx;
	float stepy;
	float partialx;
	float partialy;

	public DirectPath(Point from, Point to) {
		float distancex = to.x - from.x;
		float distancey = to.y - from.y;
		float steps = Math.max(Math.abs(distancey), Math.abs(distancex));
		stepx = distancex / steps;
		stepy = distancey / steps;
		partialx = from.x;
		partialy = from.y;
	}

	@Override
	public ArrayList<Step> step(int x, int y, ArrayList<Step> steps, Walker w) {
		partialx += stepx;
		partialy += stepy;
		steps.add(new Step(Math.round(partialx), Math.round(partialy)));
		return steps;
	}
}
