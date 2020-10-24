package javelin.controller.terrain;

import java.util.List;
import java.util.Set;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.hill.Rugged;
import javelin.controller.map.terrain.mountain.Forbidding;
import javelin.controller.map.terrain.mountain.Meadow;
import javelin.controller.map.terrain.mountain.MountainCave;
import javelin.controller.map.terrain.mountain.MountainPass;
import javelin.controller.map.terrain.mountain.MountainPath;
import javelin.controller.map.terrain.mountain.MountainsOfMadness;
import javelin.controller.terrain.hazard.Break;
import javelin.controller.terrain.hazard.Cold;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.terrain.hazard.Rockslide;
import javelin.model.world.Season;
import javelin.model.world.World;

/**
 * High altitude, snowy on winter.
 *
 * @author alex
 */
public class Mountains extends Terrain{
	/** Constructor. */
	public Mountains(){
		name="mountains";
		difficultycap=-2;
		speedtrackless=1/2f;
		speedroad=3/4f;
		speedhighway=3/4f;
		visionbonus=+4;
		representation='M';
		survivalbonus=-2;
	}

	@Override
	public Maps getmaps(){
		Maps m=new Maps();
		m.addAll(List.of(new Meadow(),new Rugged(),new Forbidding(),
				new MountainPass(),new MountainCave(),new MountainPath()));
		if(Weather.current==Weather.STORM||Season.current==Season.WINTER)
			m.add(new MountainsOfMadness());
		return m;
	}

	@Override
	protected Point generatesource(World w){
		Point source=super.generatesource(w);
		while(!w.map[source.x][source.y].equals(Terrain.FOREST)
				&&search(source,MOUNTAINS,1,w)==0)
			source=super.generatesource(w);
		return source;
	}

	@Override
	public Set<Hazard> gethazards(boolean special){
		Set<Hazard> hazards=super.gethazards(special);
		hazards.add(new Cold());
		if(special){
			hazards.add(new Rockslide());
			hazards.add(new Break());
		}
		return hazards;
	}
}