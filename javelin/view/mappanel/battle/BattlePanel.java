package javelin.view.mappanel.battle;

import java.awt.Graphics;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.controller.fight.Fight;
import javelin.controller.fight.mutator.Meld;
import javelin.model.state.BattleState;
import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;
import javelin.model.world.Period;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

/**
 * TODO remove {@link BattleMap} and rename this hierarchy
 *
 * @author alex
 */
public class BattlePanel extends MapPanel{
	/** Active unit. */
	static public Combatant current=null;

	/** If <code>true</code>, all {@link BattleTile}s are visible. */
	public boolean daylight;

	BattleState previousstate=null;
	BattleState state=null;

	/** Constructor. */
	public BattlePanel(BattleState s){
		super(s.map.length,s.map[0].length,Preferences.KEYTILEBATTLE);
		var p=Javelin.app.fight.period;
		daylight=p.equals(Period.MORNING)||p.equals(Period.AFTERNOON);
	}

	@Override
	protected Mouse getmouselistener(){
		return new BattleMouse(this);
	}

	@Override
	protected Tile newtile(int x,int y){
		return new BattleTile(x,y,daylight);
	}

	@Override
	public synchronized void refresh(){
		try{
			updatestate();
			var s=Fight.state;
			if(s==null) return;
			var update=new HashSet<Point>(s.redTeam.size()+s.blueTeam.size());
			for(var c:s.getcombatants())
				update.add(new Point(c.location[0],c.location[1]));
			for(var c:previousstate.getcombatants())
				update.add(new Point(c.location[0],c.location[1]));
			updatestate();
			for(var c:s.getcombatants())
				update.add(new Point(c.location[0],c.location[1]));
			if(!daylight) calculatevision(update);
			if(overlay!=null) update.addAll(overlay.affected);
			if(Javelin.app.fight.has(Meld.class)!=null) for(MeldCrystal m:s.meld)
				update.add(new Point(m.x,m.y));
			for(var p:update)
				tiles[p.x][p.y].repaint();
		}catch(ConcurrentModificationException e){
			/*
			 * I have no idea why this is being thrown since the HashSet is local and the
			 * method is synchronized on top of it.
			 */
			refresh();
		}
	}

	@Override
	public void paint(Graphics g){
		updatestate();
		if(!daylight) calculatevision(null);
		super.paint(g);
	}

	private void calculatevision(final HashSet<Point> update){
		Combatant active=Fight.state.clone().clone(current);
		if(active==null) return;
		final HashSet<Point> seen=active.calculatevision(Fight.state);
		for(Point p:seen){ // seen
			BattleTile t=(BattleTile)tiles[p.x][p.y];
			if(update!=null&&(!t.discovered||t.shrouded)) update.add(p);
			t.discovered=true;
			t.shrouded=false;
		}
		for(int x=0;x<tiles.length;x++)
			for(int y=0;y<tiles[0].length;y++){
				final BattleTile t=(BattleTile)tiles[x][y];
				if(!t.discovered||t.shrouded) continue;
				final Point p=new Point(x,y);
				if(!seen.contains(p)){
					t.shrouded=true;
					if(update!=null) update.add(p);
				}
			}
	}

	void updatestate(){
		previousstate=state;
		if(Fight.state==null) return;
		state=Fight.state.clonedeeply();
		if(previousstate==null) previousstate=state;
		BattleTile.panel=this;
		daylight=state.period.equals(Period.MORNING)
				||state.period.equals(Period.AFTERNOON);
		if(state.period!=previousstate.period){
			for(Tile[] tiles:tiles)
				for(Tile t:tiles){
					((BattleTile)t).shrouded=!daylight;
					t.repaint();
				}
			if(!daylight) calculatevision(null);
		}
	}

	@Override
	protected int gettilesize(){
		return Preferences.tilesizebattle;
	}
}
