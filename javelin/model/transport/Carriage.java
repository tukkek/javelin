package javelin.model.transport;

import javelin.controller.content.terrain.hazard.Break;

/**
 * Pushed by horse.
 *
 * @see Break
 */
public class Carriage extends Transport{
	/** Price for a carriage in gold pieces ($). */
	protected static final int PRICE=100;

	/** Constructor. */
	public Carriage(){
		super("Carriage",20,6,2,Carriage.PRICE,1);
		parkeable=false;
	}
}
