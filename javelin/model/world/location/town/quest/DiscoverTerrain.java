package javelin.model.world.location.town.quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * {@link Trait#NATURAL} quest: discover a new type of {@link Terrain}.
 *
 * @author alex
 */
public class DiscoverTerrain extends Quest{
	Terrain terrain=null;

	/** Reflection constructor. */
	public DiscoverTerrain(Town t){
		super(t);
		var undiscovered=new HashSet<>(Arrays.asList(Terrain.NONUNDERGROUND));
		for(var x=0;x<World.scenario.size;x++)
			for(var y=0;y<World.scenario.size;y++)
				if(WorldScreen.see(new Point(x,y)))
					undiscovered.remove(Terrain.get(x,y));
		if(!undiscovered.isEmpty()) terrain=RPG.pick(new ArrayList<>(undiscovered));
	}

	@Override
	public boolean validate(){
		return terrain!=null;
	}

	@Override
	protected String getname(){
		return "Discover terrain: "+terrain.toString().toLowerCase();
	}

	@Override
	public boolean complete(){
		for(var x=0;x<World.scenario.size;x++)
			for(var y=0;y<World.scenario.size;y++)
				if(WorldScreen.see(new Point(x,y))&&Terrain.get(x,y).equals(terrain))
					return true;
		return false;
	}
}
