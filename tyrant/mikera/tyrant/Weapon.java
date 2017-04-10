package tyrant.mikera.tyrant;

import java.util.*;

import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;



public class Weapon {
	
	private static final double WEAPON_DAMAGE_MULTIPLIER=1.0;
	
    // use to hit target
    public static int attack(Thing w, Thing wielder, Thing target) {
        int attack = calcASK(w, wielder, target);
        int defence = calcDSK(target);
        
        // perform skill test
        // include wileder and target to calulate Luck
        int result = RPG.test(attack, defence,wielder,target) ? 1 : 0;
        
        if (Combat.DEBUG&&(wielder.isHero()||target.isHero())) {
        	Game.warn("Attack ("+w.name()+"): "+attack+" vs. "+defence+": "+((result>0)?"hit":"miss"));
        	
        }
        
        // displayresult message
        if (target.isVisible(Game.hero())) {
	        if (result == 0) {
	            // miss
	            Game.messageTyrant(Text.capitalise(wielder.getTheName()
	            + (wielder.isHero() ? " miss " : " misses ") + target.getTheName()));
	        } else {
	        	// hit
	        	// message displayed later in hit(...)	 
	        }
        }
        return result;
    }
    
    public static double slayingModifier(Thing w, Thing t) {
    	if (t==null) return 1.0;
    	double a=1.0;
    	String mods=w.getstring("SlayingStats");
    	
    	if (mods!=null) {
    		String[] ms=mods.split(",");
    		for (int i=0; i<ms.length; i++) {
    			String m=ms[i];
    			if (ms.length<=0) continue;
    			
    			int p=m.indexOf("*");
    			if (p<=0) throw new Error("Invalid damage modifier ["+m+"]");
    			String crit=m.substring(0,p).trim();
    			if (t.getFlag(crit)) {
    				a*=Double.parseDouble(m.substring(p+1,m.length()));
    			}
    		}
    	}
    	return a;
    }
    
    public static int hit(Thing w, Thing wielder, Thing target) {
        // get base attack strength
        int ast = calcAST(w, wielder, target);
        
        // calculate effective hit strength 
        // includes Luck
        double hitStrength=(1.0-RPG.luckRandom(wielder,target));
        //if (Game.cdebug) {
        //	Game.warn("Hit Strength: "+hitStrength);
        //}
        
        
        // handle wielder hit event for really special effects
        if (wielder.handles("OnHit")) {
        	Event he=new Event("Hit");
        	he.set("Wielder",wielder);
        	he.set("Weapon",w);
        	he.set("Target",target);
        	he.set("AST",ast);
        	if (wielder.handle(he)) return he.getStat("Damage");
        }
        
        // handle weapon hit event
        if (w.handles("OnWeaponHit")) {
        	Event whe=new Event("WeaponHit");
        	whe.set("Wielder",wielder);
        	whe.set("Target",target);
        	whe.set("AST",ast);
        	if (w.handle(whe)) return whe.getStat("Damage");
        	
        	// allow damage increase
        	ast=whe.getStat("AST"); 
        }

        // multiply hit strength by ast to get base damage
        int dam = RPG.round(ast*hitStrength*WEAPON_DAMAGE_MULTIPLIER);
        
        Game.instance().pushMessages();
        
        // inflict damage
        String damageType=w.getstring("WeaponDamageType");
        if (damageType==null) {
        	throw new Error(w.name()+" has no WeaponDamageType!");
        }
        
        if (Combat.DEBUG) {
        	Game.warn("- AST: "+ast+" vs. ARM: "+Armour.calcArmour(target,damageType));
        }
        
        // get names before attack is conducted
        String wielderName=wielder.getTheName();
        String targetName=target.getTheName();
        
        // inflict damage
        dam = Damage.inflict(target, dam, damageType);
        
        // extra damage from weapon
        int extra=getExtraAST(w,wielder);
        if (extra>0) {
            //Game.warn("Extra AST "+extra);
            extra= Damage.inflict(target, extra, w.getstring("ExtraDamageType"));
            //Game.warn("Extra damage "+extra);
            dam+=extra;
        }
        
        // use the touch effect
        Item.touch( (w.getFlag("IsAttack"))?wielder:w , target);
        
        String ds = "";
        if (Game.isDebug()) {
            // ds = "(AST=" + ast + ", DAM=" + dam + ") ";
        }
        
        String rs;
        if (dam>0) {
        	rs="causing "+Damage.describeState(target)+" damage";
        } else {
        	rs="but fail"+(wielder.isHero()?"":"s") +" to do any damage";
        }
        
        ArrayList al=Game.instance().popMessages();
        
        Thing h=Game.hero();
        boolean isvisible = wielder.isVisible(h)||target.isVisible(h);
		if ((wielder==h)||(target==h)) isvisible=true;
        
        // report the attack
        if (isvisible) {
        	if (target.isDead()&& target.getFlag("IsBeing")) {
        		Game.messageTyrant(Text.capitalise(wielderName
    					+ (wielder.isHero() ? " have slain " : " has slain ")
    					+ targetName));
        	} else {
	        	Game.messageTyrant(Text.capitalise(ds
	        
	            + wielderName
	            + " "
				+ hitVerb(wielder,w)
				+ " "
	            + targetName
	            + " "
				+ rs
				));
        	}
        }
        
        // now report the resulting messages
        Game.message(al);
        
        if ((dam>0)&&w.handles("OnWeaponDamage")) {
            Event de=new Event("WeaponDamage");
            de.set("Wielder",wielder);
            de.set("Damage",dam);
            de.set("Target",target);
            w.handle(de);
            dam=w.getStat("Damage");
        }
        
        // slight wear and tear to weapon
        // lower hit damage = more armour = higher damage to weapon
        //   note: (ast-dam) is the number of hps deflected by armour
        // damage((ast - dam) / 3, RPG.DT_NORMAL);
        
        return dam;
    }
    
