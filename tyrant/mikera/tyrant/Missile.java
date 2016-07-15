package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
* Implements code for handling missile weapons.
* 
* Includes Missile.init() for missile library initialisation
* 
* Missile weapons can either be throw or in some cases
* used with a bow, crossbow or blowpipe (see RangedWeapon)
*/
public class Missile {

    public static Thing createThrowingWeapon(int level) {
        return Lib.createType("IsThrowingWeapon",level);
    }
    
    public static Thing createMissile(int level) {
        return Lib.createType("IsMissile",level);
    }
    
    public static int shotDifficulty(Thing target, int dist) {
        int difficulty = 2+dist;
        if (target != null)
            difficulty += (target.getStat(RPG.ST_AG)*(dist-1)*(1+target.getStat(Skill.DODGE)))/10;
        return difficulty;
    }
    
    public static Thing getTarget(BattleMap m, int x, int y) {
    	return m.getMobile(x, y);
    }
    
    public static int throwRange(Thing m) {
        int range = m.getStat("ThrowRange");
        // TODO fix this cludge
        if (range<6) range=6;
    	return range;
    }
    
    public static void throwAt(Thing ms, Thing thrower, BattleMap m, int tx, int ty) {
        Thing t = getTarget(m,tx,ty);
        
        thrower.incStat("APS", -throwCost(thrower));
        
        // weapon skill calculation
        int wsk = thrower.getStat(RPG.ST_SK);
        wsk = (wsk * (3 + thrower.getStat(Skill.THROWING))) / 3;
        
        // range
        int range = throwRange(ms);
        
        int rsk = (ms.getStat("RSKMul") * wsk) / 100 + ms.getStat("RSKBonus");
        
        // improvisation modifiers
        if (!ms.getFlag("IsThrowingWeapon")) {
            // we have improvised, so reduce hit chance
            rsk = wsk /= 2;
            range /= 2;
        }
        
        if (t != null) {
            // calculate distance and offset
            int dx = tx - thrower.x;
            int dy = ty - thrower.y;
            int dist = (int) Math.sqrt(dx * dx + dy * dy);
            
            Game.instance().doShot(thrower.x, thrower.y, tx, ty, 100, 80);
            
            boolean dying=false;
            
            int def=shotDifficulty(t, dist);
            
            if (Combat.DEBUG) Game.warn("Throwing (RSK="+rsk+", DEF="+def+")");
            if (RPG.test(rsk,def,thrower,t)) {
                //have scored a hit!
                throwHit(ms,thrower, t);
                
                // drop item if missile survives
                if (ms.getFlag("IsMissile")&&(RPG.r(100) >= ms.getStat("MissileRecovery")))
                    dying=true;
                
            } else {
                t.visibleMessage(ms.getTheName() + " misses "+t.getTheName());
            }
            
            // make it die after adding to map
            if (dying) {
            	ms.die();
            	return;
            }
       
        }
        
        // returning weapons
        int mr=ms.getStat("MissileReturns");
        if ((mr>0)&&RPG.test(mr*wsk, 100)) {
            if (thrower.isHero()) Game.messageTyrant(ms.getTheName() + " returns!");
            if ((ms.getFlag("IsBlessed"))||RPG.test(wsk*(1+thrower.getStat(Skill.SLEIGHT)), 10)) {
                thrower.message("You catch " + ms.getTheName());
                thrower.addThingWithStacking(ms);
                return;
            }
            if (thrower.isHero()) Game.messageTyrant(ms.getTheName() + " lands near your feet");
            tx = thrower.x;
            ty = thrower.y;
            ms.moveTo(m, tx, ty);
            ms.displace();
            return;
        }
        
        if (m.isTileBlocked(tx,ty)) {
        	tx-=RPG.sign(tx-thrower.x);
        	ty-=RPG.sign(ty-thrower.y);
        }
        
        // item lands
        ms.moveTo(m, tx, ty);
        
        // impact damage
        if (ms.getWeight()>100) Damage.inflict(ms,1,RPG.DT_IMPACT);
    }
    
