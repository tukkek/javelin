/*
 * Created on 19-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * Animals are wild creatures that can be hunted
 * 
 */
public class Animal {
	public static void init() {
	   Thing t;
	        
	   // base monster template
	   t=Lib.extend("base animal", "base being");
	   t.set("IsMonster",0);
	   t.set(RPG.ST_AIMODE,"Evade");
	   t.set("IsHostile",0);
	   t.set("IsAnimal",1);
	   t.set("Frequency",50);
	   t.set("IsIntelligent",0);
	   t.set("LevelMin",1);
	   t.set("LevelMax",100);
	   t.set("XPValue",5);
	   Lib.add(t);		
		
	   initWildLife();
	}
	
	public static Thing create(int level) {
		// TODO: make specific for terrain types
		Thing t=Lib.createType("IsAnimal",level);
		return t;
	}
	
	public static void initWildLife() {
		Thing t;
		
        t=Lib.extend("rabbit", "base animal");
        t.set("IsHostile",0);
        t.set("Image",285);
        t.set("IsOwned",1);
        t.set("MoveSpeed",140);
        Monster.stats(t,3,2,15,3,1,2,1,1);
        t.set("DeathDecoration","piece of rabbit meat");
        Lib.add(t);
        
        t=Lib.extend("hare","rabbit");
        t.set("MoveSpeed",240);
        Monster.stats(t,3,2,25,3,1,2,1,1);
        t.set("DeathDecoration","piece of hare meat");
        Lib.add(t);       
        
        t=Lib.extend("butterfly", "base animal");
        t.set("IsHostile",0);
        t.set("IsBlocking",0);
        t.set("Image",294);
        t.set("IsItem",1);
        t.set("DeathDecoration",null);
        t.set("ItemWeight",100);
        Monster.stats(t,1,1,5,1,1,1,1,1);
        Lib.add(t);
        
        t=Lib.extend("snail", "base animal");
        t.set("IsHostile",0);
        t.set("IsOwned",1);
        t.set("Image",387);
        t.set("MoveSpeed",40);
        Monster.stats(t,1,1,5,1,1,1,1,1);
        t.set("DeathDecoration","slime pool");
        Lib.add(t);  
        
        t=Lib.extend("escargot", "base animal");
        t.set("IsHostile",0);
        t.set("IsOwned",1);
        t.set("Image",387);
        t.set("MoveSpeed",40);
        Monster.stats(t,1,1,5,1,1,1,1,1);
        t.set("DeathDecoration","escargot steak");
        t.set("ASCII", "e");
        Lib.add(t);      
        
        t=Lib.extend("slug", "base animal");
        t.set("IsHostile",0);
        t.set("Image",386);
        t.set("MoveSpeed",40);
        Monster.stats(t,1,1,5,1,1,1,1,1);
        t.set("DeathDecoration","poison cloud");
        Lib.add(t);          
        
        t=Lib.extend("flutterby","butterfly");
        t.set("DecayRate",1000);
        t.addHandler("OnAction",Scripts.decay());
        Lib.add(t);
	}
}