    private static String hitVerb(Thing b, Thing w) {
    	String v=w.getstring("HitVerb");
    	if (v==null) {
    		Game.warn("no hit verb for "+w.name());
    		v=(b.isHero() ? "hit" : "hits");
    	} else {
    		String[] vs=v.split("/");
    		v=(b.isHero()?vs[0]:vs[1]);
    	}
    	return v;
    }
    
    // Get weapon attack skill
    public static int getDSK(Thing w, Thing wielder) {
        int DSK = wielder.getStat(RPG.ST_SK);
        
        // calculate multiplier (percent)
        double mul=(0.7+0.3*wielder.getStat(Skill.DEFENCE))*w.getStat(RPG.ST_DSKMULTIPLIER);
        
        // unarmed bonus
        if (w.getFlag("IsUnarmedWeapon")) {
        	mul*=1.0+0.2*wielder.getStat(Skill.UNARMED);
        }
        
        // status modifiers
        if (w.getFlag("IsCursed")) mul*=0.6;
        if (w.getFlag("IsBlessed")) mul*=1.2;
        
        // modify and return DSK
        DSK = (int)(DSK*mul / 100.0);
        DSK = DSK + w.getStat(RPG.ST_DSKBONUS);
        return DSK;    	
    }
    
    // Get weapon attack skill
    public static int calcASK(Thing w, Thing wielder, Thing target) {
        double ASK = getASK(w,wielder);
        
        // encumberance
        int enc=wielder.getStat(RPG.ST_ENCUMBERANCE);
        if (enc>0) {
        	enc=RPG.min(100,enc);
        	ASK=(ASK*(100-enc))/100;
        }
        
        ASK*=slayingModifier(w,target);
        
        return RPG.max(1,(int)Math.round(ASK));
    }
    
    public static int getASK(Thing w, Thing wielder) {
        int ASK = wielder.getStat(RPG.ST_SK);
        
        // calculate multiplier (percent)
        double mul=(1+0.3*wielder.getStat(Skill.ATTACK))*w.getStat(RPG.ST_ASKMULTIPLIER);
        
        // unarmed bonus
        if (w.getFlag("IsUnarmedWeapon")) {
        	mul*=1.0+0.3*wielder.getStat(Skill.UNARMED);
        }
        
        // status modifiers
        if (w.getFlag("IsCursed")) mul*=0.8;
        if (w.getFlag("IsBlessed")) mul*=1.2;
        
        // modify and return ASK
        ASK = (int)(ASK*mul / 100.0);
        ASK = ASK + w.getStat(RPG.ST_ASKBONUS);
        return ASK;
	}
    
    // Get weapon attack strength
    public static int calcAST(Thing w, Thing wielder, Thing target) {
        double ast=getAST(w,wielder)*slayingModifier(w,target);
        if (ast<=0.0) return 0;
        return RPG.max(1,(int)ast);
    }
    