    // thrown item hits
    public static void throwHit(Thing m, Thing thrower, Thing target) {
        // weapon skill calculation
        double rst = (m.getStat("RSTMul") * (thrower.getStat(RPG.ST_ST))) / 100
        + m.getStat("RSTBonus");
        
        if (!m.getFlag("IsThrowingWeapon")) {
            if (m.getFlag("IsWeapon")) {
            	rst = (m.getStat("ASTMul") * (thrower.getStat(RPG.ST_ST))) / 100
		        + m.getStat("ASTBonus");
            } else {
            	//rst tends towards st for heavy items
            	int wt=m.getWeight();
            	rst=(thrower.getStat(RPG.ST_ST)*wt/(3000.0+wt));
            }
        	
        	// we have improvised, so attack often does minimal damage
            if (!RPG.test(thrower.getStat(Skill.THROWING),2)) {
            	rst=rst/3.0;
            }
        }
        
        // throwing skill bonus
        rst = rst * (1 + 0.3*thrower.getStat(Skill.THROWING));
        
        if (target.isVisible(Game.hero()))
            Game.messageTyrant(m.getTheName() + " hits " + target.getTheName());
        
        int st=RPG.round(rst);
        if (Combat.DEBUG) Game.warn("Throw hit [rst="+st+"]");
        hit(m, thrower, target, st);
    }
    
    /**
     * Calculate APS cost of throwing an item
     */
    public static int throwCost(Thing thrower) {
    	return 800/(8+thrower.getStat(Skill.THROWING));
    }
    
    // calculate hit effect
    public static int hit(Thing m, Thing shooter, Thing target, int rst) {
        // rst calculation including luck
        rst = (int)Math.round(rst*Weapon.slayingModifier(m,target)*(1.0f-RPG.luckRandom(shooter,target)));
    	
        // work out type of damage
        String dt=m.getString("MissileDamageType");
    	if (dt==null) {
    		// TODO something more sophisticated?
    		if (RPG.test(shooter.getStat(Skill.THROWING),1)) {
    			dt=m.getString("WeaponDamageType");
    		} 
    		if (dt==null) dt="impact";
    	}

    	
        // handle weapon hit event
        if (m.handles("OnWeaponHit")) {
        	Event whe=new Event("WeaponHit");
        	whe.set("Shooter",shooter);
        	whe.set("Target",target);
        	whe.set("AST",rst);
        	if (m.handle(whe)) return whe.getStat("Damage");
        	rst=whe.getStat("AST");
        }
        
    	// inflict damage
    	int dam= Damage.inflict(target,rst, dt);

    	// handle damage event if damage is inflicted
        if ((dam>0)&&m.handles("OnWeaponDamage")) {
            Event de=new Event("WeaponDamage");
            de.set("Shooter",shooter);
            de.set("Damage",dam);
            de.set("Target",target);
            m.handle(de);
            dam=m.getStat("Damage");
        }    
        
        Item.touch(m,target);
        
        return dam;
    }
    
    
    public static void setStats(Thing t,int rskm,int rskb, int rstm,int rstb){
        t.set("IsMissile",1);
    	t.set("RSKMul",rskm);
        t.set("RSKBonus",rskb);
        t.set("RSTMul",rstm);
        t.set("RSTBonus",rstb);
    }
    
    public static void init() {
        Thing t = Lib.extend("base missile", "base item");
        t.set("Image", 80);
        t.set("IsMissile", 1);        
        t.set("MissileRecovery", 50);
        t.set("IsMissile", 1);
        t.set("WieldType", RPG.WT_MISSILE);
        t.set("MissileDamageType", RPG.DT_NORMAL);
        t.set("HPS", 4);
        t.set("ItemWeight", 500);
        t.set("Frequency", 40);
        Lib.add(t);
        
        initRocks();
        initArrows();
        initThrowingWeapons();
    }
    
