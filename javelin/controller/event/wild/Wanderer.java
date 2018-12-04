package javelin.controller.event.wild;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;

public abstract class Wanderer extends WildEvent{

	public Wanderer(String name){
		super(name);
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		return Weather.current!=Weather.STORM
				&&Javelin.getperiod()!=Javelin.PERIODNIGHT;
	}

}