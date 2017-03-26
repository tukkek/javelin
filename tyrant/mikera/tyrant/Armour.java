package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

public class Armour {
    
	
	/**
     * Calculates the armour of a given being
     * vs. a specified damage type
     * 
     * @param t The Thing for which to calculate armour
     * @param dt The type of damage, e.g. "impact"
     * @return
     */
    public static int calcArmour(Thing t, String dt) {
        // no armour vs. speial damage
        if (dt.equals(RPG.DT_SPECIAL)) return 0;
        
        // base armour
        double arm=t.getStat("ARM");
        
        // defece bonus
        int dskill=t.getStat(Skill.DEFENCE);
        
        // small natural bonus for defence skill based on TG
        arm=arm+t.getStat("TG")*(0.1+dskill*0.05);
        
        Thing[] ws=t.getWielded();
        double armourBonus=0;
        for (int i=0; i<ws.length; i++) {
        	double armour=ws[i].getStat("Armour");
        	if (ws[i].getFlag("IsBlessed")) armour*=1.3;
        	if (ws[i].getFlag("IsCursed")) armour*=0.7;
        	armourBonus+=armour;
        }
		
		// armour bonus modified by defence skill
        arm+=armourBonus * (0.6+0.2*dskill);
        
        if (dt.equals(RPG.DT_NORMAL)) {
            // OK - this is default
        } else if (dt.equals(RPG.DT_PIERCING)) {
            arm=arm/3;
        } else if (dt.equals(RPG.DT_ACID)) {
            arm=arm/2;
        } else if (dt.equals(RPG.DT_UNARMED)) {
            arm=arm*2;
        } else {
        	// low armour vs. other attack types
            arm=arm/4;
        }
        
        // Damage specific armour bonuses
        double adtbonus=0;
        for (int i=0; i<ws.length; i++) {
        	adtbonus+=ws[i].getStat("Armour:"+dt);
        }
        arm+=adtbonus * (0.6+0.2*dskill)/ 2.0;
        
        arm=arm+t.getStat("ARM:"+dt);
        return (int)Math.round(arm);
    }
    
    public static String statString(Thing armour) {
    	String s="";
    	if (armour.getFlag("IsShield")) {
    		s=s+"[* * +"+armour.getStat(RPG.ST_DSKMULTIPLIER)/10+"] ";
    	}
    	s=s+ "{arm:"+armour.getStat("Armour")+"}";
    	return s;
    }
    
    public static void init() {
        Thing t = Lib.extend("base armour", "base item");
        t.set("Image", 340);
        t.set("IsArmour", 1);
        t.set("WieldType", RPG.WT_TORSO);
        t.set("HPS", 70);
        t.set("ItemWeight", 20000);
        t.set("ValueBase",100);
        t.set("Frequency", 50);
        t.set("RES:acid",-12);
        t.set("DamageLevels",2);
        t.set("DefaultThings","5% [IsArmourRune]");
        t.set("ASCII","[");
        Lib.add(t);
        
        initHeadgear();
        initFootwear();
        initShields();
        initLeatherArmour();
        initGloves();
        initCloaks();
        initStandardArmour();

    }
    
