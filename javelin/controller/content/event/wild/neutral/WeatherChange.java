package javelin.controller.content.event.wild.neutral;

import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.content.event.wild.WildEvent;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * Immediately changes the weather.
 *
 * @author alex
 */
public class WeatherChange extends WildEvent{
	public WeatherChange(PointOfInterest l){
		super("Weather change",l);
	}

	@Override
	public void happen(Squad s){
		var previous=Weather.current;
		List<Integer> options=Arrays.asList(Weather.DISTRIBUTION);
		while(Weather.current==previous)
			Weather.current=RPG.pick(options);
		String result;
		if(Weather.current==Weather.CLEAR)
			result="The sky seems to clear, finally...";
		else if(Weather.current==Weather.RAIN)
			result="A light rain takes over...";
		else if(Weather.current==Weather.STORM)
			result="A sudden storm comes by!";
		else if(Javelin.DEBUG)
			throw new IllegalArgumentException("Unknonw weather "+Weather.current);
		else
			result="The weather suddenly changes!";
		Javelin.message(result,true);
	}
}
