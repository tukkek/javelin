//
// A chest, possibly trapped, possibly containing something useful
//

package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

public class Chest {
    // create a chest
    // value determines usefulness of contents....
    // ....and also potential danger of traps involved
    public static Thing create(int l) {
        Thing t=Lib.createType("IsChest",l);
        
        return t;
    }
    
	public static class ChestDamage extends Script {
		private static final long serialVersionUID = 4121411791663740466L;

        public boolean handle(Thing t, Event e) {
			Thing[] ts=t.getFlaggedContents("IsTrap");
			BattleMap m=t.getMap();
			
			Thing a=e.getThing("Actor");
			
			for (int i=0; i<ts.length; i++) {
				//Game.warn(ts[i].name()+" triggered on chest");
				Point p=new Point(t.x,t.y);
			
				if (a!=null) {
					int nx=t.x+RPG.sign(a.x-t.x);
					int ny=t.y+RPG.sign(a.y-t.y);
					
					p.x=nx;
					p.y=ny;
				}
				
				m.addThing(ts[i],p.x,p.y);
				Trap.trigger(ts[i]);
			}
			return false;
		}
		
	}
    
    public static class ChestCreation extends Script {
        private static final long serialVersionUID = 3256723974577598773L;

        public boolean handle(Thing t, Event e) {
            int l=e.getStat("Level");
            for (int i=RPG.min(1,RPG.d(2,4)-3); i>0; i--) {
                t.addThing(Lib.createItem(l+RPG.r(5)));
            }
            if (RPG.test(l,10)) t.addThing(Trap.createTrap(l));
            return false;
        }
    }
    
    public static class ChestClosed extends Script {
        private static final long serialVersionUID = 3256718494266373688L;

        public boolean handle(Thing t, Event e) {
            // TODO
            return false;
        }
    }
    
    public static class ChestOpen extends Script {
        private static final long serialVersionUID = 4120851040649491762L;

        public boolean handle(Thing t, Event e) {
            Thing[] ts=t.getItems();
            Score.scoreExplore(t);
            for (int i=0; i<ts.length; i++) {
                if (ts[i].getFlag("IsItem")) {
                    t.getMap().addThing(ts[i],t.x,t.y);
                }
            }
            return false;
        }
    }
    
    
    public static class ChestBump extends Script {
        private static final long serialVersionUID = 4120851040649491762L;

        public boolean handle(Thing t, Event e) {
            Thing tt=e.getThing("Target");
            if (tt.isHero()) {
            	Item.touch(tt,t);
            	Game.messageTyrant("You open "+t.getTheName());
            	Door.setOpen(t,true);
            }
            return false;
        }
    }
    
    public static void init() {
        Thing t;
        
        t=Lib.extend("base chest","base scenery");
        t.set("IsOpenable",1);
        t.set("IsChest",1);
        t.set("ImageSource","Scenery");
        t.set("Image",121);
        t.set("HPS",30);
        t.set("Frequency",30);
        t.set("ItemWeight",10000);
        t.set("LevelMin",1);
        t.set("Z",Thing.Z_ITEM-5);
        t.set("ImageOpen",2);
        t.addHandler("OnDamage",new ChestDamage());
        t.set("DefaultThings","80% [IsTrap]");
        t.set("OnBump",new ChestBump());
        Lib.add(t);
        
        t=Lib.extend("chest","base chest");
        t.set("OnCreate",new ChestCreation());
        t.set("OnOpen",new ChestOpen());
        t.set("ScoreExplore",10);
        Lib.add(t);
    }
}