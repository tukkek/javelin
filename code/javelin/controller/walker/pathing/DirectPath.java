package javelin.controller.walker.pathing;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.walker.Walker;

public class DirectPath implements Pathing{
	Float stepx=null;
	Float stepy=null;
	Float partialx=null;
	Float partialy=null;
	boolean first=true;

	@Override
	public ArrayList<Point> step(Point from,Walker w){
		if(stepx==null) calculatepath(from,w);
		partialx+=stepx;
		partialy+=stepy;
		ArrayList<Point> step=new ArrayList<>(1);
		step.add(new Point(Math.round(partialx),Math.round(partialy)));
		return step;
	}

	void calculatepath(Point first,Walker w){
		float distancex=w.to.x-first.x;
		float distancey=w.to.y-first.y;
		float distance=Math.max(Math.abs(distancey),Math.abs(distancex));
		stepx=distancex/distance;
		stepy=distancey/distance;
		partialx=(float)first.x;
		partialy=(float)first.y;
	}

	Point takefirststep(Walker w){
		Point closest=null;
		for(Point p:Point.getadjacent2()){
			p.x+=w.from.x;
			p.y+=w.from.y;
			if(w.validate(p,null)
					&&(closest==null||p.distance(w.to)<closest.distance(w.to)))
				closest=p;
		}
		return closest;
	}
}
