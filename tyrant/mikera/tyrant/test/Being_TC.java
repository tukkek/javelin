/*
 * Created on 06-Jan-2005
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Being;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Being_TC extends TyrantTestCase {
	public void testItemHandling() {
    	String mapString = 
            "####" + "\n" +
            "#@.#" + "\n" +
            "####";
        BattleMap m=new MapHelper().createMap(mapString);
        
        Thing h=getTestHero();
		
        Thing rock=Lib.create("rock");
        m.addThing(rock,1,1);
        
        Being.tryPickup(h,rock);
        assertSame(rock.place, h);
        
        Being.tryDrop(h,rock);
        assertNotSame(rock.place, h);
        assertSame(rock.place, m);
        assertEquals(rock.x, h.x);
        assertEquals(rock.y, h.y);
        
        try {
            Being.tryDrop(h,rock);
        	fail("Should throw exception trying to drop Thing not in inventory");
        } catch (Throwable t) {
        	// OK
        }
	}
}
