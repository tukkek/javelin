package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.util.LinkedList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.Movement;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.BattleTile;
import javelin.view.screen.BattleScreen;

public class BattleWalker extends OverlayWalker{
	/**
	 * Note that AP cost has a different meaning depending on context. For battle
	 * is literal AP, for world and dungeons is chance of encounter, unrelated to
	 * time.
	 *
	 * @author alex
	 */
	public class BattleStep extends OverlayStep{
		public final float apcost;
		public boolean engaged;
		public boolean safe=false;
		public float totalcost;

		public BattleStep(Point p,final float totalcost){
			super(p);
			engaged=BattleWalker.this.engaged||checkengaged(p);
			apcost=getcost();
			this.totalcost=totalcost+apcost;
			color=engaged||this.totalcost>=.5?Color.RED:Color.GREEN;
			text=Float.toString(this.totalcost).substring(0,3);
		}

		protected float getcost(){
			final float apcost;
			if(engaged)
				apcost=Movement.disengage(current);
			else if(current.burrowed)
				apcost=Movement.converttoap(current.source.burrow);
			else if(state.map[x][y].flooded){
				int swim=current.source.swim();
				apcost=Movement.converttoap(swim>0?swim:current.source.walk/2);
			}else
				apcost=Movement.converttoap(current.gettopspeed(state));
			return apcost;
		}
	}

	Combatant current;
	BattleState state;
	private boolean engaged;

	public BattleWalker(Point from,Point to,Combatant current,BattleState state){
		super(from,to);
		this.current=current;
		this.state=state;
		engaged=checkengaged(from);
	}

	@Override
	protected Point step(Point step,LinkedList<Point> previous){
		float totalcost=previous.isEmpty()?BattleScreen.partialmove
				:((BattleStep)previous.getLast()).totalcost;
		return new BattleStep(step,totalcost);
	}

	@Override
	public boolean validate(Point p,LinkedList<Point> previous){
		if(!previous.isEmpty()&&((BattleStep)previous.getLast()).engaged)
			return false;
		BattleStep step=(BattleStep)p;
		if(state.map[step.x][step.y].blocked
				&&(current.source.fly==0||!Javelin.app.fight.map.flying))
			return false;
		boolean istarget=step.equals(to);
		if(engaged&&(!previous.isEmpty()||!istarget)) return false;
		if(step.totalcost>=1||state.getmeld(step.x,step.y)!=null) return false;
		if(state.getcombatant(step.x,step.y)!=null) return false;
		Meld m=state.getmeld(step.x,step.y);
		if(m!=null) return istarget&&current.ap>=m.meldsat;
		try{
			BattleTile t=(BattleTile)BattleScreen.active.mappanel.tiles[p.x][p.y];
			return ((BattlePanel)BattleScreen.active.mappanel).daylight
					||t.discovered&&!t.shrouded;
		}catch(NullPointerException e){
			return false;
		}catch(ClassCastException e){
			return false;
		}
	}

	boolean checkengaged(Point p){
		if(current.burrowed) return false;
		final int maxx=Math.min(p.x+1,state.map.length);
		final int maxy=Math.min(p.y+1,state.map[0].length);
		for(int x=Math.max(p.x-1,0);x<=maxx;x++)
			for(int y=Math.max(p.y-1,0);y<=maxy;y++){
				final Combatant c=state.getcombatant(x,y);
				if(c!=null&&!c.isally(current,state)) return true;
			}
		return false;
	}

	@Override
	public Point resetlocation(){ // TODO
		return null;
	}
}