    private static void initCloaks() {
    	Thing t;
    	
    	t=Lib.extend("base cloak","base armour");
    	t.set("IsCloak",1);
    	t.set("WieldType",RPG.WT_CLOAK);
    	t.set("LevelMin",1);
    	t.set("ItemWeight",800);
    	t.set("Image",356);
    	t.multiplyStat("Frequency",0.8);
    	Lib.add(t);
    	
    	t=Lib.extend("light cloak","base cloak");
    	t.set("Armour",0);
    	t.set("LevelMin",1);
    	Lib.add(t);
    	
    	t=Lib.extend("cloak","base cloak");
    	t.set("Armour",1);
    	t.set("LevelMin",3);
    	t.set("ItemWeight",1200);
    	Lib.add(t);
    	
    	t=Lib.extend("heavy cloak","base cloak");
    	t.set("Armour",2);
    	t.set("LevelMin",7);
    	t.set("ItemWeight",2000);
    	Lib.add(t);
    	
    	t=Lib.extend("elegant cloak","base cloak");
    	t.set("Armour",3);
    	t.set("LevelMin",9);
    	Lib.add(t);
    	
    	t=Lib.extend("cloak of stealth","cloak");
    	t.set("UName","dark cloak");
    	t.set("Armour",1);
    	t.set("LevelMin",6);
    	t.add("WieldedModifiers",Modifier.bonus(Skill.STEALTH,1));
    	Lib.add(t);
    	
    	t=Lib.extend("cloak of protection","heavy cloak");
    	t.set("UName","heavy cloak");
    	t.set("Armour",5);
    	t.set("LevelMin",10);
    	Lib.add(t);
    	
    	t=Lib.extend("cloak of defence","cloak");
    	t.set("UName","ragged cloak");
    	t.set("Armour",3);
    	t.set("LevelMin",15);
    	t.add("WieldedModifiers",Modifier.bonus(Skill.DEFENCE,1));

    	Lib.add(t);
    }
    
    private static void initGloves() {
    	Thing t;
    	
    	t=Lib.extend("base gloves","base armour");
    	t.set("IsGlove",1);
    	t.set("NameType",Description.NAMETYPE_QUANTITY);
    	t.set("WieldType",RPG.WT_HANDS);
    	t.set("LevelMin",1);
    	t.set("ItemWeight",500);
    	t.set("Image",370);
		Weapon.setStats(t,40,0,40,0,25,0);
    	t.multiplyStat("Frequency",0.5);
    	Lib.add(t);
    	
    	t=Lib.extend("leather gloves","base gloves");
    	t.set("Armour",1);
    	t.set("LevelMin",3);
    	Lib.add(t);
    	
    	t=Lib.extend("soft gloves","base gloves");
    	t.set("Armour",0);
    	t.set("LevelMin",1);
    	Lib.add(t);
    	
    	t=Lib.extend("thieving gloves","soft gloves");
    	t.set("UName","soft gloves");
    	t.multiplyStat("Frequency",0.2);
        t.add("WieldedModifiers",Modifier.bonus(Skill.PICKPOCKET,1));
        t.add("WieldedModifiers",Modifier.bonus("SK",2));
    	t.set("LevelMin",10);
    	Lib.add(t);
    	
    	t=Lib.extend("thick leather gloves","base gloves");
    	t.set("Armour",2);
    	t.set("LevelMin",6);
    	t.set("ItemWeight",800);
    	Lib.add(t);
    	
    	t=Lib.extend("leather gauntlets","base gloves");
    	t.set("Armour",3);
		t.set("IsGauntlet",1);
    	t.set("LevelMin",9);
    	t.set("ItemWeight",900);
		Weapon.setStats(t,35,0,45,0,25,0);
    	Lib.add(t);
    	
    	t=Lib.extend("gauntlets of power","leather gauntlets");
    	t.set("Armour",4);
    	t.set("LevelMin",12);
    	t.set("ItemWeight",900);
    	t.multiplyStat("Frequency",0.2);
        t.add("WieldedModifiers",Modifier.bonus("ST",RPG.d(3)));
    	Lib.add(t);
    	
    	t=Lib.extend("gauntlets of protection","leather gauntlets");
    	t.set("UName","leather gauntlets");
    	t.set("Armour",10);
    	t.set("LevelMin",14);
    	t.set("ItemWeight",1000);
    	t.multiplyStat("Frequency",0.2);
        t.add("WieldedModifiers",Modifier.bonus(Skill.DEFENCE,1));
    	Lib.add(t);
    	
    	t=Lib.extend("red leather gauntlets","leather gauntlets");
    	t.set("UName","leather gauntlets");
    	t.set("Image",371);
    	t.set("Armour",5);
    	t.set("LevelMin",16);
    	t.set("ItemWeight",700);
    	Lib.add(t);
    	
    	t=Lib.extend("red dragon leather gauntlets","red leather gauntlets");
    	t.set("UName","red leather gauntlets");
    	t.set("Armour",12);
    	t.set("Image",371);
    	t.set("LevelMin",23);
    	t.set("ItemWeight",800);
        t.add("WieldedModifiers",Modifier.bonus("ARM:fire",8));
    	Lib.add(t);
    	
    	t=Lib.extend("gauntlets","base gloves");
    	t.set("Image",372);
    	t.set("LevelMin",7);
    	t.set("Armour",4);
    	t.set("ItemWeight",1500);
		Weapon.setStats(t,35,0,50,0,25,0);
    	addWithVariants(t);
    	
    }

    
    private static void initLeatherArmour() {
    	Thing t;
    	
    	t=Lib.extend("base leather armour","base armour");
    	t.set("Image",348);
    	t.set("ItemWeight",2500);
    	t.set("ARM",4);
    	t.set("LevelMin",1);
        t.set("DefaultThings","2% [IsArmourRune],2% [IsItemRune]");
    	Lib.add(t);

    	t=Lib.extend("leather vest","base leather armour");
    	t.set("Image",347);
    	t.set("ItemWeight",1500);
    	t.set("Armour",2);
    	t.set("LevelMin",1);
    	addArmour(t);
    	
    	t=Lib.extend("leather armour","base leather armour");
    	t.set("Image",348);
    	t.set("ItemWeight",2500);
    	t.set("Armour",5);
    	t.set("LevelMin",4);
    	addArmour(t);
    	
    	t=Lib.extend("studded leather armour","base leather armour");
    	t.set("Image",351);
    	t.set("ItemWeight",2800);
    	t.set("Armour",6);
    	t.set("LevelMin",7);
    	addArmour(t);
    	
    	t=Lib.extend("dragon leather armour","leather armour");
    	t.set("Image",348);
    	t.set("ItemWeight",3000);
    	t.set("Armour",25);
    	t.set("Armour:fire",25);
    	t.set("LevelMin",23);
    	addArmour(t);
    	
    	t=Lib.extend("red dragon leather armour","dragon leather armour");
    	t.set("Armour:fire",80);
    	t.set("LevelMin",26);
    	addArmour(t);
    }
    
