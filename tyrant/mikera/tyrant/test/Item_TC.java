package tyrant.mikera.tyrant.test;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;

/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class Item_TC extends TyrantTestCase {
    
    public void testIdentify() {
        Thing ring = Lib.create("The One Ring");
        assertFalse(Item.isIdentified(ring));
        Item.identify(ring);
        assertTrue(Item.isIdentified(ring));
    }
    
    public void testRemove() {
    	// make some arrows
        Thing arrow = Lib.create("arrow");
        int num=arrow.getNumber();
        assertTrue(num>1);
    	Thing one=arrow.remove(1);
    	
    	assertEquals(null,one.place);
        assertEquals(1,one.getNumber());
        assertEquals(num-1,arrow.getNumber());
        
        assertEquals(arrow.getInherited(),one.getInherited());
    }

    public void testSeparate() {
    	// make some arrows
        Thing arrow = Lib.create("arrow");
        arrow.set("Number",100);
        int num=arrow.getNumber();
        assertEquals(100,num);
        
        // make a person to carry the arrows
        Thing person=Lib.create("goblin");
        person.addThing(arrow);
        
        Thing[] ts=new Thing[10];
        for (int i=0; i<10; i++) {
        	ts[i]=arrow.separate(i+1);
        }
        
        assertEquals(45,arrow.getNumber());
        
        for (int i=0; i<10; i++) {
        	assertEquals(i+1,ts[i].getNumber());
        	assertEquals(person,ts[i].place);
        	ts[i].remove();
        	assertEquals(null,ts[i].place);
        }
        
        for (int i=0; i<10; i++) {
        	ts[i]=person.addThingWithStacking(ts[i]);
        }
        
        // should all have been returned to original stack
        assertEquals(100,arrow.getNumber());
    }
    
    public void testDropCursed() {
    	BattleMap map=new BattleMap(1,1);
        
    	// make some arrows
        Thing arrow = Lib.create("arrow");
        int num=arrow.getNumber();
        assertTrue(num>1);
        
        // put a person on the map
        Thing person=Lib.create("goblin");
        person.addThing(arrow);
        map.addThing(person,0,0);
        
        arrow.set("IsCursed",1);
        person.wield(arrow);
        
        Being.tryDrop(person,arrow);
        
        assertEquals(person,arrow.place);
            
    }
    
    public void testDropStacking() {
    	BattleMap map=new BattleMap(1,1);
    
    	// make some arrows
        Thing arrow = Lib.create("arrow");
        int num=arrow.getNumber();
        assertTrue(num>1);
        
        // put a person on the map
        Thing person=Lib.create("goblin");
        person.addThing(arrow);
        map.addThing(person,0,0);
        
        // drop in two batches
        Thing drop1=arrow.separate(2);
        Being.tryDrop(person,drop1);
        Thing drop2=arrow.separate(3);
        Being.tryDrop(person,drop2);
        
        assertEquals(num-5,arrow.getNumber());
        assertNotNull(drop1.place);
        assertEquals(5,drop1.getNumber());
    }
    
    public void testStacking() {
    	// make some arrows
        Thing arrow = Lib.create("arrow");
        int num=arrow.getNumber();
        assertTrue(num>1);
        
        // add to person
        Thing person=Lib.create("goblin");
        person.addThing(arrow);
        
        // separate one arrow
        Thing one=arrow.separate(1);
        assertEquals(person,arrow.place);
        assertEquals(person,one.place);
        assertEquals(1,one.getNumber());
        assertEquals(num-1,arrow.getNumber());
        assertTrue(one.equalsIgnoreNumber(arrow));
        assertEquals(arrow.getInherited(),one.getInherited());

        
        // remove and add without stacking
        one.remove();
        person.addThing(one);
        assertEquals(person,arrow.place);
        assertEquals(person,one.place);
        assertEquals(1,one.getNumber());
        assertEquals(num-1,arrow.getNumber());
        
        // now re-add with stacking
        one.remove();
        person.addThingWithStacking(one);
	
		// arrow stack should have full number restored
		assertEquals(num,arrow.getNumber());
        
        // one should be left floating
        assertEquals(1,one.getNumber());
		assertNull(one.place);
    }
    
    public void testOwned() {
        Thing it = Lib.create("carrot");
        assertFalse(Item.isOwned(it));
        it.set("IsOwned",1);
        assertTrue(Item.isOwned(it));
    }
    
    public void testTouching() {
        Thing flower = Lib.create("kahnflower");
        int hitPoints = person.getStat("HPS");
        Item.touch(person, flower);
        assertTrue(person.getStat("HPS") < hitPoints);
    }
	
    public void testPickup() {
        BattleMap m=new BattleMap(3,3);
		m.fillArea(0,0,2,2,Tile.CAVEFLOOR);
		Thing a=Lib.create("carrot");
		m.addThing(a,1,1);
		
		Thing b=Lib.create("orc");
		m.addThing(b,1,1);
		
		// check pickup works
		assertTrue(a.place==m);
		assertTrue(Item.pickup(b,a));
		assertTrue(a.place==b);
		
		Thing c=Lib.create("apple");
		m.addThing(c,1,1);
		c.set("IsOwned",1);
		
		// check pickup clears ownership flag
		assertTrue(Item.isOwned(c));
		assertTrue(Item.pickup(b,c));
		assertTrue(c.place==b);
		assertFalse(Item.isOwned(c));
    }
    
    public void testScript() {
        final Thing carrot = Lib.create("carrot");
        final boolean[] handleCalled = new boolean[] {false};
        carrot.addHandler("OnBob", new EventHandler() {
            public boolean handle(Thing thing, Event event) {
                assertEquals(carrot, thing);
                handleCalled[0] = true;
                return true;
            }
        });
        assertTrue(carrot.handles("OnBob"));
        assertFalse(handleCalled[0]);
        assertTrue(carrot.handle(new Event("Bob")));
        assertTrue(handleCalled[0]);
        
    }
    
    public void testScript_several_withEventPropagation() {
        final Thing carrot = Lib.create("carrot");
        final String[] value = new String[] {""};
        final boolean[] toReturn = new boolean[] {true};
        carrot.addHandler("OnBob", new EventHandler() {
            public boolean handle(Thing thing, Event event) {
                value[0] += "a";
                return toReturn[0];
            }
        });
        carrot.addHandler("OnBob", new EventHandler() {
            public boolean handle(Thing thing, Event event) {
                value[0] += "b";
                return toReturn[0];
            }
        });
        carrot.addHandler("OnBob", new EventHandler() {
            public boolean handle(Thing thing, Event event) {
                value[0] += "c";
                return toReturn[0];
            }
        });
        assertTrue(carrot.handles("OnBob"));
        assertEquals("", value[0]);
        assertTrue(carrot.handle(new Event("Bob")));
        assertEquals("a", value[0]);
        
        value[0] = "";
        toReturn[0] = false;
        assertFalse(carrot.handle(new Event("Bob")));
        assertEquals("abc", value[0]);
    }
}

