package tyrant.mikera.tyrant.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javelin.controller.old.Game;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.*;


/**
 * @author Chris Grindstaff chris@gstaff.org
 */
public class Lib_TC extends TyrantTestCase {
    
    private List all=null;

    public void testEmpty() {
        Lib lib = new Lib();
        assertEquals(0, lib.getAll().size());
    }
    
    public void testExtend() {
    	Thing test1=Lib.extend("extend test item 1","base item");
    	assertEquals(1,test1.getStat("IsItem"));
    	assertNotNull(test1.getInherited());
    	
    	Thing test2=Lib.extendCopy("extend test item 2","base item");
    	assertEquals(1,test2.getStat("IsItem"));
    	assertEquals(null,test2.getInherited());
    }

    public void testLibLinks() {
    	assertEquals(Game.instance().get("Library"),Lib.instance());
    	
    }
    
    public void testIdentity() {
        Thing rabbit = Lib.create("rabbit");
        Thing rabbit2 = Lib.create("rabbit");
        assertNotSame(rabbit, rabbit2);
    }
    
    public void testCreate_count() {
        assertEquals(500, Lib.create("500 carrot juice").getNumber());
        assertEquals(501, Lib.create("501carrot juice").getNumber());
    }
    
    public void testCreate_percentage() {
        assertNull(Lib.create("0%         carrot juice"));
        assertNotNull(Lib.create("100%         carrot juice"));
    }
    
    public void testCreate_KleeneStar() {
        RPG.setRandSeed(0);
        Thing carrotJuice = Lib.create("100* carrot juice");
        assertEquals(61, carrotJuice.getNumber());
        assertEquals(1, Lib.create("0* carrot juice").getNumber());
    }
    
    public void testCreate_type() {
        assertEquals(1, Lib.create("[IsEquipment]").getLevel());
        assertEquals(1, Lib.create("[IsFood]").getLevel());
    }

    /**
     * Helper function to work out the type of property
     * 
     * @param value Any object found in Thing properties
     * @return String detailing the type of the object
     */
    private String getPropertyType(Object value) {
    	if (value==null) return null;
    	if (value instanceof String) return "String";
    	if (value instanceof Integer) return "Integer";
    	if (value instanceof Modifier[]) return "Modifier[]";
    	if (value instanceof Modifier) return "Modifier";
    	if (value instanceof Script) return "Script";
    	return "Object";
    }

    public void setUp() throws Exception {
    	super.setUp();
    	all=Lib.instance().getAll();
    }

    /**
     * Test creation of all objects in the game library
     * Name should be preserved
     *
     */
    public void testCreate() {
    	Iterator it=all.iterator();
    	while (it.hasNext()) {
            BaseObject p = (BaseObject) it.next();
    		String name=(String)p.get("Name");
    		
    		Thing t=Lib.create((String)p.get("Name"));
    		
    		assertTrue("["+name+"] not created with correct Name",t.name()==name);
    	}
    }
    
    public void testCreateIgnoreCase() {
    	Thing t=Lib.createIgnoreCase("fIreBAll");
    	assertEquals(t.name(),"Fireball");
    	
    }


    /**
     * Test all objects flagged as items
     *
     */
    public void testItems() {
    	Iterator it=all.iterator();
    	while (it.hasNext()) {
            BaseObject p = (BaseObject) it.next();	
    		String name=(String)p.get("Name");
    		if (name.startsWith("base ")) continue;
    		
    		Thing t=Lib.create(name);
    		
    		if (t.getFlag("IsItem")) {
    			assertTrue("["+name+"] has no weight",t.getStat("ItemWeight")>0);
    			assertTrue("["+name+"] has no LevelMin",t.getStat("LevelMin")>0);
    			assertTrue("["+name+"] has no HPS",t.getStat("HPS")>0);
    		
    			// some test functions
    			try {
    				Item.value(t);
    				Item.inspect(t);
    				Item.bless(t);
    				Item.curse(t);
    				Item.repair(t,true);
    			
    			
    			} catch (Throwable e) {
    				throw new Error("Exception testing ["+name+"]",e);
    			}
    			
    			if (t.getFlag("WieldType")) {
    				hero.addThing(t);
    				hero.wield(t);
    				t.remove();
    			}
    		}
    	}
    }

    /**
     * Test all properties in library for type consistency
     *
     */
    public void testPropertyTypes() {
    	Iterator it=all.iterator();
    	HashMap types=new HashMap();
    	HashMap seen=new HashMap();
    	while (it.hasNext()) {
            BaseObject p = (BaseObject) it.next();
    		String name=(String)p.get("Name");
    		
    		Thing t=Lib.create(name);
    		Map h = t.getCollapsedMap();
    		
    		for (Iterator hi=h.keySet().iterator();hi.hasNext();) {
    			String key=(String)hi.next();
    			
    			Object value=t.get(key);
    			
    			String type=getPropertyType(value);
    			String seenType=(String)types.get(key);
    			if ((type!=null)&&(seenType!=null)) {
    				assertTrue("["+name+"] has property ["+key+"] of unexpected type ["+type+"] "+seen.get(key),type.equals(seenType));
    			} else if (type!=null) {
    				// record the observed type
    				types.put(key,type);
    				seen.put(key,"("+type+" for "+name+")");
    			}			
    		}
    	}
    }
    
    /**
     * TEST for https://sourceforge.net/forum/message.php?msg_id=2916833
     */
    public void testArmour() throws Exception {
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            BaseObject thingAsProperties = (BaseObject) iter.next();
            String name = (String) thingAsProperties.get("Name");
            if (name.startsWith("base ")) continue;
            Thing thing = Lib.create(name);
            if (thing.getFlag("IsArmour") && thing.getStat("WieldType") == RPG.WT_BOOTS) {
                assertTrue("boot " + name + " should specify attack", thing.getStat(RPG.ST_ASKMULTIPLIER) > 0);
            }
        }
    }
    
    public void testCreateTypeAndLevels() throws Exception {
        Lib oldLib=Lib.instance();
    	try {
            Lib.setInstance(new Lib());
            Thing thing = new Thing();
            thing.set("Name", "tree");
            thing.set("IsTree", true);
            thing.set("LevelMin", 7);
            thing.set("Frequency", 100);
            thing.set("LevelMax", 19);
            Lib.add(thing);
            assertNotNull(Lib.createType("IsTree", 7));
            assertNotNull(Lib.createType("IsTree", 1));
            assertNotNull(Lib.createType("IsTree", 20));
        } finally {
            Lib.setInstance(oldLib);
        }
    }
}
