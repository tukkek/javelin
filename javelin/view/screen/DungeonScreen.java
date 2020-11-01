package javelin.view.screen;

import java.awt.Image;
import java.util.HashSet;
import java.util.List;

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
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.Wilderness;
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
		dungeon.squadlocation.x=x;
		dungeon.squadlocation.y=y;
	}

	@Override
	public void view(int xp,int yp){
		var vision=dungeon.squadvision;
		for(int x=-vision;x<=+vision;x++)
			for(int y=-vision;y<=+vision;y++)
				try{
					Point hero=dungeon.squadlocation;
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
		var i=dungeon.images;
		var file=dungeon.map[x][y]==Template.WALL?i.get(DungeonImages.WALL)
				:i.get(DungeonImages.FLOOR);
		var folder=dungeon instanceof Wilderness?"":"dungeon";
		return Images.get(List.of(folder,file));
	}

	@Override
	public Fight encounter(){
		return dungeon.fight();
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
	public Point getsquadlocation(){
		return dungeon.squadlocation;
	}

	@Override
	protected HashSet<Point> getdiscovered(){
		return dungeon.discovered;
	}
}
