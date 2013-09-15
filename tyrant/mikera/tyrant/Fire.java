package tyrant.mikera.tyrant;

import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

public class Fire  {
	public static Thing create(int strength) {
		Thing t=Lib.create("fire");
		t.set("AreaDamage",strength);
		return t;
	}
	
	// create fire at specified map location
	// add to existing fire if present
	public static void createFire(BattleMap m, int tx, int ty, int s) {
		if (m == null)
			return;
		Thing f = m.getNamedObject(tx,ty,"fire");
		if (f == null) {
			m.addThing(Fire.create(s), tx, ty);
		} else {
			f.incStat("AreaDamage",s);
		}
	}
        
	public static void init() {
		Thing t=Lib.extend("fire", "base scenery");
        t.set("HPS",15);
		t.set("IsFire",1);
		t.set("IsBlocking",0);
		t.set("IsWarning",1);
		t.set("LevelMin",6);
		t.set("IsDestructible",1);
		t.addHandler("OnAction",Scripts.areaDamage(RPG.DT_FIRE,5,10000,"The fire burns you!","IsPhysical"));
		t.set("RES:normal",30);
		t.set("RES:impact",20);
		t.set("RES:piercing",20);
		t.set("RES:fire",1000);
		t.set("RES:ice",-10);
		t.set("ImageSource","Scenery");
		t.set("Image",180);
		t.addHandler("OnAction",Scripts.decay());
		Lib.add(t);
		
		t=Lib.extend("medium fire","fire");
		t.set("DecayRate",1500);
		Lib.add(t);		
		
		t=Lib.extend("small fire","fire");
        t.set("HPS",5);
		t.set("DecayRate",2000);
		Lib.add(t);
		
	}

}