    public static void initRocks() {
        Thing t = Lib.extend("rock", "base missile");
        t.set("IsRock",1);
        t.set("MissileType", "bullet");
        t.set("MissileRecovery", 98);
        t.set("MissileDamageType", "impact");
        t.set("Image", 105);
        t.set("HPS", 40);
        t.set("ItemWeight", 2000);
        t.set("RSKMul", 50);
        t.set("RSTMul", 70);
        t.set("LevelMin", 1);
        t.set("Frequency", 80);
        t.set("ASCII","`");
        Lib.add(t);
 
        t=Lib.extend("large rock", "rock");
        t.set("LevelMin", 5);
        t.set("HPS", 80);
        t.set("ItemWeight", 6000);
        Missile.setStats(t,40,-3,100,-5);
        t.multiplyStat("Frequency",0.5);
        Lib.add(t);
        
        // strange rock used to indicate creation bugs....
        t = Lib.extend("strange rock", "rock");
        t.set("Image", 104);
        t.set("Frequency",0);
        Lib.add(t);

        
        t = Lib.extend("stone", "rock");
        t.set("Image", 105);
        t.set("HPS", 20);
        t.set("RSKMul", 60);
        t.set("RSTMul", 60);
        t.set("ItemWeight", 600);
        t.set("Frequency", 60);
        Lib.add(t);
        
        t = Lib.extend("smooth stone", "rock");
        t.set("IsThrowingWeapon",1);
        t.set("Image", 105);
        t.set("HPS", 20);
        t.set("RSKMul", 80);
        t.set("RSTMul", 60);
        t.set("ItemWeight", 600);
        t.set("LevelMin", 7);
        t.set("Frequency", 20);
        Lib.add(t);
        
        t = Lib.extend("pebble", "rock");
        t.set("Image", 105);
        t.set("HPS", 8);
        t.set("RSKMul", 70);
        t.set("RSTMul", 50);
        t.set("ItemWeight", 200);
        t.set("Frequency", 80);
        Lib.add(t);
    }
    
