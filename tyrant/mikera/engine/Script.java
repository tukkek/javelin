/*
 * Created on 27-Jun-2004
 *
 */
package tyrant.mikera.engine;

import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.EventHandler;

/**
 * @author Mike
 *
 * Script represents a piece of code that can respond
 * to an Event.
 * 
 * Script should be subclassed with handle(...) overriden
 * in order to implement scripted behaviour
 * 
 */
public class Script extends BaseObject implements EventHandler {

	private static final long serialVersionUID = -8185055965927876189L;
	
	/**
	 * Stub event handler
	 * 
	 * @param t The Thing to which the event has been sent
	 * @param e The event to respond to 
	 */
	public boolean handle(Thing t, Event e) {
		
		return false;
	}


}
