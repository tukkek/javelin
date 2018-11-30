package javelin.view.screen;

import java.awt.Image;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.walker.Walker;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.dungeon.DungeonPanel;

/**
 * Shows the inside of a {@link Dungeon}.
 *
 * @author alex
 */
public class DungeonScreen extends WorldScreen{
	public static final int VIEWRADIUS=4;

	Dungeon dungeon;

	public DungeonScreen(Dungeon dungeon){
		super(false);
		this.dungeon=dungeon;
		open();
	}

	/**
	 * If <code>false</code> skip updating the location this time. TODO is hack?
	 */
	public static boolean updatelocation=true;

	@Override
	public boolean explore(int x,int y){
		try{
			RandomEncounter.encounter(1f/dungeon.stepsperencounter);
		}catch(StartBattle e){
			throw e;
		}
		return !dungeon.hazard();
	}

	@Override
	public boolean react(int x,int y){
		Combatant searching=Squad.active.getbest(Skill.PERCEPTION);
		for(Feature f:dungeon.features.copy())
			if(Walker.distanceinsteps(x,y,f.x,f.y)==1)
				f.discover(searching,searching.roll(Skill.PERCEPTION));
		Feature f=dungeon.features.get(x,y);
		if(f==null) return false;
		boolean activated=f.activate();
		if(activated&&f.remove) dungeon.features.remove(f);
		if(!f.enter) WorldMove.abort=true;
		if(!activated) return false;
		if(!WorldMove.abort) WorldMove.place(x,y);
		return true;
	}

	@Override
	public boolean allowmove(int x,int y){
		return dungeon.map[x][y]!=Template.WALL;
	}

	@Override
	public void updatelocation(int x,int y){
		dungeon.herolocation.x=x;
		dungeon.herolocation.y=y;
	}

	@Override
	public void view(int xp,int yp){
		for(int x=-VIEWRADIUS;x<=+VIEWRADIUS;x++)
			for(int y=-VIEWRADIUS;y<=+VIEWRADIUS;y++)
				try{
					Point hero=dungeon.herolocation;
					Point target=new Point(hero);
					target.x+=x;
					target.y+=y;
					if(checkclear(hero,target)) dungeon.setvisible(hero.x+x,hero.y+y);
				}catch(ArrayIndexOutOfBoundsException e){
					continue;
				}
	}

	boolean checkclear(Point hero,Point target){
		Point step=new Point(hero);
		while(step.x!=target.x||step.y!=target.y){
			if(step.x!=target.x) step.x+=step.x>target.x?-1:+1;
			if(step.y!=target.y) step.y+=step.y>target.y?-1:+1;
			if(!step.equals(target)&&(dungeon.map[step.x][step.y]==Template.WALL
					||dungeon.features.get(step.x,step.y) instanceof Door))
				return false;
		}
		return true;
	}

	@Override
	public Image gettile(int x,int y){
		return Images
				.get(dungeon.map[x][y]==Template.WALL?dungeon.wall:dungeon.floor);
	}

	@Override
	public Fight encounter(){
		return dungeon.encounter();
	}

	@Override
	protected MapPanel getmappanel(){
		return new DungeonPanel(dungeon);
	}

	@Override
	public boolean validatepoint(int x,int y){
		return 0<=x&&x<dungeon.size&&0<=y&&y<dungeon.size;
	}

	@Override
	public Point getherolocation(){
		return dungeon.herolocation;
	}

	@Override
	protected HashSet<Point> getdiscovered(){
		return dungeon.discovered;
	}
}