    private static void initHeadgear() {
        Thing t;
        
        t=Lib.extend("base headgear","base armour");
        t.set("WieldType", RPG.WT_HEAD);
        t.set("ItemWeight", 1000);
        t.set("Image", 323);
        Lib.add(t);
        
        t=Lib.extend("base crown","base headgear");
        t.set("ItemWeight", 2000);
        t.set("Image", 425);
        Lib.add(t);
        
        t = Lib.extend("base helmet", "base headgear");
        t.set("IsHelmet", 1);
        t.set("Image", 323);
        t.set("LevelMin", 1);
        t.set("HPS", 40);
        t.set("ItemWeight", 4000);
        Lib.add(t);
        
        initCaps();
    }
    
    private static void initShields() {
    	Thing t;
    	
    	t=Lib.extend("base shield","base armour");
    	t.set("IsShield",1);
    	t.set("WieldType",RPG.WT_SECONDHAND);
    	t.set("Image",380);
    	t.set("DSKBonus",8);
    	t.set(RPG.ST_DSKMULTIPLIER,60);
        t.set("ItemWeight", 6000);
        t.set("Armour", 5);
        t.set("DefaultThings","2% [IsArmourRune],2% [IsShieldRune]");
    	Lib.add(t);
    	
    	t=Lib.extend("small shield","base shield");
        t.set("ItemWeight", 4000);
    	t.set("DSKBonus",2);
    	t.set(RPG.ST_DSKMULTIPLIER,70);
    	t.set("LevelMin",1);
    	t.set("Image",380);
        t.set("Armour", 4);
    	addWithVariants(t);
    	
    	t=Lib.extend("buckler","base shield");
        t.set("ItemWeight", 2500);
    	t.set("DSKBonus",0);
    	t.set(RPG.ST_DSKMULTIPLIER,100);
    	t.set("LevelMin",3);
    	t.set("Image",388);
        t.set("Armour", 3);
    	addWithVariants(t);
    	
    	t=Lib.extend("shield","base shield");
        t.set("ItemWeight", 6000);
    	t.set("DSKBonus",2);
    	t.set(RPG.ST_DSKMULTIPLIER,65);
    	t.set("LevelMin",3);
    	t.set("Image",381);
        t.set("Armour", 6);
    	addWithVariants(t);
    	
    	t=Lib.extend("large shield","base shield");
        t.set("ItemWeight", 10000);
    	t.set("DSKBonus",0);
    	t.set(RPG.ST_DSKMULTIPLIER,60);
    	t.set("LevelMin",4);
    	t.set("Image",381);
        t.set("Armour", 10);
    	addWithVariants(t);
    }
    
