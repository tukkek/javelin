package javelin.controller.event.wild.negative;

import java.util.ArrayList;

import javelin.controller.event.wild.WildEvent;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * Triggers a {@link Hazard}.
 *
 * @author alex
 */
public class FindHazard extends WildEvent{
	Hazard hazard;

	/** Reflection-friendly constructor. */
	public FindHazard(){
		super("Find hazard");
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		var hazards=new ArrayList<>(Terrain.get(l.x,l.y).gethazards(true));
		for(var h:RPG.shuffle(hazards))
			if(h.validate()){
				hazard=h;
				break;
			}
		return hazard!=null;
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		hazard.hazard(Math.round(s.move(false,Terrain.current(),l.x,l.y)));
	}
}
