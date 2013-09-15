/*
 * Created on 27-Dec-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LOS_TC extends TyrantTestCase {
    private MapHelper mapHelper;

    protected void setUp() throws Exception {
        super.setUp();
        mapHelper = new MapHelper();
    }
    
    public void testCanSee() {
        String mapString = 
          // 0123456789
            "----------" + "\n" +
            "|...@....|" + "\n" +
            "|...-....|" + "\n" +
            "|........|" + "\n" +
            "|........|" + "\n" +
            "----------";
        
        BattleMap map = mapHelper.createMap(mapString);
        Thing h=map.getMobile(4,1);
        
        h.calculateVision();
        assertTrue(map.isVisible(3,1));
        assertTrue(map.isVisible(4,1));
        assertTrue(map.isVisible(4,2));
        assertFalse(map.isVisible(4,3));
		
		assertTrue(map.isHeroLOS(3,1));
		assertTrue(map.isHeroLOS(4,1));
		assertFalse(map.isHeroLOS(4,3));
        
        h.set("IsBlind",1);
        h.calculateVision();
        assertFalse(map.isVisible(3,1));
		assertFalse(map.isVisible(4,1));
		
		// blindness shouldn't affect LOS
		assertTrue(map.isHeroLOS(3,1));
		assertTrue(map.isHeroLOS(4,1));
		assertFalse(map.isHeroLOS(4,3));
        
    }
}
