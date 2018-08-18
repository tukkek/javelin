package javelin.controller.terrain;

import java.util.List;

import javelin.controller.map.Map;
import javelin.controller.map.Maps;
import javelin.controller.map.terrain.underground.Constructed;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.underground.BigCave;
import javelin.old.underground.Caves;
import javelin.old.underground.Complex;
import javelin.old.underground.Floor;
import javelin.old.underground.Maze;
import javelin.old.underground.Pit;
import javelin.view.Images;

/**
 * See {@link Terrain#UNDERGROUND}.
 *
 * @author alex
 */
public class Underground extends Terrain{
	/** Constructor. */
	public Underground(){
		name="underground";
		difficulty=0;
		difficultycap=Integer.MAX_VALUE;
	}

	@Override
	public Maps getmaps(){
		Maps maps=new Maps();
		for(Map m:List.of(new Caves(),new BigCave(),new Maze(),new Pit(),
				new Floor(),new Complex(),new Constructed())){
			Dungeon d=Dungeon.active;
			if(d!=null){
				m.floor=Images.getImage(d.floor);
				m.wall=Images.getImage(d.wall);
			}
			maps.add(m);
		}
		return maps;
	}
}
