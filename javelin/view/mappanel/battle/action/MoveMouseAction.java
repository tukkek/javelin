package javelin.view.mappanel.battle.action;

import javelin.Javelin;
import javelin.Javelin.Delay;
import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.BattleWalker;
import javelin.view.mappanel.battle.overlay.BattleWalker.BattleStep;
import javelin.view.mappanel.overlay.DrawMoveOverlay;
import javelin.view.mappanel.overlay.MoveOverlay;
import javelin.view.screen.BattleScreen;

public class MoveMouseAction extends BattleMouseAction{
	public MoveMouseAction(){
		clearoverlay=false;
	}

	@Override
	public boolean validate(Combatant current,Combatant target,BattleState s){
		return target==null;
	}

	@Override
	public Runnable act(final Combatant current,Combatant target,BattleState s){
		final MoveOverlay walk=MapPanel.overlay instanceof MoveOverlay
				?(MoveOverlay)MapPanel.overlay
				:null;
		if(walk==null||walk.steps.isEmpty()) return null;
		walk.clear();
		return ()->{
			int finalstep=walk.steps.size()-1;
			final BattleStep to=(BattleStep)walk.steps.get(finalstep);
			BattleState move=Fight.state;
			Combatant c=move.clone(current);
			c.location[0]=to.x;
			c.location[1]=to.y;
			c.ap+=to.totalcost-BattleScreen.partialmove;
			MeldCrystal m=move.getmeld(to.x,to.y);
			if(m!=null&&c.ap>=m.meldsat) Fight.current.meld(c,m);
			if(to.engaged) Javelin.message(c+" disengages...",Delay.WAIT);
		};
	}

	@Override
	public void onenter(Combatant current,Combatant target,Tile t,BattleState s){
		Point from=current.getlocation();
		Point to=new Point(t.x,t.y);
		DrawMoveOverlay.draw(new MoveOverlay(new BattleWalker(from,to,current,s)));
	}
}