    public static int getAST(Thing w, Thing wielder){
        double AST = wielder.getStat(RPG.ST_ST);
        
        // calculate multiplier (percent)
        double mul=(1+0.10*wielder.getStat(Skill.ATTACK)) * w.getStat(RPG.ST_ASTMULTIPLIER);
        
        // unarmed bonus
        if (w.getFlag("IsUnarmedWeapon")) {
        	mul*=1.0+0.10*wielder.getStat(Skill.UNARMED);
        }
        
        if (w.getFlag("IsCursed")) mul*=0.8;
        if (w.getFlag("IsBlessed")) mul*=1.2;
        
        // modify and return AST
        AST = AST * mul / 100.0;
        AST = AST + w.getStat(RPG.ST_ASTBONUS);
        return (int)(AST);    	
    }

    // Get weapon attack strength
    public static int getExtraAST(Thing w, Thing wielder) {
        int AST = wielder.getStat(RPG.ST_ST);
        AST = (AST * w.getStat("ExtraASTMultiplier")) / 100;
        AST = AST + w.getStat("ExtraASTBonus");
        return AST;
    }
        
    public static int calcDSK(Thing t) {
        // counter for defence skill
        double dsk=0;
        
        // add defence bonuses for weapons
    	Thing[] ws=t.getInventory();
    	for (int i=0; i<ws.length; i++) {
    		Thing w=ws[i];
    		if ((w!=null)&&(w.y>0)) {
    			dsk+=getDSK(w,t);
    		}
    	}
        
    	// bonus for agility and dodge skill
    	dsk+=t.getStat(RPG.ST_AG)*(0.2+0.5*t.getStat(Skill.DODGE));
        
    	// bonus for skill in unarmed combat
    	dsk+=t.getStat(RPG.ST_SK)*t.getStat(Skill.UNARMED)*0.2;
    	
        // encumberance
        int enc=t.getStat(RPG.ST_ENCUMBERANCE);
        if (enc>0) {
        	dsk=(dsk*(100-enc)/100.0);
        }
    	
    	// penalty if more foes than bravery can counter
    	BattleMap m=t.getMap();
    	if (m!=null) {
    		int foes=0;
    		Thing[] ts=m.getObjects(t.x-1,t.y-1,t.x+1,t.y+1,"IsBeing");
    		for (int i=0; i<ts.length; i++) {
    			if (ts[i].isHostile(t)) foes++;
    		}
    		foes-=t.getStat(Skill.BRAVERY);
    		if (foes>1) dsk=dsk/foes;
    	}
    	
        return RPG.max(0,(int)Math.round(dsk));
    }
    
    public static Thing createWeapon(int level) {
        return Lib.createWeapon(level);
    }
    
    public static void init() {
        Thing t = Lib.extend("base weapon", "base item");
        t.set("Image", 2);
        t.set("IsWeapon", 1);
        t.set("WieldType", RPG.WT_MAINHAND);
        t.set("WeaponDamageType", "normal");
        t.set("HPS", 40);
        t.set("ValueBase", 200);
        t.set("ItemWeight", 6000);
        t.set("Frequency", 50);
        t.set("AttackCost", 100);
        t.set("DamageLevels",1);
        t.set("RES:acid",-15);
        t.set("LevelMin",1);
        t.set("HitVerb","hit/hits");
        t.set("ASCII","(");
        Lib.add(t);
        
        initCrudeWeapons();
        initSpears();
        initStandardWeapons();
        initSpecialWeapons();
        initAttacks();
        initAlterations();
        
    }
    
    public static String statString(Thing w) {
		int ask=w.getStat(RPG.ST_ASKMULTIPLIER)/10;
		int ast=w.getStat(RPG.ST_ASTMULTIPLIER)/10;
		int dsk=w.getStat(RPG.ST_DSKMULTIPLIER)/10;
		return "(+"+ask+",+"+ast+") ["+dsk+"]";    	
    }
    
    private static void initSpears() {
    	Thing t;
    	
        t = Lib.extend("spear", "base weapon");
        t.set("MissileType", "thrown");
        t.set("IsThrowingWeapon", 1);
        t.set("MissileRecovery", 95);
        t.set("Image",21);
        t.set("LevelMin",1);
        t.set("ItemWeight", 5000);
        t.set("WeaponDamageType", "piercing");
        t.set("Frequency",50);
        Weapon.setStats(t,60,0,70,1,50,1);
        Missile.setStats(t,60,0,8,3);
        addWeapon(t);
    	
    }
    
