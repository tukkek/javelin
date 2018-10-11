package javelin.controller.walker.state;

import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.walker.pathing.DirectPath;
import javelin.model.state.BattleState;
import javelin.model.state.Square;

/**
 * Finds an unobstructed path.
 *
 * @author alex
 */
public class ClearPath extends StateWalker{
	public ClearPath(Point from,Point to,BattleState s){
		super(from,to,s);
		pathing=new DirectPath();
	}

	@Override
	public boolean validate(Point p,LinkedList<Point> previous){
		if(!p.validate(0,0,state.map.length,state.map[0].length)) return false;
		final Square s=state.map[p.x][p.y];
		return !s.blocked&&!s.obstructed&&state.getcombatant(p.x,p.y)==null;
	}
}