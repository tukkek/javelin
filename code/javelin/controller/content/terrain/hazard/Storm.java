package javelin.controller.content.terrain.hazard;

import javelin.controller.Weather;

/**
 * A storm on the sea will throw everything about, but not destroy it.
 *
 * @author alex
 */
public class Storm extends Hazard{

	@Override
	public void hazard(int hoursellapsed){
		GettingLost.getlost("The wild sea trashes you about!",0);
	}

	@Override
	public boolean validate(){
		return Weather.current==Weather.STORM;
	}
}
