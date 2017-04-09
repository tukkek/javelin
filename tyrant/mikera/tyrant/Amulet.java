/*
 * Created on 28-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Amulet {
    private static String[] types = {"gold", "silver", "ivory","ruby","firestone","amber","aquamarine","amethyst","emerald"};
    private static int[]   images = {  400,  401,      401,    402,   402,        403,    404,         405,       406,      };
    
    private static void addNecklace(Thing t) {
        int type=RPG.r(types.length);
        
        t.set("ImageSource","Items");
        t.set("Image",images[type]);
        t.set("UName",types[type]+" necklace");
        
        // fix up plural name if needed
        String name=t.getstring("Name");
        if (name.indexOf("necklace")==0) {
            String s="necklaces"+name.substring(8);
            t.set("NamePlural",s);
        }
        
        if (name.indexOf("amulet")==0) {
            String s="amulets"+name.substring(6);
            t.set("NamePlural",s);
        }
        
        if (name.indexOf("talisman")==0) {
            String s="talismans"+name.substring(6);
            t.set("NamePlural",s);
        }
        
        Lib.add(t);
    }
  
    public static void init() {
		Thing t=Lib.extend("base necklace","base item");
		t.set("ItemWeight",500);
		t.set("ValueBase",700);
		t.set("LevelMin",1);
		t.set("IsNecklace",1);
		t.set("HPS",8);
		t.set("Frequency",30);
		t.set("WieldType",RPG.WT_NECK);
		Lib.add(t);
		
		initNecklaces();
		initAmulets();
	}
	
	public static void initNecklaces() {
		for (int i=0; i<types.length; i++) {
			Thing t=Lib.extend(types[i]+" necklace","base necklace");
			addNecklace(t);
		}
		
	}
	
	public static void initAmulets() {
		Thing t;
		
		t=Lib.extend("base amulet","base necklace");
		t.set("IsMacicItem",1);
		t.set("ValueBase",1000);
		Lib.add(t);
		
		t=Lib.extend("amulet of wrath","base amulet");
		t.set("LevelMin",18);
        t.additem("WieldedModifiers",Modifier.bonus(Skill.ATTACK,RPG.d(3)));
        t.additem("WieldedModifiers",Modifier.bonus(RPG.ST_ATTACKSPEED,RPG.d(2,20)));
		addNecklace(t);
		
		t=Lib.extend("amulet of regeneration","base amulet");
		t.set("LevelMin",3);
        t.additem("WieldedModifiers",Modifier.bonus(RPG.ST_REGENERATE,30));
		addNecklace(t);
		
		t=Lib.extend("amulet of protection","base amulet");
		t.set("LevelMin",9);
        t.additem("WieldedModifiers",Modifier.bonus("ARM",RPG.d(6)));
		addNecklace(t);	
		
		t=Lib.extend("amulet of confusion","base amulet");
		t.set("LevelMin",7);
        t.additem("WieldedModifiers",Modifier.bonus("IsConfused",1));
        t.set("IsCursed",1);
		addNecklace(t);	
		
		t=Lib.extend("amulet of spellcasting","base amulet");
		t.set("LevelMin",12);
        t.additem("WieldedModifiers",Modifier.bonus(Skill.CASTING,RPG.d(3)));
        t.additem("WieldedModifiers",Modifier.bonus("IN",RPG.d(2,3)));
		addNecklace(t);	
		
		t=Lib.extend("amulet of courage","base amulet");
		t.set("LevelMin",2);
        t.additem("WieldedModifiers",Modifier.bonus(Skill.BRAVERY,RPG.d(3)));
		addNecklace(t);	
		
		t=Lib.extend("amulet of motion","base amulet");
		t.set("LevelMin",16);
        t.additem("WieldedModifiers",Modifier.linear(RPG.ST_ENCUMBERANCE,70,0));
		addNecklace(t);	
		
		t=Lib.extend("amulet of fire resistance","base amulet");
		t.set("LevelMin",5);
        t.additem("WieldedModifiers",Modifier.bonus("RES:fire",10));
		addNecklace(t);	
		
		t=Lib.extend("amulet of ice resistance","base amulet");
		t.set("LevelMin",10);
        t.additem("WieldedModifiers",Modifier.bonus("RES:ice",10));
		addNecklace(t);	
		
		t=Lib.extend("amulet of acid resistance","base amulet");
		t.set("LevelMin",15);
        t.additem("WieldedModifiers",Modifier.bonus("RES:acid",10));
		addNecklace(t);	
		
		t=Lib.extend("amulet of shock resistance","base amulet");
		t.set("LevelMin",20);
        t.additem("WieldedModifiers",Modifier.bonus("RES:shock",10));
		addNecklace(t);	
		
		t=Lib.extend("amulet of chill resistance","base amulet");
		t.set("LevelMin",25);
        t.additem("WieldedModifiers",Modifier.bonus("RES:chill",10));
		addNecklace(t);	
		
		t=Lib.extend("amulet of poison resistance","base amulet");
		t.set("LevelMin",30);
        t.additem("WieldedModifiers",Modifier.bonus("RES:poison",10));
		addNecklace(t);	
		
		t=Lib.extend("amulet of disintegration resistance","base amulet");
		t.set("LevelMin",35);
        t.additem("WieldedModifiers",Modifier.bonus("RES:disintegrate",10));
		addNecklace(t);	
	}
}