    public static void setStats(Thing t, int askm, int askb, int astm, int astb, int dskm, int dskb) {
        t.set(RPG.ST_ASKMULTIPLIER,askm);
        t.set(RPG.ST_ASKBONUS,askb);
        t.set(RPG.ST_ASTMULTIPLIER,astm);
        t.set(RPG.ST_ASTBONUS,astb);
        t.set(RPG.ST_DSKMULTIPLIER,dskm);
        t.set(RPG.ST_DSKBONUS,dskb);
    }
    
    
    // weapon modifiers for each primary material
    // iron is "standard"
    private static String[] wmat={"stone", "iron","steel","silver",   "elven steel", "mithril", "krithium","black steel","blue steel", "red steel", "crystal", "parillite", "sapphire",         "emerald"};
    private static String[] umat={"heavy", null,  null,   "silvery",  "shining",     "silvery", "heavy",  "dark",       "silvery",     null,        "shining", "shining",   "shimmering blue",  "shimmering green"};
    private static int[] levels= {2  ,     3  ,   7 ,     9,          12,            20,        16,        21,           15,           18,          60,        27,          24,                 30};
    private static int[] weights={140,     100,   90,     80,         70,            50,        160,       130,          80,           100,         60,        40,          65,                 60};
    private static int[] freqs=  {40 ,     100,   70,     20,         50,            20,        40,        50,           50,           60,          40,        30,          20,                 10};
    private static int[] skills= {80,      100,   105,    100,        120,           140,       80,        100,          150,          110,         140,       170,         190,                210};
    private static int[] strs=   {90,      100,   110,    90,         120,           130,       140,       170,          110,          150,         140,       200,         160,                210};
    	
    // add variants of different metal types
    public static void addWithVariants(Thing w) {
        
        for (int i=0; i<wmat.length; i++) {
            Thing t=(Thing)w.clone();
            String name=w.getstring("Name");
            t.set("Name",wmat[i]+" "+name);
            t.set("Material",wmat[i]);
            t.set("UName", (umat[i]==null) ? name : umat[i]+" "+name);
            t.multiplyStat("ItemWeight",weights[i]/100.0);
            t.set("LevelMin",RPG.max(1,levels[i]+w.getStat("LevelMin")));
            t.multiplyStat("Frequency",freqs[i]/100.0);
            t.multiplyStat("AttackCost",Math.sqrt(weights[i]/100.0));
            t.multiplyStat(RPG.ST_ASKMULTIPLIER,skills[i]/100.0);
            t.multiplyStat(RPG.ST_ASKBONUS,skills[i]/100.0);
            t.multiplyStat(RPG.ST_DSKMULTIPLIER,skills[i]/100.0);
            t.multiplyStat(RPG.ST_DSKBONUS,skills[i]/100.0);
            t.multiplyStat(RPG.ST_ASTMULTIPLIER,strs[i]/100.0);
            t.multiplyStat(RPG.ST_ASTBONUS,strs[i]/100.0);
            t.multiplyStat(RPG.ST_HPS,Math.pow(80,strs[i]/100.0-1.0));
            addWeapon(t);
        } 
    }
    
    /**
     * Adds a weapon to the library
     * Use this method to do any final weapon modifications
     * 
     * @param town The weapon to be added
     */
    private static void addWeapon(Thing w) {
    	w.set("LevelMax",w.getStat("LevelMin")*3/2+5);
    	Lib.add(w);
    }
    