    public static void initArrows() {
        Thing t;
        
        // note
        // stats for Skill and Strngth are 
        // *relative* to the bow being used
        // e.g. 100,0 = use bow value
        //      200,0 = twice bow value
        //      200,3 = twice bow value plus five
        
        t = Lib.extend("arrow", "base missile");
        t.set("MissileType", "arrow");
        t.set("Image", 80);
        t.set("IsArrow", 1);
        t.set("HPS", 5);
        t.set("MissileRecovery", 70);
        t.set("ItemWeight", 200);
        setStats(t,100,0,100,0);
        t.set("LevelMin", 1);
        t.set("ValueBase",50);
        t.set("Number",6);
        t.set("Frequency", 100);
        t.set("ASCII","/");
        Lib.add(t);
        
        t=Lib.extend("unbreakable arrow","arrow");
        t.set("UName","arrow");
        t.set("IsMagicItem",1);
        t.set("MissileRecovery", 100);
        setStats(t,100,0,100,0);
        t.set("Frequency", 40);
        t.set("LevelMin", 10);
        t.set("HPS", 500);
        Lib.add(t);
        
        t=Lib.extend("charmed arrow","arrow");
        t.set("UName","arrow");
        t.set("IsMagicItem",1);
        t.set("LevelMin", 7);
        t.set("MissileRecovery", 90);
        setStats(t,200,1,100,1);
        t.set("Frequency", 40);
        t.set("HPS", 20);
        Lib.add(t);
        
        t=Lib.extend("fire arrow","arrow");
        t.set("Image", 81);
        t.set("UName","arrow");
        t.set("IsMagicItem",1);
        t.set("LevelMin", 10);
        t.set("MissileRecovery", 0);
        setStats(t,100,1,100,1);
        t.set("Frequency", 30);
        t.set("HPS", 25);
        t.addHandler("OnWeaponHit",Scripts.spellEffect("Target","Fireball",100));
        Lib.add(t);
        
        t=Lib.extend("armour piercing arrow","arrow");
        t.set("UName","arrow");
        t.set("LevelMin", 11);
        t.set("WeaponDamageType","piercing");
        t.set("MissileRecovery", 80);
        setStats(t,90,1,110,1);
        t.set("Frequency", 20);
        t.set("HPS", 8);
        t.set("LevelMin", 11);
        Lib.add(t);
        
        t=Lib.extend("huntsman's arrow","arrow");
        t.set("UName","arrow");
        t.set("MissileRecovery", 70);
        t.set("LevelMin", 12);
        setStats(t,100,0,70,6);
        t.set("Frequency", 60);
        t.set("HPS", 10);
        Lib.add(t);
        
        t = Lib.extend("elven arrow", "arrow");
        t.set("UName","arrow");
        t.set("Image", 81);
        t.set("MissileRecovery", 80);
        t.set("HPS", 8);
        t.set("ItemWeight", 200);
        t.set("LevelMin", 9);
        setStats(t,130,0,100,2);
        t.set("Frequency", 30);
        Lib.add(t);
        
        t = Lib.extend("lesser elven arrow", "arrow");
        t.set("UName","arrow");
        t.set("MissileRecovery", 80);
        t.set("HPS", 8);
        t.set("ItemWeight", 100);
        t.set("LevelMin", 1);
        setStats(t,100,0,100,2);
        t.set("Frequency", 30);
        Lib.add(t);
        
        t = Lib.extend("doom arrow", "arrow");
        t.set("UName","arrow");
        t.set("Image", 81);
        t.set("MissileRecovery", 50);
        t.set("HPS", 18);
        t.set("ItemWeight", 200);
        t.set("LevelMin", 19);
        setStats(t,130,0,130,2);
        t.set("Frequency", 10);
        t.set("OnWeaponDamage",Weapon.damageEffect("curse"));
        Lib.add(t);
        
        t=Lib.extend("slaying arrow","arrow");
        t.set("UName","arrow");
        t.multiplyStat("Frequency",0.1);
        setStats(t,100,0,100,1);
        t.set("MissileRecovery", 75);
        t.set("LevelMin", 10);
        t.set("ValueBase",100);
        Lib.add(t);
        
        t=Lib.extend("goblin-slaying arrow","slaying arrow");
        t.set("SlayingStats","IsGoblin*3");
        t.set("LevelMin", 11);
        Lib.add(t);
        
        t=Lib.extend("orc-slaying arrow","slaying arrow");
        t.set("SlayingStats","IsOrc*3");
        t.set("LevelMin", 13);
        Lib.add(t);
        
        t=Lib.extend("slime-slaying arrow","slaying arrow");
        t.set("SlayingStats","IsSlime*3");
        t.set("LevelMin", 8);
        Lib.add(t);
        
        t=Lib.extend("dragon-slaying arrow","slaying arrow");
        t.set("SlayingStats","IsDragon*3");
        t.set("LevelMin", 30);
        Lib.add(t);
        
        t = Lib.extend("goblin arrow", "arrow");
        t.set("Image", 81);
        t.set("MissileRecovery", 80);
        t.set("HPS", 2);
        t.set("LevelMin", 4);
        t.set("ItemWeight", 240);
        setStats(t,80,4,90,4);
        t.set("Frequency", 60);
        Lib.add(t);
        
        t = Lib.extend("poisoned goblin arrow", "goblin arrow");
        t.set("UName","goblin arrow");
        t.set("Image", 82);
        t.set("MissileRecovery", 80);
        t.set("HPS", 3);
        t.set("LevelMin", 9);
        t.set("ItemWeight", 240);
        setStats(t,80,5,90,3);
        t.set("Frequency", 30);
        t.set("OnWeaponDamage",Weapon.damageEffect("poison"));
        
        Lib.add(t);        
    }
    
    public static void initThrowingWeapons() {
        Thing t = Lib.extend("base throwing weapon", "base missile");
        t.set("MissileType", "thrown");
        t.set("IsThrowingWeapon", 1);
        t.set("LevelMin", 4);
        t.set("MissileRecovery", 80);
        t.set("Image", 100);
        t.set("HPS", 15);
        t.set("ItemWeight", 1000);
        t.set("ValueBase",200);
        setStats(t,50,0,100,0);
        t.set("Frequency", 100);
        Lib.add(t);
        
        initDarts();
        initThrowingKnives();
        initThrowingStars();
    }
    
