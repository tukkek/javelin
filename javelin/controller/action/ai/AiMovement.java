package javelin.controller.action.ai;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.view.Images;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.mappanel.battle.overlay.BattleWalker;
import javelin.view.mappanel.battle.overlay.BattleWalker.BattleStep;

/**
 * Attempst to offer a more fluid experience than having {@link AiMovement}
 * simple do long step-by-step movement. Tries to move at most .5AP (move
 * action).
 *
 * @author alex
 */
public class AiMovement extends Action implements AiAction{
	/**
	 * Target value for number of outcome nodes.We want to limit the number of
	 * node outcomes so that the AI doesn't become to slow.
	 */
	static final int MOVES=4;
	/** Unique instace of this class. */
	public static final AiMovement SINGLETON=new AiMovement();
	/** Image overlay representing movement. */
	public static final Image MOVEOVERLAY=Images.get("overlaymove");

	class LongMove extends ChanceNode{
		float score;

		LongMove(Combatant c,BattleStep s,List<Point> steps,BattleWalker mover,
				Meld m,BattleState n){
			super(n,1,getmessage(c,m,s),
					m==null?Javelin.Delay.WAIT:Javelin.Delay.BLOCK);
			AiOverlay o=new AiOverlay(steps.subList(0,steps.indexOf(s)));
			o.affected.add(new Point(mover.from.x,mover.from.y));
			o.image=AiMovement.MOVEOVERLAY;
			overlay=o;
			score=BattleAi.measuredistances(n.getteam(c),n.getopponents(c));
		}
	}

	private AiMovement(){
		super("Long move");
		allowburrowed=true;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant c,BattleState s){
		if(c.gettopspeed(s)==0) return Collections.emptyList();
		ArrayList<List<ChanceNode>> outcomes=new ArrayList<>(MOVES);
		Point from=c.getlocation();
		for(Point interest:getpointsofinterest(c,s))
			add(move(c,from,interest,s),outcomes);
		if(outcomes.size()>=MOVES) return outcomes;
		ArrayList<List<ChanceNode>> extra=new ArrayList<>(MOVES);
		for(var destination:getdestinations(c,s))
			add(move(c,from,destination,s),extra);
		if(extra.isEmpty()) return outcomes;
		extra.sort((a,b)->Float.compare(((LongMove)a.get(0)).score,
				((LongMove)b.get(0)).score));
		Random r=AiThread.getrandom();
		while(outcomes.size()<MOVES&&!extra.isEmpty()){
			int max=extra.size()-1;
			int index;
			for(index=0;index<max&&r.nextBoolean();index++)
				continue;
			outcomes.add(extra.remove(index));
		}
		return outcomes;
	}

	static HashSet<Point> getpointsofinterest(Combatant c,BattleState s){
		HashSet<Point> interesting=new HashSet<>();
		for(Combatant enemy:s.getopponents(c))
			interesting.add(enemy.getlocation());
		for(Meld m:s.meld)
			interesting.add(new Point(m.x,m.y));
		return interesting;
	}

	static void add(ChanceNode n,ArrayList<List<ChanceNode>> outcomes){
		if(n!=null){
			ArrayList<ChanceNode> chances=new ArrayList<>();
			chances.add(n);
			outcomes.add(chances);
		}
	}

	LongMove move(Combatant c,Point from,Point to,BattleState s){
		BattleWalker mover=new BattleWalker(from,to,c,s);
		List<Point> steps=mover.walk();
		if(steps.isEmpty()) return null;
		BattleStep step=choosestep(steps);
		s=s.clone();
		c=s.clone(c);
		c.ap+=step.totalcost;
		c.location[0]=step.x;
		c.location[1]=step.y;
		Meld m=s.getmeld(step.x,step.y);
		if(m!=null){
			c.meld();
			s.meld.remove(m);
		}
		return new LongMove(c,step,steps,mover,m,s);
	}

	static BattleStep choosestep(List<Point> steps){
		for(Point p:steps){
			BattleStep step=(BattleStep)p;
			if(step.engaged||step.totalcost>=ActionCost.MOVE) return step;
		}
		return (BattleStep)steps.get(steps.size()-1);
	}

	/**
	 * TODO this can still be optimized, most likely
	 *
	 * 1. costly calculations (namerly haslineofsight), can be deferred, so that
	 * we don't have to calculate it dozens of times even if only {@link #MOVES}
	 * at most will be selected.
	 *
	 * 2. since this can produce a considerable number of results, might want
	 * instead to take the Random parameter and pick random points until we have
	 * {@link #MOVES}x2 or something like that.
	 */
	static HashSet<Point> getdestinations(Combatant c,BattleState s){
		var destinations=new HashSet<Point>();
		var f=Javelin.app.fight;
		var flies=f.map.flying&&c.source.fly>0;
		var range=s.isengaged(c)?1:c.gettopspeed(s)/5;
		Point from=c.getlocation();
		for(var x=from.x-range;x<=from.x+range;x++)
			for(var y=from.y-range;y<=from.y+range;y++){
				var point=new Point(x,y);
				if(!point.validate(0,0,s.map.length,s.map[0].length)) continue;
				if(s.map[point.x][point.y].blocked&&!flies) continue;
				if(s.getcombatant(point.x,point.y)!=null) continue;
				Meld m=s.getmeld(point.x,point.y);
				if(m!=null&&!m.crystalize(s)) continue;
				if(s.haslineofsight(c,point)==Vision.BLOCKED) continue;
				destinations.add(point);
			}
		return destinations;
	}

	@Override
	public boolean perform(Combatant active){
		throw new UnsupportedOperationException();
	}

	static String getmessage(Combatant c,Meld m,BattleStep s){
		if(m!=null) return c+" powers up!";
		if(s.engaged) return c+" disengages...";
		return c+" moves...";
	}
}
