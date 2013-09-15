package tyrant.mikera.tyrant.test;

import javelin.controller.Movement;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Skill;
import tyrant.mikera.tyrant.Tile;

/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class Movement_TC extends TyrantTestCase {
    public void testMovement() {
        BattleMap map = new BattleMap(4, 4);
        map.fillArea(0,0,3,3,Tile.FLOOR);
        map.addThing("wall", 0, 0);
        Thing rabbit = map.addThing("rabbit", 1, 0);
        map.addThing("stone", 2, 0);
        
        assertTrue(Movement.canMove(rabbit, map, rabbit.getMapX() + 1, rabbit.getMapY()));
        assertTrue(Movement.canMove(rabbit, map, rabbit.getMapX(), rabbit.getMapY() + 1));
        assertFalse(Movement.canMove(rabbit, map, rabbit.getMapX() - 1, rabbit.getMapY()));
        assertFalse(Movement.canMove(rabbit, map, rabbit.getMapX(), rabbit.getMapY() - 1));
    }
    
    public void testDoorOpening() throws Exception {
        String mapString = 
            "----" + "\n" +
            "|@+|" + "\n" +
            "----";
         new MapHelper().createMap(mapString);
         assertTrue(Movement.tryMove(hero, hero.getMap(), 2, 1));
         assertTrue(getDoor(hero.getMap(), 2, 1).getFlag("IsOpen"));
    }
    
    public void testDoorOpening_whileRunning() throws Exception {
        String mapString = 
            "----" + "\n" +
            "|@+|" + "\n" +
            "----";
         new MapHelper().createMap(mapString);
         hero.isRunning(true);
         assertFalse(Movement.tryMove(hero, hero.getMap(), 2, 1));
         assertFalse(getDoor(hero.getMap(), 2, 1).getFlag("IsOpen"));
    }

    private Thing getDoor(BattleMap map, int x, int y) {
        Thing[] things = map.getThings(x, y);
        for (int i = 0; i < things.length; i++) {
            Thing thing = things[i];
            if(thing.getFlag("IsDoor")) return thing;
        }
        return null;
    }
    
    /**
     * canMove(...) tests whether a move is phyiclly possible
     * 
     * it may still be stupid, e.g. jumping into lava....
     *
     */
    public void testCanMove() {
    	String mapString = 
            "####" + "\n" +
            "#@.#" + "\n" +
            "####";
        BattleMap m=new MapHelper().createMap(mapString);
        
        Thing h=getTestHero();
        assertTrue(Movement.canMove(h,m,2,1));
        
        m.setTile(2,1,Tile.CAVEWALL);
        assertTrue(!Movement.canMove(h,m,2,1));
        
        // can jump in river - even if not sensible!
        m.setTile(2,1,Tile.RIVER);
        assertTrue(Movement.canMove(h,m,2,1));
        
        m.setTile(2,1,Tile.TREE);
        assertTrue(!Movement.canMove(h,m,2,1));
        
        // can jump in lava - even if not sensible!
        m.setTile(2,1,Tile.LAVA);
        assertTrue(Movement.canMove(h,m,2,1));
        
        // can pass through mountains with climbing skill
        m.setTile(2,1,Tile.MOUNTAINS);
        assertTrue(!Movement.canMove(h,m,2,1));
        h.incStat(Skill.CLIMBING,1);
        assertTrue(Movement.canMove(h,m,2,1));
        h.incStat(Skill.CLIMBING,-1);
        assertTrue(!Movement.canMove(h,m,2,1));
        
        // can pass through solid walls if ethereal
        m.setTile(2,1,Tile.CAVEWALL);
        h.incStat("IsEthereal",1);
        assertTrue(Movement.canMove(h,m,2,1));
        h.incStat("IsEthereal",-1);
        assertTrue(!Movement.canMove(h,m,2,1));
        
        // can't pass through solid walls if ethereal
        m.setTile(2,1,Tile.CAVEWALL);
        h.incStat("IsFlying",1);
        assertTrue(!Movement.canMove(h,m,2,1));
        h.incStat("IsFlying",-1);
        assertTrue(!Movement.canMove(h,m,2,1));
    }
    
    public void testBlockingSecnery() {
    	String mapString = 
            "####" + "\n" +
            "#@.#" + "\n" +
            "####";
        BattleMap m=new MapHelper().createMap(mapString);
        
        Thing h=getTestHero();
        assertTrue(Movement.canMove(h,m,2,1));
        
        Thing s=Lib.create("tree");
        m.addThing(s,2,1);
        assertFalse(Movement.canMove(h,m,2,1));
        s.remove();
        assertTrue(Movement.canMove(h,m,2,1));
   
        s=Lib.create("menhir");
        m.addThing(s,2,1);
        assertFalse(Movement.canMove(h,m,2,1));
        s.remove();
        assertTrue(Movement.canMove(h,m,2,1));
        
        s=Lib.create("[IsAltar]");
        m.addThing(s,2,1);
        assertFalse(Movement.canMove(h,m,2,1));
        s.remove();
        assertTrue(Movement.canMove(h,m,2,1));
        
        s=Lib.create("door");
        m.addThing(s,2,1);
        assertFalse(Movement.canMove(h,m,2,1));
        s.remove();
        assertTrue(Movement.canMove(h,m,2,1));
        
        // can walk over tree stump
        s=Lib.create("tree stump");
        m.addThing(s,2,1);
        assertTrue(Movement.canMove(h,m,2,1));
        s.remove();
        assertTrue(Movement.canMove(h,m,2,1));
    }
}
