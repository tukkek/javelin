package javelin.controller.content.terrain;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.content.map.Maps;
import javelin.controller.content.map.terrain.forest.DenseForest;
import javelin.controller.content.map.terrain.forest.ForestPath;
import javelin.controller.content.map.terrain.forest.MediumForest;
import javelin.controller.content.map.terrain.forest.SparseForest;
import javelin.controller.content.terrain.hazard.Break;
import javelin.controller.content.terrain.hazard.FallingTrees;
import javelin.controller.content.terrain.hazard.GettingLost;
import javelin.controller.content.terrain.hazard.Hazard;
import javelin.model.world.World;

/**
 * Dense forest but not quite a jungle.
 *
 * @author alex
 */
public class Forest extends Terrain{
	/** Constructor. */
	public Forest(){
		name="forest";
		difficultycap=-3;
		speedtrackless=1/2f;
		speedroad=1f;
		speedhighway=1f;
		visionbonus=-4;
		representation='F';
		survivalbonus=+4;
	}

	@Override
	public Maps getmaps(){
		Maps m=new Maps();
		m.add(new SparseForest());
		m.add(new MediumForest());
		m.add(new DenseForest());
		m.add(new ForestPath());
		return m;
	}

	@Override
	HashSet<Point> generatearea(World world){
		return gettiles(world);
	}

	@Override
	public Set<Hazard> gethazards(boolean special){
		Set<Hazard> hazards=super.gethazards(special);
		hazards.add(new GettingLost(16));
		if(special){
			hazards.add(new FallingTrees());
			hazards.add(new Break());
		}
		return hazards;
	}
}