    public static void initThrowingStars() {
        Thing t;
        
    	t = Lib.extend("base throwing star", "base throwing weapon");
        t.set("Image", 101);
        t.set("LevelMin",6);
        t.set("ItemWeight",1000);
        t.set("MissileRecovery",99);
        t.set("Frequency",10);
        t.set("ValueBase",400);
        t.set("Number",1);
        setStats(t,40,0,80,0);
        Lib.add(t);
        
        t=Lib.extend("throwing star","base throwing star");
        t.set("LevelMin",6);
        Lib.add(t);
        
        t=Lib.extend("poison star","base throwing star");
        t.set("UName","dark throwing star");
        t.set("OnWeaponDamage",Weapon.damageEffect("poison"));
        t.set("LevelMin",9);
        Lib.add(t);
        
        t=Lib.extend("death star","base throwing star");
        t.set("UName","dark throwing star");
        setStats(t,140,0,180,0);
        t.set("LevelMin",19);
        Lib.add(t);
    }
    
    public static void initThrowingKnives() {
        Thing t;
        
    	t = Lib.extend("base throwing knife", "base throwing weapon");
        t.set("Image", 106);
        t.set("LevelMin",1);
        t.set("ItemWeight",1000);
        t.set("MissileRecovery",98);
        t.set("Frequency",50);
        t.set("ValueBase",250);
        t.set("Number",3);
        setStats(t,50,0,50,0);
        Weapon.setStats(t,50,0,50,0,20,0);
        Lib.add(t);
        
        t=Lib.extend("throwing knife","base throwing knife");
        t.set("LevelMin",4);
        Lib.add(t);
        
        t=Lib.extend("curved stick","base throwing weapon");
        t.set("Image", 100);
        t.set("LevelMin",6);
        setStats(t,50,0,70,0);
        t.set("MissileRecovery",90);
        t.set("Number",1);
        t.set("MissileReturns",20);
        Lib.add(t);
        
        t=Lib.extend("magic curved stick","curved stick");
        t.set("UName","decorated curved stick");
        t.set("LevelMin",13);
        setStats(t,100,0,100,0);
        t.set("MissileRecovery",97);
        t.set("MissileReturns",100);
        Lib.add(t);

        
        t=Lib.extend("golden throwing knife","base throwing knife");
        t.set("Number",2);
        setStats(t,80,0,80,0);
        t.set("LevelMin",10);
        Lib.add(t);
    	
        t=Lib.extend("knife of returning","base throwing knife");
        t.set("UName","shining throwing knife");
        setStats(t,80,0,60,0);
        t.set("MissileReturns",100);
        t.set("LevelMin",7);
        Lib.add(t);
    	
    }

    public static void initDarts() {
        Thing t;
        
    	t = Lib.extend("base dart", "base throwing weapon");
        t.set("Image", 88);
        t.set("HPS", 15);
        t.set("LevelMin",1);
        t.set("ItemWeight",300);
        t.set("MissileRecovery",85);
        t.set("Frequency",50);
        t.set("ValueBase",150);
        t.set("Number",5);
        setStats(t,70,3,30,3);  
        Lib.add(t);
        
    	t = Lib.extend("dart", "base dart");
        t.set("Image", 88);
        t.set("HPS", 15);
        t.set("LevelMin",4);
        setStats(t,70,3,30,3);
        Lib.add(t);

        t = Lib.extend("poison dart", "base dart");
        t.set("Image", 89);
        t.set("HPS", 10);
        t.set("LevelMin",8);
        t.set("OnWeaponDamage",Weapon.damageEffect("poison"));
        setStats(t,70,3,30,3);
        Lib.add(t);
        
    	t = Lib.extend("runic dart", "base dart");
        t.set("Image", 91);
        t.set("MissileRecovery",60);
        t.set("Frequency",20);
        t.set("HPS", 35);
        t.set("LevelMin",14);
        t.addHandler("OnWeaponHit",Scripts.spellEffect("Target","Fireball",100));
        setStats(t,120,0,40,10);
        Lib.add(t);
    }
}