    private static void initCaps() {
        Thing t;
        
        t = Lib.extend("leather cap", "base helmet");
        t.set("Image", 320);
        t.set("WieldType", RPG.WT_HEAD);
        t.set("HPS", 24);
        t.set("Armour", 1);
        t.set("ItemWeight", 2000);
        Lib.add(t);
        
        t = Lib.extend("feathered cap", "base helmet");
        t.set("Image", 321);
        t.set("WieldType", RPG.WT_HEAD);
        t.set("HPS", 13);
        t.set("ItemWeight", 1200);
        Lib.add(t);
        
        t = Lib.extend("magic cap", "base helmet");
        t.set("UName","feathered cap");
        t.set("Image", 321);
        t.set("WieldType", RPG.WT_HEAD);
        t.set("HPS", 13);
        t.set("ItemWeight", 1200);
        t.set("Armour",2);
        t.set("LevelMin",10);
        t.set("DefaultThings","100% [IsArmourRune]");
        t.multiplyStat("Frequency",0.2);
        Lib.add(t);
        
    	t=Lib.extend("mining cap","base helmet");
    	t.set("UName","sturdy cap");
    	t.set("Image",324);
    	t.set("LevelMin",5);	
    	t.set("Armour",2);
    	t.set("ItemWeight",2300);  
    	t.set("Frequency",5);
        t.add("WieldedModifiers",Modifier.bonus(Skill.MINING,1));
        Lib.add(t);
    }
    
    private static void addFootwear(Thing t) {
        String n=t.getstring("Name");
        String un=t.getstring("UName");
        if (n!=null) t.set("NamePlural","pairs of "+n);
        if (un!=null) t.set("UNamePlural","pairs of "+un);
        Lib.add(t);
    }
    
    private static void initFootwear() {
        Thing t;
        
        t=Lib.extend("base footwear","base armour");
        t.set("WieldType", RPG.WT_BOOTS);
        t.set("IsFootwear", 1);
        t.multiplyStat("Frequency",0.5);
        Weapon.setStats(t,50,0,70,0,0,0);
        t.set("WeaponDamageType","impact");
        t.set("HitVerb","kick/kicks");
        Lib.add(t);
        
        t = Lib.extend("leather boots", "base footwear");
        t.set("Image", 360);
        t.set("NameType", Description.NAMETYPE_QUANTITY);
        t.set("HPS", 24);
        t.set("LevelMin", 5);
        t.set("ItemWeight", 2800);
        t.set("ValueBase",100);
        t.set("Material","leather");
        addFootwear(t);
        
        t = Lib.extend("old boots", "leather boots");
        t.set("UName","worn boots");
        t.set("LevelMin", 1);
        addFootwear(t);
        
        t = Lib.extend("boots of agility", "leather boots");
        t.set("UName","worn boots");
        t.set("LevelMin", 6);
        t.add("WieldedModifiers",Modifier.linear("AG",100,RPG.d(2,6)));
        addFootwear(t);
        
        t = Lib.extend("shoes", "leather boots");
        t.set("LevelMin", 2);
        t.set("ItemWeight", 1400);
        t.set("Image", 362);
        addFootwear(t);
        
        t = Lib.extend("fine shoes", "shoes");
        t.set("LevelMin", 12);
        t.set("ItemWeight", 1200);
        t.set("Image", 362);
        t.set("Armour",1);
        t.add("WieldedModifiers",Modifier.bonus("CH",2));
        addFootwear(t);
        
        t = Lib.extend("dancing shoes", "shoes");
        t.set("LevelMin", 5);
        t.add("WieldedModifiers",Modifier.bonus("AG",3));
        t.set("Image", 362);
        addFootwear(t);
        
        t = Lib.extend("ironclad boots", "leather boots");
        t.set("UName","iron boots");
        t.set("LevelMin", 11);
        t.set("Image", 361);
        t.set("Armour",8);
        t.multiplyStat("ItemWeight",1.5);
        Weapon.setStats(t,50,0,80,0,0,0);
        addFootwear(t);
        
        t = Lib.extend("magic boots", "leather boots");
        t.set("IsMagicItem",1);
        t.set("UName","worn boots");
        t.add("WieldedModifiers",Modifier.linear("MoveSpeed",100,100));
        t.set("Armour", 4);
        t.set("LevelMin", 12);
        addFootwear(t);
        
    }

