package javelin.controller.content.event.wild.positive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.event.wild.WildEvent;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.World;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Shows undiscovered parts ot the current {@link Terrain} formation.
 *
 * @author alex
 */
public class RevealRegion extends WildEvent{
	HashSet<Point> undiscovered=new HashSet<>();
	String friend=null;

	/** Reflection-friendly constructor. */
	public RevealRegion(PointOfInterest l){
		super("Reveal region",l);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		var here=location.getlocation();
		var friendly=Terrain.get(here.x,here.y).getmonsters().stream()
				.filter(m->m.alignment.isgood()&&m.think(-1))
				.collect(Collectors.toList());
		if(friendly.isEmpty()) return false;
		friend=RPG.pick(friendly).toString().toLowerCase();
		var terrain=Terrain.get(location.x,location.y);
		scan(location.getlocation(),terrain);
		for(var tile:new ArrayList<>(undiscovered))
			if(WorldScreen.current.mappanel.tiles[tile.x][tile.y].discovered)
				undiscovered.remove(tile);
		return !undiscovered.isEmpty();
	}

	void scan(Point p,Terrain t){
		if(!undiscovered.contains(p)&&World.validatecoordinate(p.x,p.y)
				&&t.equals(Terrain.get(p.x,p.y))){
			undiscovered.add(p);
			for(Point near:Point.getadjacent2()){
				near.x+=p.x;
				near.y+=p.y;
				scan(near,t);
			}
		}
	}

	@Override
	public void happen(Squad s){
		var success=s.getbest(Skill.DIPLOMACY).roll(Skill.DIPLOMACY);
		if(success<1) success=1;
		var undiscovered=new ArrayList<>(this.undiscovered);
		var here=location.getlocation();
		undiscovered.sort((a,b)->Double.compare(a.distance(here),b.distance(here)));
		for(var i=0;i<undiscovered.size()&&i<success;i++){
			var tile=undiscovered.get(i);
			WorldScreen.discover(tile.x,tile.y);
		}
		Javelin.redraw();
		var terrain=Terrain.get(here.x,here.y);
		var message="A friendly "+friend
				+" tells you some details about the surrounding "
				+terrain.toString().toLowerCase()+".";
		Javelin.message(message,true);
	}
}
