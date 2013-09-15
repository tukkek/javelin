// A magic wand casts a particular type of spell
// It has it's own supply of magic points (mps)
//
// It can be recharged, although this will occasionally destroy the wand
//
// If broken, the wand will fire it's ramianing charge randomly

package tyrant.mikera.tyrant;

import java.util.*;

import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;



public class Wand  {
    // 27-08-04
	// added new names for the wands,
	// names supplied by DragonLady.
    private static String[] wandtypes= {"willow",
    		"oak","chestnut","birch","elm",
			"acacia","eucalyptus","metal",
			"ivory","sandalwood","crystal",
			"oak","ebony","hazelwood",
			"phoenix feathered","unicorn horn",
			"narwhale horn","silver plated",
			"gold plated","copper plated",
			"brass plated","mithral plated",
			"ash","rowen","redwood","ironwood",
			"cottonwood","pine","treantwood",
			"applewood","pearwood","mahogany",
			"cherrywood","bone","ambre","jade",
			"bamboo","steel","brass","copper",
			"mithral","silver","gold","green",
			"red","blue","black","white",
			"yellow","orange","purple",
			"indigo","rainbow"
			};
    private static String[] wandAdj = {
    		"bent", 
			"carved", 
			"rusty", 
			"faded", 
			"sticky", 
			"shiny", 
			"dull", 
			"cracked", 
			"wavy", 
			"glowing", 
			"thin", 
			"fat", 
			"heavy", 
			"light", 
			"gem encrusted", 
			"jewelled",
			"spiral", 
			"long", 
			"short", 
			"feathered", 
			"scaled"};
    private static int[]    images   = {288,
    		288,288,288,288,288,288,290,288,
			288,289,288,290,288,288,288,288,
			290,290,290,290,290,288,288,288,
			290,288,288,288,288,288,288,288,
			290,289,289,288,290,290,290,290,
			290,290,289,289,289,289,289,289,
			289,289,289,289};
    
    
    private static class WandScript extends Script {
    	private static final long serialVersionUID = 8376119664408202073L;

        public boolean handle(Thing t, Event e) {
    		int charges=t.getStat("Charges");
    		Thing user=e.getThing("User");
    		if (charges>0) {
    			user.message("You wave "+t.getTheName());
    			Thing s=Spell.create(t.getString("WandSpell"));
    			QuestApp.getInstance().getScreen().castSpell(user,s);
    			Item.identify(t);
    			t.set("Charges",charges-1);
    		} else {
    			user.message("The wand appears useless");
    		}
    		return false;
    	}
    }
    
    private static class DestroyWandScript extends Script {
        private static final long serialVersionUID = 6191533767727822085L;

        public boolean handle(Thing t, Event e) {
            String spell=t.getString("WandSpell");
            Thing s=Spell.create(spell);
            Spell.castAtLocation(s,null,(BattleMap)e.get("DeathMap"),e.getStat("DeathX"),e.getStat("DeathY"));
        	return false;
        }
    }    
    
    private static void initWand(Thing spell) {
        int type=RPG.r(wandtypes.length);
        int adj=RPG.r(wandAdj.length);
        Thing t=Lib.extend("xxx wand","base wand");

        String spellName=spell.name();
        int spellLvl = spell.getStat("Level");
        //System.out.println(spellLvl);
        
        if (spell==null) throw new Error("Can't create wand spell: "+spellName);
        
        t.set("Image",images[type]);
        if (spellLvl > 9) {
        	// add adj if the spell is high enough level
        	t.set("UName",wandAdj[adj]+" "+wandtypes[type]+" wand");
        } else {
        	t.set("UName",wandtypes[type]+" wand");
        }
        t.set("Name","wand of "+spellName);
        t.set("NamePlural","wands of "+spellName);
        t.set("WandSpell",spellName);
        t.set("LevelMin",RPG.max(1,spell.getLevel()-4));
        t.set("OnUse",new WandScript());
        t.set("OnDeath",new DestroyWandScript());
        
        Lib.add(t);
    }
    
    public static void init() {
        Thing t = Lib.extend("base wand", "base item");
        t.set("IsWand", 1);
        t.set("IsMagicItem", 1);
        t.set("Image", 288);
        t.set("HPS", 10);
        t.set("ItemWeight", 800);
        t.set("ValueBase",500);
        t.set("Frequency", 40);
        t.set("Charges",3);
        t.set("ASCII","/");
        Lib.add(t);
        
        initRandomWands();
    }
    
    private static void initRandomWands() {
        ArrayList spells=Spell.getSpellNames();
    	
        for(int i=0; i<spells.size(); i++) {
        	Thing s=Spell.create((String)spells.get(i));
        	initWand(s);
        }
    }
}