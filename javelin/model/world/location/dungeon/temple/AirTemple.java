package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.terrain.Mountains;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.artifact.Flute;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;

/**
 * Found high on the {@link Mountains}. The wind can push you up to three
 * squares around explored spaces.
 *
 * @see Temple
 * @author alex
 */
public class AirTemple extends Temple{
	private static final String FLUFF="You are at the very peak of this mountain range, way above the cloud cover.\n"
			+"The cold wind makes your limbs tremble and your heart lust for warmer surroundings.\n"
			+"At last you manage to overcome the stone entryway that dives deep into the summit's core.\n"
			+"As you enter you immediately feel warmer but the strong gale coming from the outside threatens to carry you along its momentum.";

	/** Constructor. */
	public AirTemple(Integer level){
		super(Realm.AIR,Terrain.MOUNTAINS,level,new Flute(level),FLUFF);
		images.put(DungeonImages.FLOOR,"floordungeon");
		images.put(DungeonImages.WALL,"walldungeon");
	}

	@Override
	public boolean hazard(DungeonFloor d){
		if(!RPG.chancein(d.stepsperencounter)) return false;
		ArrayList<Point> steps=new ArrayList<>();
		steps.add(JavelinApp.context.getsquadlocation());
		int nsteps=RPG.r(3,7);
		for(int i=0;i<nsteps;i++){
			Point p=push(steps,d);
			if(p==null) return false;
			steps.add(p);
		}
		Javelin.message("A strong wind pushes you around!",true);
		Point to=steps.get(steps.size()-1);
		d.teleport(to);
		return true;
	}

	private Point push(ArrayList<Point> steps,DungeonFloor d){
		Point current=steps.get(steps.size()-1);
		ArrayList<Point> possibilities=new ArrayList<>();
		for(int x=current.x-1;x<=current.x+1;x++)
			for(int y=current.y-1;y<=current.y+1;y++){
				try{
					if(!d.visible[x][y]) continue;
				}catch(IndexOutOfBoundsException e){
					continue;
				}
				for(Feature f:d.features)
					if(f.x==x&&f.y==y) continue;
				Point step=new Point(x,y);
				if(!steps.contains(step)&&d.map[step.x][step.y]!=MapTemplate.WALL)
					possibilities.add(step);
			}
		return possibilities.isEmpty()?null:RPG.pick(possibilities);
	}
}
