/*
 * Created on 12-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Time {
	public static void advance(int t) {
		int advance=t;
		Thing h=Game.hero();
		
		//Game.warn("Time.advance: "+t);
		
		while (advance >= 0) {
			BattleMap map=h.getMap();
			
			// divide into smaller steps if needed
			int step = advance;

			// call map action
			if (map!=null) {
				if ((step > 100)&&(!map.getFlag("IsWorldMap")))
					step = 100;
				
				Event actionEvent=Event.createActionEvent(step);
				map.action(actionEvent);
				Quest.notifyAction(actionEvent);
			}
			
			advance -= step;
			
			if (advance==0) break;
		}
		
		logTime(t);
		h.incStat("TurnCount",1);
	}
	
	public static void logTime(int t) {
		Thing h=Game.hero();
		h.incStat("GameTime",t);	
		
	}
	
}