package javelin.model.world.location.dungeon.branch.temple;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.terrain.Mountains;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.branch.DungeonHazard;
import javelin.old.RPG;

/**
 * Found on the {@link Mountains}. The wind {@link Hazard} can push you around.
 *
 * @author alex
 */
public class AirTemple extends Temple{
	/** Branch singleton. */
	public static final Branch BRANCH=new AirBranch();

	static final String FLUFF="You are at the very peak of this mountain range, way above the cloud cover.\n"
			+"The cold wind makes your limbs tremble and your heart lust for warmer surroundings.\n"
			+"At last you manage to overcome the stone entryway that dives deep into the summit's core.\n"
			+"As you enter you immediately feel warmer but the strong gale coming from the outside threatens to carry you along its momentum.";

	static class Wind extends DungeonHazard{
		@Override
		public boolean trigger(){
			var steps=new ArrayList<Point>();
			steps.add(JavelinApp.context.getsquadlocation());
			int nsteps=RPG.r(3,7);
			for(int i=0;i<nsteps;i++){
				var p=push(steps);
				if(p==null) return false;
				steps.add(p);
			}
			Javelin.message("A strong wind pushes you around!",true);
			var to=steps.get(steps.size()-1);
			Dungeon.active.teleport(to);
			return true;
		}

		Point push(ArrayList<Point> steps){
			var current=steps.get(steps.size()-1);
			var possibilities=new ArrayList<Point>();
			var d=Dungeon.active;
			for(int x=current.x-1;x<=current.x+1;x++)
				for(int y=current.y-1;y<=current.y+1;y++){
					try{
						if(!d.visible[x][y]) continue;
					}catch(IndexOutOfBoundsException e){
						continue;
					}
					if(d.features.get(x,y)!=null) continue;
					var step=new Point(x,y);
					if(!steps.contains(step)&&d.map[step.x][step.y]!=MapTemplate.WALL)
						possibilities.add(step);
				}
			return possibilities.isEmpty()?null:RPG.pick(possibilities);
		}

	}

	static class AirBranch extends Branch{
		protected AirBranch(){
			super("floortemplegood","walltempleair");
			terrains.add(Terrain.MOUNTAINS);
			hazards.add(new Wind());
		}
	}

	/** Constructor. */
	public AirTemple(){
		super(Realm.AIR,new AirBranch(),FLUFF);
	}
}
