package javelin.controller.walker.state;

import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.action.Charge;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;

/**
 * @see Charge
 *
 * @author alex
 */
public class ChargePath extends ClearPath{
	final boolean swimmer;

	public ChargePath(Point me,Point target,BattleState state,boolean swimmerp){
		super(me,target,state);
		swimmer=swimmerp;
		includetarget=true;
	}

	@Override
	public boolean validate(Point p,LinkedList<Point> previous){
		if(p.equals(to)) return true;
		for(Meld m:state.meld)
			if(m.x==p.x&&m.y==p.y) return false;
		return (swimmer||!state.map[p.x][p.y].flooded)&&super.validate(p,previous);
	}
}