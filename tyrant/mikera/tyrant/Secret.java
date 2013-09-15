// This represents a secret/hidden map element
// Does not appear unless discovered

package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

public class Secret {
    
    
    
    public static void searchAround() {
        Thing h=Game.hero();
        BattleMap map = h.getMap();
        if (map == null) {
            Game.warn("Hero not on map in Secret.searchAround!");
            return;
        }
        
        
        if (h.getFlag("IsBlind")) {
        	Game.messageTyrant("You fumble around ineffectively");
        	return;
        }
        
        boolean searchFlag = search();
        
        if(!searchFlag)
        	Game.messageTyrant("You search but find nothing of interest");
    }
    
    public static boolean search() {
    	Thing h=Game.hero();
 
        boolean searchFlag = false;
        Thing[] th = h.getMap().getThings(h.x - 1, h.y - 1, h.x + 1, h.y + 1);
        for (int i = 0; i < th.length; i++) {
            Thing st = th[i];
            // use the OR operator ...
            // so if something is found, the flag
            // will stay true.
            searchFlag |= search(st);
        }
        
    	return searchFlag;
    }
    
    public static boolean search(Thing t) {
    //public static void search(Thing t) {
        BattleMap map = t.getMap();
        
        if (!t.getFlag("IsSecret")) return false;
        
        if (t.getFlag("IsSecretDoor")) {
            Game.messageTyrant("You have found a secret door!");
            map.setTile(t.x, t.y, map.floor());
            String s=t.getString("SecretDoorType");
            Thing door;
            if (s==null) {
            	door=Door.createDoor(map.getLevel());
            } else {
            	door=Lib.create(s);
            }
            Score.scoreSecretDoor(map.getLevel());
            map.addThing(door, t.x, t.y);
            map.setVisible(t.x,t.y);
            t.remove();
            return true;
        }
        
        if (t.getFlag("IsTrap")&&(t.getFlag("IsInvisible"))) {
            Game.messageTyrant("You have found a "+t.name()+"!!");
        	t.set("IsDiscovered",1);
        	t.set("IsInvisible",0);
        	return true;
        }
        
        if (t.getFlag("IsSecretPassage")) {
            Game.messageTyrant("You have found a hidden passage!");
            map.setTile(t.x, t.y, map.floor());
            t.remove();
            return true;
        }
        
        if (t.getFlag("IsHiddenItem")) {
            Thing it=(Thing)t.get("HiddenThing");
            if (it==null) {
                String s=t.getString("HiddenItem");
                if (s==null) it=Lib.createItem(map.getLevel());
                else it=Lib.create(s);
            }
            Game.messageTyrant("You have discovered "+it.getAName()+"!");
            map.addThing(it,t.x,t.y);
            t.remove();
            return true;
        }
        return false;
    }
    
    public static Thing hide(Thing t) {
        Thing s=Lib.create("secret item");
        s.set("HiddenThing",t);
        t.remove();
        s.addThing(t);
        return s;
    }
    
    public static void init() {
        Thing t;
        
        t=Lib.extend("base secret","base special");
        t.set("IsPhysical",0);
        t.set("IsDestructible",0);
        t.set("Image",5);
        t.set("IsSecret",1);
        t.set("ASCII", "o");
        Lib.add(t);
        
        t=Lib.extend("secret door","base secret");
        t.set("IsSecretDoor",1);
        Lib.add(t);
        
        t=Lib.extend("secret passage","base secret");
        t.set("IsSecretPassage",1);
        Lib.add(t);
        
        t=Lib.extend("secret item","base secret");
        t.set("IsHiddenItem",1);
        Lib.add(t);
        
    }
    
}