package javelin.view.screen;

import java.awt.Image;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.fight.Fight;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.walker.Walker;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.DungeonFloor;
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

	/** Constructor. */
	public DungeonScreen(DungeonFloor f){
		super(false);
		floor=f;
		open();
	}

	@Override
	public boolean explore(int x,int y){
		return floor.explore();
	}

	@Override
	public boolean react(int x,int y){
		var searching=Squad.active.getbest(Skill.PERCEPTION);
		for(var f:floor.features.copy())
			if(Walker.distanceinsteps(x,y,f.x,f.y)==1)
				f.discover(searching,searching.roll(Skill.PERCEPTION));
		var f=floor.features.get(x,y);
		if(f==null) return false;
		var activated=f.activate();
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
		var v=floor.dungeon.vision;
		var s=floor.squadlocation;
		for(int x=s.x-v;x<=s.x+v;x++)
			for(int y=s.y-v;y<=s.y+v;y++)
				if(validatepoint(x,y)&&checkclear(s,new Point(x,y)))
					floor.setvisible(x,y);
	}

	boolean checkclear(Point hero,Point target){
		var p=new Point(hero);
		while(!p.equals(target)){
			if(p.x!=target.x) p.x+=p.x>target.x?-1:+1;
			if(p.y!=target.y) p.y+=p.y>target.y?-1:+1;
			if(p.equals(target)) break;
			if(floor.map[p.x][p.y]==MapTemplate.WALL
					||floor.features.get(p.x,p.y) instanceof Door)
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
