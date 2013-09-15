/*
 * Created on 19-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import tyrant.mikera.engine.BaseObject;

/**
 * @author Mike
 *
 * Events are created to notify game objects about 
 * occurences in the game that they may need to respond to
 * 
 * Typical usage:
 * 
 * Thing t=....
 * 
 * if (t.handles("OnAction")) {
 *  Event e=new Event("Action");
 *  e.set("SomeProperty",10);
 *  e.set("SomeOtherProperty","Hello");
 * 	t.handle(e);
 * }
 * 
 */
public final class Event extends BaseObject {

	private static final long serialVersionUID = 3546358448502944819L;

    public Event(String s) {
		Game.assertTrue(!s.startsWith("On"));
		set("Name",s);
		set("HandlerName",("On"+s).intern());
	}
	
	public String handlerName() {
		return getString("HandlerName");
	}
	
	public static Event createActionEvent(int time) {
		Event e=new Event("Action");
		e.set("Time",time);
		return e;
	}
}