    public static void addArmour(Thing t) {
    	if (t.getStat("ValueBase")==0) {
    		t.set("ValueBase",t.getStat("ItemWeight")/10);
    	}
    	
    	Lib.add(t);
    }
    
    // armour modifiers for each primary material
    // iron is "standard"
    private static String[] amat={"iron","steel","silver",   "elven steel", "mithril", "krithium","black steel","blue steel", "red steel", "crystal", "parillite", "sapphire",         "emerald"};
    private static String[] umat={null,  null,   "silvery",  "shining",     "silvery", "dark",    "dark",       "silvery",     null,        "shining", "shining",  "shimmering blue", "shimmering green"};
    private static int[] levels= {3  ,   7 ,     9,          12,            20,        16,        22,           15,           18,          60,        28,          25,                 31};
    private static int[] weights={100,   90,     80,         70,            50,        160,       130,          80,           100,         60,        40,          65,                 60};
    private static int[] freqs=  {100,   70,     20,         50,            20,        40,        50,           50,           60,          30,        10,          20,                 10};
    private static int[] skills= {100,   105,    100,        120,           140,       80,        100,          150,          110,         140,       170,         190,                220};
    private static int[] strs=   {100,   110,    90,         120,           150,       140,       180,          140,          160,         140,       200,         210,                250};
    	
    // add variants of different metal types
    public static void addWithVariants(Thing a) {
        
        for (int i=0; i<amat.length; i++) {
            Thing t=(Thing)a.clone();
            String name=t.name();
            t.set("Name",amat[i]+" "+name);
            t.set("Material",amat[i]);
            t.set("UName", (umat[i]==null) ? name : umat[i]+" "+name);
            
            // plural for boots
            if (name.indexOf("boots")>=0) {
                String n=t.getstring("Name");
                String un=t.getstring("UName");
                if (n!=null) t.set("NamePlural","pairs of "+n);
                if (un!=null) t.set("UNamePlural","pairs of "+un);
            }
            
            t.multiplyStat("ItemWeight",weights[i]/100.0);
            t.set("LevelMin",RPG.max(1,levels[i]+t.getStat("LevelMin")));
            t.multiplyStat("Frequency",freqs[i]/100.0);
            t.multiplyStat(RPG.ST_DSKMULTIPLIER,skills[i]/100.0);
            t.multiplyStat(RPG.ST_DSKBONUS,skills[i]/100.0);
            t.multiplyStat("Armour",(strs[i]/100.0)*(strs[i]/100.0)*(strs[i]/100.0));
            t.set("HPS",t.getStat("Armour")*5);
            addArmour(t);
        } 
    }    
    
