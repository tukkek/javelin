package tyrant.mikera.tyrant.test;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

public class Properties_TC extends TyrantTestCase {
    public void testPut() throws Exception {
        Thing carrot = Lib.create("carrot");
        int originalBaseSize = carrot.getInherited().size();
        
        Object valueInCarrot = carrot.get("HPS");
        carrot.set("HPS", valueInCarrot);
        assertEquals(originalBaseSize, carrot.getInherited().size());
    }

    public void testDefault() {
    	Thing thing = new Thing();
    	assertEquals(0, thing.getStat("A"));
    	assertNull(thing.get("B"));
    }
    
    public void testFlattening() {
    	Thing thing = new Thing();
    	thing.set("A",1);
    	
    	Thing nt=new Thing(thing);
    	
    	assertEquals(1,nt.getStat("A"));
    	
    	// change in base object should propagate
    	thing.set("A",2);
    	assertEquals(2,nt.getStat("A"));
    	
    	nt.flattenProperties();
    	
    	// change in base object should not propagate
    	// since we have now performed flattening
    	thing.set("A",3);
    	assertEquals(2,nt.getStat("A"));
    	
    }


    public void testSet() {
    	Thing parent = new Thing();
    	
    	parent.set("A", 1);
    	assertEquals(1, parent.getStat("A"));
    	
    	Thing child = new Thing(parent);
    	
    	// change parent while child is still referencing it
    	assertEquals(1, parent.getStat("A"));
    	parent.set("A",2);
    	assertEquals(2, child.getStat("A"));
    	parent.set("A",1);
    	assertEquals(1, child.getStat("A"));
    	
    	// set q to lower value
    	parent.set("A",2);
    	child.set("A",1);
    	assertEquals(2, parent.getStat("A"));
    	assertEquals(1, child.getStat("A"));
    	
    	// set q to same value as p
    	child.set("A",2);
    	assertEquals(2, child.getStat("A"));
    	
    	// should now be overriding p
    	parent.set("A",5);
    	assertEquals(2, child.getStat("A"));
    }
    
    public void testChain() {
    	Thing a=new Thing();
    	Thing b=new Thing(a);
    	Thing c=new Thing(b);
    	
    	b.set("A",1);
    	assertEquals(1, c.getStat("A"));
    	
    	a.set("A",2);
    	assertEquals(1, c.getStat("A"));
    
    	// test setting c to parent values
    	c.set("A",3);
    	assertEquals(3, c.getStat("A"));
    	
    	c.set("A",2);
    	assertEquals(2, c.getStat("A"));
    	
    	c.set("A",1);
    	assertEquals(1, c.getStat("A"));
    	
    	c.set("A",0);
    	assertEquals(0, c.getStat("A"));
    }

}
