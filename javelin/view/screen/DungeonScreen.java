package javelin.view.screen;

import java.awt.Image;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.fight.Fight;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.dungeon.DungeonPanel;

/**
 * Shows the inside of a {@link DungeonFloor}.
 *
 * @author alex
 */
public class DungeonScreen extends WorldScreen{
	DungeonFloor floor;

	public DungeonScreen(DungeonFloor dungeon){
		super(false);
		floor=dungeon;
		open();
	}

	/**
	 * If <code>false</code> skip updating the location this time. TODO is hack?
	 */
	public static boolean updatelocation=true;

	@Override
	public boolean explore(int x,int y){
		return floor.explore();
	}

	@Override
	public boolean react(int x,int y){
		Combatant searching=Squad.active.getbest(Skill.PERCEPTION);
		for(Feature f:floor.features.copy())
			if(Walker.distanceinsteps(x,y,f.x,f.y)==1)
				f.discover(searching,searching.roll(Skill.PERCEPTION));
		Feature f=floor.features.get(x,y);
		if(f==null) return false;
		boolean activated=f.activate();
		if(activated&&f.remove) floor.features.remove(f);
		if(!f.enter) WorldMove.abort=true;
		if(!activated) return false;
		if(!WorldMove.abort) WorldMove.place(x,y);
		return true;
	}

	@Override
	public boolean allowmove(int x,int y){
		return floor.map[x][y]!=MapTemplate.WALL;
	}

	@Override
	public void updatelocation(int x,int y){
		floor.squadlocation.x=x;
		floor.squadlocation.y=y;
	}

	@Override
	public void view(int xp,int yp){
		var vision=floor.dungeon.vision;
		for(int x=-vision;x<=+vision;x++)
			for(int y=-vision;y<=+vision;y++)
				try{
					Point hero=floor.squadlocation;
					Point target=new Point(hero);
					target.x+=x;
					target.y+=y;
					if(checkclear(hero,target)) floor.setvisible(hero.x+x,hero.y+y);
				}catch(ArrayIndexOutOfBoundsException e){
					continue;
				}
	}

	boolean checkclear(Point hero,Point target){
		Point step=new Point(hero);
		while(step.x!=target.x||step.y!=target.y){
			if(step.x!=target.x) step.x+=step.x>target.x?-1:+1;
			if(step.y!=target.y) step.y+=step.y>target.y?-1:+1;
			if(!step.equals(target)&&(floor.map[step.x][step.y]==MapTemplate.WALL
					||floor.features.get(step.x,step.y) instanceof Door))
				return false;
		}
		return true;
	}

	@Override
	public Image gettile(int x,int y){
		//handled by DungeonTile
		throw new UnsupportedOperationException();
	}

	@Override
	public Fight encounter(){
		return floor.dungeon.fight();
	}

	@Override
	protected MapPanel getmappanel(){
		return new DungeonPanel(floor);
	}

	@Override
	public boolean validatepoint(int x,int y){
		return 0<=x&&x<floor.size&&0<=y&&y<floor.size;
	}

	@Override
	public Point getsquadlocation(){
		return floor.squadlocation;
	}

	@Override
	protected HashSet<Point> getdiscovered(){
		return floor.discovered;
	}
}
