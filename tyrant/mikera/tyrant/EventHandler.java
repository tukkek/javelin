/*
 * Created on 27-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface EventHandler {
	public boolean handle(Thing t, Event e);
}