    private static void initStandardArmour() {
        
    	Thing t;
    	
    	t=Lib.extend("base standard armour","base armour");
        t.set("DefaultThings","2% [IsArmourRune],2% [IsItemRune]");
    	t.set("Frequency",30);
    	Lib.add(t);
    	
    	// ARMOUR
    	t=Lib.extend("chain mail","base standard armour");
    	t.set("Image",349);
    	t.set("LevelMin",3);
    	t.set("Armour",10);
    	t.set("ItemWeight",8000);
    	addWithVariants(t);
    	
    	t=Lib.extend("scale mail","base standard armour");
    	t.set("Image",350);
    	t.set("LevelMin",4);	
    	t.set("Armour",14);
    	t.set("ItemWeight",11000);
    	addWithVariants(t);
    	
    	t=Lib.extend("banded mail","base standard armour");
    	t.set("Image",353);
    	t.set("LevelMin",5);	
    	t.set("Armour",20);
    	t.set("ItemWeight",15000);
    	addWithVariants(t);
    	
    	t=Lib.extend("plate mail","base standard armour");
    	t.set("Image",354);
    	t.set("LevelMin",6);	
    	t.set("Armour",30);
    	t.set("ItemWeight",20000);
    	addWithVariants(t);
    	
    	t=Lib.extend("heavy plate mail","base standard armour");
    	t.set("Image",354);
    	t.set("LevelMin",6);	
    	t.set("Armour",40);
    	t.set("ItemWeight",30000);
    	addWithVariants(t);
    	
    	t=Lib.extend("plate armour","base standard armour");
    	t.set("Image",355);
    	t.set("LevelMin",6);	
    	t.set("Armour",60);
    	t.set("WieldType",RPG.WT_FULLBODY);
    	t.set("ItemWeight",40000);
    	addWithVariants(t);
    	
    	t=Lib.extend("heavy plate armour","base standard armour");
    	t.set("Image",355);
    	t.set("LevelMin",7);	
    	t.set("Armour",80);
    	t.set("ItemWeight",60000);
     	t.set("WieldType",RPG.WT_FULLBODY);
        addWithVariants(t);
        
        // HELMETS
        t=Lib.extend("base standard helmet","base standard armour");
    	t.set("Image",323);
    	t.set("WieldType",RPG.WT_HEAD);
    	t.set("ItemWeight",3000);
    	t.set("Frequency",30);
    	Lib.add(t);
        
    	t=Lib.extend("cap","base standard helmet");
    	t.set("Image",324);
    	t.set("LevelMin",1);	
    	t.set("Armour",2);
    	t.set("ItemWeight",2000);        
    	addWithVariants(t);  
    	
   	
    	t=Lib.extend("studded cap","base standard helmet");
    	t.set("Image",320);
    	t.set("LevelMin",1);	
    	t.set("Armour",3);
    	t.set("ItemWeight",2200);
    	addWithVariants(t);
        
        t=Lib.extend("helmet","base standard helmet");
    	t.set("Image",323);
    	t.set("LevelMin",2);	
    	t.set("Armour",4);
    	t.set("ItemWeight",4000); 
    	addWithVariants(t);
        
        t=Lib.extend("horned helm","base standard helmet");
    	t.set("Image",330);
    	t.set("LevelMin",4);	
    	t.set("Armour",7);
    	t.set("ItemWeight",6000); 
        addWithVariants(t);
        
        t=Lib.extend("spike helm","base standard helmet");
    	t.set("Image",329);
    	t.set("LevelMin",5);	
    	t.set("Armour",6);
    	t.set("ItemWeight",4500); 
        addWithVariants(t);
        
        t=Lib.extend("large helmet","base standard helmet");
    	t.set("Image",339);
    	t.set("LevelMin",6);	
    	t.set("Armour",10);
    	t.set("ItemWeight",8000); 
    	addWithVariants(t);
        
        // Metal boots
        t=Lib.extend("base standard boots","base footwear");
        t.set("WieldType",RPG.WT_BOOTS);
        t.set("Frequency",10);
        Weapon.setStats(t,50,0,70,0,0,0);
        Lib.add(t);
        
        t = Lib.extend("plated shoes", "base standard boots");
        t.set("LevelMin", 3);
        t.set("Image", 361);
        t.set("Armour",2);
        t.set("ItemWeight",1500);
        addWithVariants(t);
        
        t = Lib.extend("plated boots", "base standard boots");
        t.set("LevelMin", 3);
        t.set("Image", 361);
        t.set("Armour",3);
        t.set("ItemWeight",3000);
        addWithVariants(t);
        
        t = Lib.extend("plated heavy boots", "base standard boots");
        t.set("LevelMin", 4);
        t.set("Image", 361);
        t.set("Armour",5);
        t.set("ItemWeight",5000);
        addWithVariants(t);
    }
}