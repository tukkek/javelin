/*
 * Created on 12-Jun-2005
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Portal_TC extends TyrantTestCase {
    public void testTravel() {
        BattleMap map = new BattleMap(4, 4);
        Thing p=Lib.create("dungeon");
        
        map.addThing(p,1,1);
        
        Thing rock=Lib.create("rock");
        
        map.addThing(rock,1,1);
        
        BattleMap dmap=Portal.getTargetMap(p);
        assertNotNull(dmap);
        assertTrue(dmap!=map);
        
        Portal.travel(p,rock);
        assertTrue(Portal.getTargetMap(p)==dmap);
        assertTrue(rock.getMap()==dmap);
    }

    
    public void testSetDestination() {
        BattleMap map = new BattleMap(4, 4);
        Thing p=Lib.create("dungeon");
        map.addThing(p,1,1);
        Thing rock=Lib.create("rock");
        Portal.setDestination(p,map,2,3);
        
        Portal.travel(p,rock);
        assertEquals(2,rock.getMapX());
        assertEquals(3,rock.getMapY());
        assertTrue(rock.getMap()==map);
    }
    
    public void testPitTraps() {
    	for (int i=0; i<100; i++) {
            BattleMap map = new BattleMap(4, 4);         
    		Thing pit=Lib.create("pit trap");
    		map.addThing(pit,1,1);
    		
    		Trap.trigger(pit);
    		
    		// check that the pit trap has become a pit
    		assertEquals("pit",pit.name());
    		
            BattleMap dmap=Portal.getTargetMap(pit);
            int tx=pit.getStat("PortalTargetX");
            int ty=pit.getStat("PortalTargetY");
            
            Thing ent=dmap.getEntrance();
            assertNotNull(ent);
            assertEquals(ent.getMap(),dmap);
            int ex=ent.getMapX();
            int ey=ent.getMapY();
            assertTrue(ex>=0);
            assertTrue(ey>=0);
            assertTrue(ex<dmap.getWidth());
            assertTrue(ey<dmap.getHeight());
            
            assertNotNull(dmap);
            assertTrue(tx>=0);
            assertTrue(ty>=0);
            assertTrue(tx<dmap.getWidth());
            assertTrue(ty<dmap.getHeight());
            
    		testPortal(pit);
    	}
    }
    
    // test a portal already on a map
    private void testPortal(Thing p) {
        Thing rock=Lib.create("rock");

        Portal.travel(p,rock);
        assertNotNull(rock.getMap());
        
        BattleMap dmap=Portal.getTargetMap(p);
        assertNotNull(dmap);
        assertTrue(rock.getMap()==dmap);
        
    }
}
