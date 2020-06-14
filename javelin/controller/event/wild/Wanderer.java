package javelin.controller.event.wild;

import javelin.controller.Weather;
import javelin.model.unit.Squad;
import javelin.model.world.Period;
import javelin.model.world.location.PointOfInterest;

public abstract class Wanderer extends WildEvent{

	public Wanderer(String name,PointOfInterest l){
		super(name,l);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return Weather.current!=Weather.STORM&&!Period.NIGHT.is();
	}

}