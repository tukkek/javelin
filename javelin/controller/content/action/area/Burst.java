package javelin.controller.content.action.area;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.ThreadManager;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class Burst extends Area{
	final ArrayList<Point> directions=new ArrayList<>();

	public Burst(final int sourcex,final int sourcey,final int ax,final int ay,
			final int bx,final int by,final int cx,final int cy){
		super(sourcex,sourcey);
		directions.add(new Point(ax,ay));
		directions.add(new Point(bx,by));
		directions.add(new Point(cx,cy));
	}

	@Override
	public Set<Point> fill(int range,final Combatant c,final BattleState state){
		range-=5;
		final HashSet<Point> area=new HashSet<>();
		final Point p=initiate(c);
		if(checkclear(state,p)) recursivefill(p,p,range,area,state);
		return area;
	}

	private void recursivefill(final Point source,final Point current,
			final int range,final HashSet<Point> area,final BattleState s){
		if(ThreadManager.working) AiThread.checkinterrupted();
		if(Walker.distance(source.x,source.y,current.x,current.y)>range/5
				||!checkclear(s,current))
			return;
		area.add(current);
		for(final Point direction:directions)
			recursivefill(source,
					new Point(current.x+direction.x,current.y+direction.y),range,area,s);
	}
}