    public static void initStandardWeapons() {
        Thing t;
        
        t = Lib.extend("base standard weapon", "base weapon");
        t.set("DefaultThing","3% [IsWeaponRune],3% [IsWeaponRune]");
        t.set("ValueBase",200);
        Lib.add(t);
        
        t = Lib.extend("base sword", "base standard weapon");
        t.set("IsSword",1);
        t.set("ValueBase",300);
        t.set("HitVerb","slash/slashes");
        Lib.add(t);
        
        t = Lib.extend("base dagger", "base sword");
        t.set("IsSword",0);
        t.set("IsDagger",1);
        t.set("ValueBase",100);
        t.set("HitVerb","stab/stabs");
        Lib.add(t);
        
        t = Lib.extend("longsword", "base sword");
        t.set("Image",3);
        t.set("LevelMin",7);
        t.set("Frequency",100);
        t.set("ItemWeight", 8000);
        setStats(t,80,0,80,0,40,0);
        t.set("ValueBase",400);
        addWithVariants(t);
        
        t = Lib.extend("broadsword", "base sword");
        t.set("Image",3);
        t.set("LevelMin",6);
        t.set("Frequency",60);
        t.multiplyStat("AttackCost",1.2);
        t.set("ItemWeight", 10000);
        t.set("ValueBase",400);
        setStats(t,60,0,100,0,40,0);
        
        addWithVariants(t);
        
        t = Lib.extend("scimitar", "base sword");
        t.set("Image",61);
        t.set("LevelMin",6);
        t.set("Frequency",50);
        t.set("ItemWeight", 7000);
        setStats(t,80,1,90,1,30,1);
        addWithVariants(t);
        
        t = Lib.extend("sword", "base sword");
        t.set("Image",3);
        t.set("LevelMin",5);
        setStats(t,70,0,70,1,40,0);
        t.set("ItemWeight", 6000);
        t.set("Frequency",100);
        addWithVariants(t);
        
        t = Lib.extend("short sword", "base sword");
        t.set("Image",5);
        t.set("LevelMin",4);
        setStats(t,60,1,65,1,35,1);
        t.set("ItemWeight", 4000);
        t.set("Frequency",100);
        addWithVariants(t);
        
        t = Lib.extend("dagger", "base dagger");
        t.set("Image",2);
        t.set("LevelMin",2);
        t.set("Frequency",100);
        t.set("ItemWeight", 2500);
        t.set("AttackCost", 75);
        setStats(t,50,0,60,1,30,0);
        t.set("ValueBase",200);
        addWithVariants(t);
        
        t = Lib.extend("knife", "base dagger");
        t.set("Image",0);
        t.set("LevelMin",0);
        t.set("Frequency",100);
        t.set("ItemWeight", 1500);
        t.set("AttackCost", 60);
        setStats(t,50,0,50,1,20,1);
        t.set("ValueBase",100);
        addWithVariants(t);
        
        t = Lib.extend("two-handed sword", "base sword");
        t.set("Image",4);
        t.set("LevelMin",7);
        t.set("Frequency",30);
        t.set("ItemWeight", 12000);
        setStats(t,100,0,110,0,60,0);
        t.set("WieldType",RPG.WT_TWOHANDS);
        addWithVariants(t);
        
        
        //// war hammers
        
        t = Lib.extend("base hammer", "base standard weapon");
        t.set("IsHammer",1);
        t.set("WeaponDamageType", "impact");
        t.multiplyStat("AttackCost",1.2);
        t.set("HitVerb","bash/bashes");
        Lib.add(t);
        
        
        t = Lib.extend("hammer", "base hammer");
        t.set("Image",9);
        t.set("LevelMin",3);
        t.set("Frequency",50);
        t.set("ItemWeight", 8000);
        setStats(t,45,0,70,1,20,1);
        addWithVariants(t);
        
        t = Lib.extend("warhammer", "base hammer");
        t.set("Image",10);
        t.set("LevelMin",5);
        t.set("Frequency",50);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("ItemWeight", 16000);
        setStats(t,60,0,150,5,5,-4);
        addWithVariants(t);
        
        // axes
        
        t = Lib.extend("base axe", "base standard weapon");
        t.set("IsAxe",1);
        t.set("Frequency",50);
        t.set("HitVerb","chop/chops");
        Lib.add(t);
        
        t = Lib.extend("battle axe", "base axe");
        t.set("Image",11);
        t.set("LevelMin",6);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("ItemWeight", 14000);
        setStats(t,70,0,140,5,20,-2);
        addWithVariants(t);
        
        t = Lib.extend("hand axe", "base axe");
        t.set("Image",12);
        t.set("LevelMin",3);
        t.set("ItemWeight", 7000);
        setStats(t,45,0,75,2,20,0);
        addWithVariants(t);
        
        /// polearms
        
        t = Lib.extend("base polearm", "base standard weapon");
        t.set("IsPolearm",1);
        Lib.add(t);
        
        t = Lib.extend("trident", "base polearm");
        t.set("Image",23);
        t.set("IsTrident",1);
        t.set("LevelMin",5);
        t.set("ItemWeight", 6000);
        t.set("Frequency",30);
        setStats(t,50,0,70,4,60,3);
        addWithVariants(t);
        
        t = Lib.extend("halberd", "base polearm");
        t.set("Image",27);
        t.set("LevelMin",6);
        t.set("ItemWeight", 12000);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("Frequency",30);
        t.set("IsHalberd",1);
        setStats(t,60,0,120,0,80,0);
        addWithVariants(t);
       
        
        // maces
        
        t = Lib.extend("base mace", "base standard weapon");
        t.set("IsMace",1);
        Lib.add(t);
        
        t = Lib.extend("mace", "base mace");
        t.set("Image",44);
        t.set("LevelMin",4);
        t.set("ItemWeight", 7000);
        t.set("Frequency",50);
        setStats(t,45,0,75,1,30,0);
        addWithVariants(t);
        
        t = Lib.extend("two-handed mace", "base mace");
        t.set("Image",44);
        t.set("LevelMin",5);
        t.set("ItemWeight", 14000);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("Frequency",30);
        setStats(t,50,0,150,0,20,-3);
        addWithVariants(t);
        
        t = Lib.extend("morning star", "base mace");
        t.set("Image",45);
        t.set("LevelMin",4);
        t.set("ItemWeight", 11000);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("Frequency",20);
        t.multiplyStat("AttackCost",1.3);
        setStats(t,60,-3,180,-4,30,0);
        addWithVariants(t);
        
    }
    
