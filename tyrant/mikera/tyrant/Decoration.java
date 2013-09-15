package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Thing;

/**
 * Decorations for dungeons
 * 
 * Generally no real game effects, but look nice :-)
 */ 
public class Decoration  {
   
    public static Thing createDecoration(int n) {
        switch (n) {
            case 0 :
                return Lib.create("blood pool");
            case 1 :
                return Lib.create("slime pool");
        }
        
        return null;
    }
    
    public static void init() {
        Thing t;
        
        t=Lib.extend("base decoration", "base thing");
        t.set("ImageSource", "Scenery");
        t.set("IsPhysical",1);
        t.set("IsDestructible",1);
        t.set("IsDecoration",1);
        t.set("IsTransparent",1);
        t.set("HPS",1);
        t.set("Z",Thing.Z_ONFLOOR);
        Lib.add(t);
        
        t=Lib.extend("base pool", "base decoration");
        t.set("Image", 105);
        t.set("LevelMin", 3);
        t.set("NoStack", 1);
        t.set("ASCII","0");
        Lib.add(t);
        
        t=Lib.extend("blood pool", "base pool");
        t.set("IsActive",1);
        t.set("Image", 100);
        t.set("LevelMin", 7);
        t.set("LifeTime",20000);
        t.addHandler("OnAction",Scripts.generator("fly swarm",5));
        t.addHandler("OnAction",Scripts.decay());
        Lib.add(t);
        
        t=Lib.extend("water pool", "base pool");
        t.set("IsActive",1);
        t.set("Image", 105);
        t.set("LevelMin", 1);
        t.set("LifeTime",30000);
        t.addHandler("OnAction",Scripts.decay());
        Lib.add(t);
        
        t=Lib.extend("slime pool", "base pool");
        t.set("IsActive",1);
        t.set("LevelMin", 1);
        t.set("Image", 104);
        t.set("LifeTime",43000);
        t.addHandler("OnAction",Scripts.decay());
        Lib.add(t);
    }
}