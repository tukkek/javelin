package javelin.model.diplomacy.mandate;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.Point;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.world.World;
import javelin.model.world.location.town.District;
import javelin.view.screen.WorldScreen;

/**
 * Reveals area around a {@link District}.
 *
 * @author alex
 */
public class RequestMap extends Mandate{
	Set<Point> area=new HashSet<>();

	/** Reflection constructor. */
	public RequestMap(Relationship r){
		super(r);
		var t=r.town;
		var radius=t.getdistrict().getradius()+t.getrank().rank;
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
		return target.getstatus()>=Relationship.FRIENDLY&&countundiscovered()>0;
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