    public static void initCrudeWeapons() {
        Thing t;
        
        t=Lib.extend("stick","base weapon");
        setStats(t,50,1,20,2,30,3);
        t.set("Image",40);
        t.set("LevelMin",1);
        t.set("Frequency",100);
        t.set("ItemWeight",1000);
        t.set("ValueBase",30);
        t.set("Material","wood");
        t.set("HitVerb","strike/strikes");
        addWeapon(t);
        
        t=Lib.extend("bone","stick");
        setStats(t,40,2,15,3,15,3);
        t.set("IsEdible",1);
        t.set("Nutrition",3);
        t.set("ItemWeight",2000);
        t.set("Frequency",50);
        t.set("ValueBase",20);
        t.set("WeaponDamageType","impact");
        t.set("Material","bone");
        t.set("Image",302);
        addWeapon(t);
        
        t=Lib.extend("large bone","stick");
        setStats(t,40,2,20,3,15,3);
        t.set("IsEdible",1);
        t.set("Nutrition",3);
        t.set("ItemWeight",4000);
        t.set("Frequency",30);
        t.set("ValueBase",30);
        t.set("WeaponDamageType","impact");
        t.set("Material","bone");
        t.set("Image",302);
        addWeapon(t);
        
        t=Lib.extend("huge bone","bone");
        t.set("ItemWeight",7000);
        t.set("Frequency",25);
        t.set("ValueBase",40);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("HitVerb","bash/bashes");
        setStats(t,40,2,35,6,20,4);
        addWeapon(t);
        
        t=Lib.extend("wooden club","stick");
        t.set("IsClub",1);
        setStats(t,40,1,55,3,20,1);
        t.set("Image",26);
        t.set("LevelMin",1);
        t.set("ItemWeight",8000);
        t.set("Frequency",50);
        t.set("WeaponDamageType","impact");
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("Material","wood");
        t.set("HitVerb","bash/bashes");
        addWeapon(t);
         
        t=Lib.extend("stone club","wooden club");
        setStats(t,35,1,70,5,20,0);
        t.set("Image",25);
        t.set("ItemWeight",16000);
        t.set("LevelMin",3);
        t.multiplyStat("AttackCost",1.3);
        t.set("Material","stone");
        addWeapon(t);   

        t=Lib.extend("spiked club","wooden club");
        setStats(t,40,1,70,8,20,3);
        t.set("Image",50);
        t.set("WeaponDamageType","piercing");
        t.set("ItemWeight",10000);
        t.set("LevelMin",5);
        t.set("Material","wood");
        addWeapon(t); 
        
        t=Lib.extend("cudgel","stick");
        setStats(t,40,2,70,5,20,2);
        t.set("Image",260);
        t.set("ItemWeight",7000);
        t.set("LevelMin",6);
        addWeapon(t);  
        
        t=Lib.extend("scythe","stick");
        setStats(t,60,0,100,0,50,0);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("WeaponDamageType",RPG.DT_NORMAL);
        t.set("Image",49);
        t.set("ItemWeight",5000);
        t.set("LevelMin",5);
        addWeapon(t);  
    
        t=Lib.extend("wooden staff","stick");
        setStats(t,50,1,35,1,90,2);
        t.set("IsStaff",1);
        t.set("Image",60);
        t.set("ItemWeight",4000);
        t.set("ValueBase",60);
        t.multiplyStat("Frequency",0.5);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("WeaponDamageType","impact");
        t.set("DefaultThings","5% [IsWeaponRune]");
        t.set("LevelMin",1);
        addWeapon(t);  
        
        t=Lib.extend("heavy staff","wooden staff");
        setStats(t,50,-1,70,-2,80,1);
        t.set("Image",60);
        t.set("ItemWeight",8000);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("WeaponDamageType","impact");
        t.set("LevelMin",3);
        addWeapon(t);  
        
        t=Lib.extend("wooden log","stick");
        setStats(t,40,-5,120,-5,0,0);
        t.set("Frequency",5);
        t.set("Image",64);
        t.set("ItemWeight",30000);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("WeaponDamageType","impact");
        t.set("LevelMin",7);
        addWeapon(t);  
        
        t=Lib.extend("quarterstaff","wooden staff");
        setStats(t,50,0,50,0,120,2);
        t.set("Image",60);
        t.set("ItemWeight",6000);
        t.set("WieldType",RPG.WT_TWOHANDS);
        t.set("ValueBase",100);
        t.set("LevelMin",5);
        addWeapon(t);  
 
        t=Lib.extend("magic staff","quarterstaff");
        t.set("UName","quarterstaff");
        t.set("IsMagicItem",1);
        t.set("LevelMin",7);
        t.multiplyStat("Frequency",0.5);
        t.set("DefaultThings","[IsWeaponRune],10% [IsWeaponRune]");
        Lib.add(t);
        
        t=Lib.extend("staff of defence","quarterstaff");
        t.set("UName","quarterstaff");
        setStats(t,65,0,40,0,160,20);
        t.set("Armour",6);
        t.set("ItemWeight",5000);
        t.set("LevelMin",9);
        t.multiplyStat("Frequency",0.3);
        addWeapon(t);  
        
        t=Lib.extend("staff of smiting","quarterstaff");
        t.set("UName","quarterstaff");
        setStats(t,80,0,80,20,120,3);
        t.set("ItemWeight",7000);
        t.set("LevelMin",11);
        t.multiplyStat("Frequency",0.3);
        addWeapon(t);  
        
        t=Lib.extend("staff of cursing","quarterstaff");
        t.set("UName","quarterstaff");
        setStats(t,80,0,80,0,120,3);
        t.set("ItemWeight",6000);
        t.set("LevelMin",13);
        t.multiplyStat("Frequency",0.2);
        t.set("OnWeaponDamage",Weapon.damageEffect("curse"));
        addWeapon(t);    
    }
    
