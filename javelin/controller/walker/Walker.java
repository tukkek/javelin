package javelin.controller.walker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.walker.pathing.BranchingPath;
import javelin.controller.walker.pathing.Pathing;
import javelin.model.unit.Combatant;

/**
 * Extensible path-finding algorithm.
 *
 * TODO improve documentation for all hierarchy.
 *
 * @author alex
 */
public class Walker{
	public static final int[] DELTAS=new int[]{0,+1,-1};

	class OptimalSteps extends ArrayList<Point>{
		int distance=Integer.MAX_VALUE;

		public OptimalSteps(List<Point> steps){
			super(steps);
		}

		@Override
		public boolean add(Point step){
			if(isEmpty()) return super.add(step);
			final int d=to.distanceinsteps(step);
			if(d>distance) return false;
			if(d<distance) clear();
			return super.add(step);
		}
	}

	public Pathing pathing=BranchingPath.INSTANCE;
	public LinkedList<Point> solution=null;
	public LinkedList<Point> partial=null;
	public Point from;
	public Point to;

	protected boolean optimize=true;
	protected boolean includetarget=false;
	protected boolean acceptpartial=false;

	public Walker(Point from,Point to){
		this.from=from;
		this.to=to;
	}

	public LinkedList<Point> walk(){
		walk(from,new LinkedList<Point>());
		if(solution!=null) return solution;
		return acceptpartial?partial:null;
	}

	/**
	 * @param x
	 * @param y
	 * @param walk
	 */
	private void walk(Point step,LinkedList<Point> walk){
		boolean istarget=step.equals(to);
		if(!step.equals(from)&&(!istarget||includetarget)){
			step=step(step,walk);
			if(validate(step,walk))
				walk.add(step);
			else{
				partial=walk;
				return;
			}
		}
		if(solution!=null&&walk.size()>=solution.size()) return;
		if(istarget){
			solution=walk;
			return;
		}
		List<Point> next=pathing.step(step,this);
		if(optimize) next=new OptimalSteps(next);
		for(Point n:next)
			walk(n,(LinkedList<Point>)walk.clone());
	}

	protected Point step(Point step,LinkedList<Point> previous){
		return step;
	}

	public boolean validate(Point p,LinkedList<Point> previous){
		return true;
	}

	public void reset(){
		solution=null;
		partial=null;
	}

	public static int distanceinsteps(int ax,int ay,int bx,int by){
		return Math.max(Math.abs(ax-bx),Math.abs(ay-by));
	}

	public static double distance(final Combatant c1,final Combatant c2){
		return distance(c1.location[0],c1.location[1],c2.location[0],
				c2.location[1]);
	}

	public static double distance(final int ax,final int ay,final int bx,
			final int by){
		final int deltax=Math.abs(ax-bx);
		final int deltay=Math.abs(ay-by);
		return Math.sqrt(deltax*deltax+deltay*deltay);
	}
}
