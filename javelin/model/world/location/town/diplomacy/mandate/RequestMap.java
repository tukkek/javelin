package javelin.model.world.location.town.diplomacy.mandate;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.world.World;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.view.screen.WorldScreen;

/**
 * Reveals area around a {@link District}.
 *
 * @author alex
 */
public class RequestMap extends Mandate{
	Set<Point> area=new HashSet<>();

	/** Reflection constructor. */
	public RequestMap(Town t){
		super(t);
		var radius=t.getdistrict().getradius()*2;
		for(var x=t.x-radius;x<=t.x+radius;x++)
			for(var y=t.y-radius;y<=t.y+radius;y++){
				var p=new Point(x,y);
				if(p.validate(0,0,World.scenario.size,World.scenario.size)) area.add(p);
			}
	}

	long countundiscovered(){
		return area.stream().filter(p->!WorldScreen.see(p)).count();
	}

	@Override
	public boolean validate(Diplomacy d){
		return countundiscovered()>0;
	}

	@Override
	public String getname(){
		return "Reveal area around "+target;
	}

	@Override
	public void act(Diplomacy d){
		for(Point p:area)
			WorldScreen.current.mappanel.tiles[p.x][p.y].discovered=true;
	}
}