    /**
     * Creates a script to inflict special damage effects
     * Can be applied to any weapon or missile
     * 
     * @param s
     * @return
     */
    public static Script damageEffect(String s) {
    	Thing effect=Lib.create(s);
        Script script=Scripts.addEffect("Target",effect);
        return script;    	
    }
    
  
    /**
     * Create monster special attacks
     *
     */
    public static void initAttacks() {
        // Note: these aren't phyical items!
    	// Hence don't use addWeapon()!!
    	Thing t=Lib.extend("base attack","base weapon");
    	t.set("IsAttack",1);
        t.set("IsUnarmedWeapon",1);
        t.set("Frequency",0);
        setStats(t,40,0,40,0,20,0);
        Lib.add(t);        
        
        t=Lib.extend("unarmed attack","base attack");
        t.set("WeaponDamageType", "unarmed");
        t.set("HitVerb","clumsily hit/clumsily hits");
        t.set("AttackCost",150);
        setStats(t,40,0,25,1,10,0);
        Lib.add(t);
        
        t=Lib.extend("kick attack","base attack");
        t.set("WeaponDamageType", "impact");
        t.set("HitVerb","kick/kicks");
        setStats(t,40,0,50,0,20,0);
        Lib.add(t);
        
        t=Lib.extend("bite attack","base attack");
        t.set("HitVerb","bite/bites");
        setStats(t,50,0,70,0,20,0);
        Lib.add(t);
		
        t=Lib.extend("poison bite attack","base attack");
        t.set("HitVerb","bite/bites");
        setStats(t,50,0,70,0,20,0);
        t.set("OnWeaponDamage",Weapon.damageEffect("poison"));
        Lib.add(t);
        
        t=Lib.extend("claw attack","base attack");
        setStats(t,60,0,60,0,50,0);
        t.set("HitVerb","claw/claws");
        t.set("AttackCost", 75);
        Lib.add(t);
        
        t=Lib.extend("razor claw attack","base attack");
        setStats(t,60,0,100,0,50,0);
        t.set("HitVerb","claw/claws");
        t.set("AttackCost", 75);
        Lib.add(t);
        
        t=Lib.extend("bash attack","base attack");
        setStats(t,60,0,60,0,50,0);
        t.set("WeaponDamageType", "impact");
        t.set("HitVerb","bash/bashes");
        t.set("AttackCost", 100);
        Lib.add(t);
        
        t=Lib.extend("poison attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("OnWeaponDamage",Weapon.damageEffect("poison"));
        Lib.add(t);
        
        t=Lib.extend("strong poison attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("OnWeaponDamage",Weapon.damageEffect("strong poison"));
        Lib.add(t);
        
        t=Lib.extend("poison whip attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("HitVerb","whip/whips");
        t.set("WeaponDamageType", "poison");
        t.set("OnWeaponDamage",Weapon.damageEffect("extreme poison"));
        Lib.add(t);
        
        t=Lib.extend("blind attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("OnWeaponDamage",Weapon.damageEffect("curse of blindness"));
        Lib.add(t);
        
        t=Lib.extend("curse attack","base attack");
        setStats(t,60,0,60,0,25,0);
        t.set("OnWeaponDamage",Weapon.damageEffect("curse"));
        Lib.add(t);       
        
        t=Lib.extend("hex attack","base attack");
        setStats(t,60,0,60,0,25,0);
        t.set("OnWeaponDamage",Weapon.damageEffect("hex"));
        Lib.add(t);       
        
        t=Lib.extend("chill attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("WeaponDamageType","chill");
        Lib.add(t);     
        
        t=Lib.extend("ice attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("WeaponDamageType","ice");
        Lib.add(t);    
        
        t=Lib.extend("fire attack","base attack");
        setStats(t,60,0,60,0,40,0);
        t.set("WeaponDamageType","fire");
        Lib.add(t);  
        
        t=Lib.extend("acid attack","base attack");
        setStats(t,60,0,60,0,25,0);
        t.set("WeaponDamageType","acid");
        Lib.add(t); 
        
        t=Lib.extend("drain attack","base attack");
        setStats(t,60,0,60,0,25,0);
        t.set("WeaponDamageType","drain");
        Lib.add(t); 
        
        t=Lib.extend("disintegrate attack","base attack");
        setStats(t,60,0,60,0,25,0);
        t.set("WeaponDamageType","disintegrate");
        Lib.add(t); 
    }
    
    /**
     * Create special weapon alterations
     * These are used to improve weapons 
     * Can be temporary or permanent (set LifeTime)
     *
     */
    public static void initAlterations() {
    	Thing t;
    	
    	t=Lib.extend("base weapon alteration","base thing");
    	t.set("IsAlteration",1);
    	t.set("NoStack",1);
    	t.set("LevelMin",1);
    	t.set("AlterationType","IsWeapon");
    	Lib.add(t);
    	
    	t=Lib.extend("flaming alteration","base weapon alteration");
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","flaming"));
    	t.additem("CarriedModifiers",Modifier.constant("ExtraDamageType","fire"));
    	t.additem("CarriedModifiers",Modifier.bonus("ExtraASTBonus",RPG.d(3,6)));
    	//t.add("CarriedModifiers",Modifier.simple("ItemWeight",100000));
        Lib.add(t);
        
    	t=Lib.extend("accuracy alteration","base weapon alteration");
    	// t.add("CarriedModifiers",Modifier.constant("Adjective","flaming"));
    	t.additem("CarriedModifiers",Modifier.linear(RPG.ST_ASKMULTIPLIER,200,0));
    	//t.add("CarriedModifiers",Modifier.simple("ItemWeight",100000));
        Lib.add(t);
        
    	t=Lib.extend("smiting alteration","base weapon alteration");
    	// t.add("CarriedModifiers",Modifier.constant("Adjective","flaming"));
    	t.additem("CarriedModifiers",Modifier.linear(RPG.ST_ASTMULTIPLIER,200,0));
    	//t.add("CarriedModifiers",Modifier.simple("ItemWeight",100000));
        Lib.add(t);

    }
    
    public static void initSpecialWeapons() {
    	Thing t;
    	
    	t=Lib.extend("staff of mighty smiting","quarterstaff");
    	t.multiplyStat(RPG.ST_ASTMULTIPLIER,3.0);
    	t.set("LevelMin",17);
    	addWeapon(t);
    	
    	t=Lib.extend("mace of destruction","blue steel mace");
    	t.multiplyStat(RPG.ST_ASTMULTIPLIER,2.0);
    	t.set("LevelMin",20);
    	addWeapon(t);
    	
    	t=Lib.extend("sword of death","krithium sword");
    	t.set("SlayingStats","IsLiving*1.4");
    	t.set("LevelMin",23);
    	addWeapon(t);
    	
    	t=Lib.extend("axe of slaying","red steel hand axe");
    	t.set("SlayingStats","IsLiving*1.7");
    	t.set("LevelMin",27);
    	addWeapon(t);
    	
    	t=Lib.extend("vorpal sword","black steel sword");
    	t.set("SlayingStats","IsHumanoid*2.0");
    	t.set("LevelMin",30);
    	addWeapon(t);
    	
    	t=Lib.extend("longsword of light","elven steel longsword");
    	t.set("SlayingStats","IsUndead*2.0");
    	t.set("LevelMin",20);
    	addWeapon(t);
    	
    	t=Lib.extend("crystal whirlwind dagger","crystal dagger");
    	t.multiplyStat("AttackCost",0.4);
    	t.set("LevelMin",26);
    	addWeapon(t);
    }
}