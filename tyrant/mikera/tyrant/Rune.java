/*
 * Created on 29-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Rune {
	protected static final String[] runes=new String[] {
			"Ait",			
			"Ber",
			"Ciz",
			"Dar",
			"Erg",
			"Futh",
			"Gu",
			"Hok",
			"Ik",
			"Jos",
			"Kaz",
			"Llam",
			"Mon",
			"Ni",
			"Om",
			"Puk",
			"Qor",
			"Rux",
			"Si",
			"Tux",
			"Uth",
			"Vorg",
			"Wei",
			"Xu",
			"Yrg",
			"Zyk"
	};

	public static void init() {
		// runestones are uses for runecasting
		Thing t=Lib.extend("base runestone","base item");
		t.set("ItemWeight",50);
		t.set("IsRunestone",1);
		t.set("ValueBase",100);
		t.set("LevelMin",1);
		t.set("IsRune",1);
		t.set("HPS",3);
		t.set("Image",500);
		t.set("Frequency",50);
		// t.set("RecipeIngredients","blank runestone");
		t.set("OnUse",new RuneUseScript());
		t.set("IsRuneIngredient",1);
		t.set("ASCII","~");
		Lib.add(t);
		
		// runes can be added to items to change their properties
		t=Lib.extend("base rune","base thing");
    	t.set("IsRune",1);
    	t.set("IsAlteration",1);
    	t.set("NoStack",1);
    	t.set("LevelMin",1);
    	t.additem("CarriedModifiers",Modifier.constant("IsRunic",1));
		Lib.add(t);
		
		t=Lib.extend("blank runestone","base runestone");
		t.set("IsRuneIngredient",0);
		t.set("OnUse",new RuneScribeScript());
		Lib.add(t);
		
		initRuneStones();
		initWeaponRunes();
		initArmourRunes();
		initItemRunes();
        initShieldRunes();
        initMissileRunes();
	}
	
	private static void addRuneStone(Thing t) {
		Lib.add(t);
	}
	
	public static String runeStoneName(int i) {
		return runes[i]+" runestone";
	}
	
	private static void initRuneStones() {
		for (int i=0; i<runes.length; i++) {
			Thing t=Lib.extend(runeStoneName(i),"base runestone");
			t.set("UName","strange runestone");
			t.set("LevelMin",i+6+i/3+RPG.r(i/2)); // max=50!
			t.set("Frequency",55-2*i);
			t.set("Image",501+i%3);
			t.set("RuneIndex",i);
			
			// some oddities
			if (runes[i].equals("Si")) {
				t.set("IsActive",1);
				t.addHandler("OnAction",Scripts.generator("Si rune",5));
			} else if (runes[i].equals("Rux")) {
				t.set("IsDestructible",0);
			} else if (runes[i].equals("Wei")) {
				t.set("IsActive",1);
				t.addHandler("OnAction",Scripts.generator("flutterby",100));
			} else if (runes[i].equals("Llam")) {
				t.set("DeathDecoration","Blaze");
			}
			
			addRuneStone(t);
		}
		
	}
	
	protected static int countExistingRunes(Thing t) {
		Thing[] rs=t.getFlaggedContents("IsRune");
		int rc=0;
		for (int i=0; i<rs.length; i++) {
			rc+=rs[i].getStat("RuneCount");
		}
		return rc;
	}
	
	protected static int destroyExistingRunes(Thing t, int chance) {
		Thing[] rs=t.getFlaggedContents("IsRune");
		int rc=0;
		for (int i=0; i<rs.length; i++) {
			if (RPG.r(100)<chance) {
				rc++;
				rs[i].remove();
			}
		}
		return rc;
	}
	
	private static class RuneScribeScript extends Script {
		private static final long serialVersionUID = 3762820367848585273L;

        public boolean handle(Thing t, Event e) {
    		Thing user=e.getThing("User");
    		
    		int skill=user.getStat(Skill.RUNELORE)-1;
    		if (skill<0) {
    			Game.messageTyrant("You don't know how to scribe runes");
    			return false;
    		} else if (skill==0) {
    			Game.messageTyrant("You must further improve your Rune Lore skill in order to scribe runes");
    		}
    		
    		String[] rs=new String[RPG.min(runes.length,skill)];
    		for (int i=0; i<rs.length; i++) rs[i]=runes[i];
    		
    		String rname=Game.selectString("Select a rune to scribe: ",rs);
    		rname=rname+" runestone";
    		
    		if (rs!=null) {
    			t.unequip(1);
    			Thing r=Lib.create(rname);
    			int lev=r.getStat("RuneIndex");
    			int prob=RPG.middle(0,50-3*lev+(user.getStat("CR")*(skill-lev)/RPG.round(Math.pow(1.15,lev))),100);
    			Game.warn("Success chance = "+prob);
    			if (RPG.r(100)<prob) {
    				Item.identify(r);
    				user.message("You successfully scribe "+r.getAName());
    				user.addThingWithStacking(r);
    			} else {
    				user.message("You fail to scribe the "+rname+" correctly");
    			}
    		}
    		
    		return false;
		}
	}
	
	private static class RuneUseScript extends Script {
		private static final long serialVersionUID = 3258688810429592888L;

        public boolean handle(Thing t, Event e) {
    		Thing user=e.getThing("User");
    		
    		Thing it=Game.selectItem("Select an item to apply the rune to:",user.getItems());
    		
    		if (it==null) {
    			return false;
    		}
    		
    		
    		if (it.getStat("Number")>1) {
    			it=it.separate(RPG.d(4));
    		}
    		
    		String at=t.getstring("AlterationType");
    		
    		if (at==null) {
    			Game.messageTyrant("Nothing seems to happen");
    			Game.warn("No AlterationType for "+t.name());
    			return true;
    		}
    		
			if (!it.getFlag(at)) {
    			Game.messageTyrant("You do not seem to be able to use "+t.getTheName()+" with "+it.getTheName());
				return true;
			}
    		
    		t.unequip(1);

			if (it.getFlag("IsArtifact")) {
    			Game.messageTyrant(it.getTheName()+" seems totally unaffected");   				
    			return true;
			}
			
			int rc=countExistingRunes(it);
			if (rc>0) {
				int des=destroyExistingRunes(it,50);
				if (des>0) Game.messageTyrant("Some existing runes are destroyed!");
			}
			
    		if (t.handles("OnApply")) {
    			Game.messageTyrant(it.getTheName()+" shines brightly for a second");
    			Event de=new Event("Apply");
    			de.set("Target",it);
    			de.set("User",user);
    			t.handle(de);
    		}
		
    		return true;
		}
	}
	
	private static void addRune(Thing t) {
		Lib.add(t);

		// add a corresponding runestone
		String rsn=t.name().replaceFirst("rune","runestone");
		Thing rs=Lib.extend(rsn,"base runestone");
		rs.set("Frequency",15);
		rs.set("LevelMin",t.get("LevelMin"));
		rs.set("UName","weird runestone");
		rs.set("AlterationType",t.get("AlterationType"));
		rs.set("IsWeaponRunestone",t.get("IsWeaponRune"));
		rs.set("Image",501+RPG.r(3));
		rs.set("ValueBase",1000);
		rs.set("OnApply",Scripts.addThing("Target",t.name()));
		Lib.add(rs);
		
		Recipe.register(Skill.RUNELORE,rs,rs.getStat("RuneCount"));
		
	}
	
	private static void initItemRunes() {
	 	Thing t=Lib.extend("base item rune","base rune");
    	t.set("IsItemRune",1);
    	t.set("Frequency",20);
    	t.set("AlterationType","IsItem");
    	t.set("ValueBase",1000);
    	Lib.add(t);
    	
     	t=Lib.extend("permacurse rune","base item rune");
    	t.set("LevelMin",5);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.bonus("IsCursed",1));
        addRune(t);
    	
     	t=Lib.extend("lightness rune","base item rune");
    	t.set("LevelMin",15);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.linear("ItemWeight",80,0));
        addRune(t);
        
     	t=Lib.extend("featherweight rune","base item rune");
    	t.set("LevelMin",25);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.linear("ItemWeight",60,0));
        addRune(t);
	}

	private static void initArmourRunes() {
	 	Thing t=Lib.extend("base armour rune","base rune");
    	t.set("IsArmourRune",1);
    	t.set("Frequency",30);
    	t.set("AlterationType","IsArmour");
    	t.set("ValueBase",500);
    	Lib.add(t);
    	
	 	t=Lib.extend("light armour rune","base armour rune");
    	t.set("LevelMin",7);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.linear("ItemWeight",90,0));
        addRune(t);
    	
	 	t=Lib.extend("armour rune","base armour rune");
    	t.set("LevelMin",6);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.bonus("Armour",4));
        addRune(t);
        
     	t=Lib.extend("toughness rune","base armour rune");
    	t.set("LevelMin",11);
    	t.set("RuneCount",3);
       	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.linear("TG",100,RPG.d(3))));
        addRune(t);
        
	 	t=Lib.extend("advanced armour rune","base armour rune");
    	t.set("LevelMin",16);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.bonus("Armour",16));
        addRune(t);
        
	 	t=Lib.extend("fire defence rune","base armour rune");
    	t.set("LevelMin",7);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("ARM:fire",16)));
        addRune(t);
        
	 	t=Lib.extend("ice defence rune","base armour rune");
    	t.set("LevelMin",9);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("ARM:fire",20)));
        addRune(t);
        
	 	t=Lib.extend("poison resistance rune","base armour rune");
    	t.set("LevelMin",12);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("RES:poison",2)));
        addRune(t);
        
     	t=Lib.extend("bravery rune","base armour rune");
    	t.set("LevelMin",14);
    	t.set("RuneCount",3);
       	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("Bravery",1)));
        addRune(t);
        
	 	t=Lib.extend("speed rune","base armour rune");
    	t.set("LevelMin",17);
    	t.set("RuneCount",4);
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("MoveSpeed",20)));
        addRune(t);
        
	 	t=Lib.extend("greater armour rune","base armour rune");
    	t.set("LevelMin",26);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.linear("Armour",110,30));
        addRune(t);
        
	 	t=Lib.extend("fire immunity rune","base armour rune");
    	t.set("LevelMin",33);
    	t.set("RuneCount",4);
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("RES:fire",12)));
        addRune(t);
        
	 	t=Lib.extend("poison immunity rune","base armour rune");
    	t.set("LevelMin",37);
    	t.set("RuneCount",4);
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.bonus("RES:poison",12)));
        addRune(t);
        
	 	t=Lib.extend("ultimate armour rune","base armour rune");
    	t.set("LevelMin",45);
    	t.set("RuneCount",4);
    	t.additem("CarriedModifiers",Modifier.linear("Armour",140,120));
        addRune(t);
    	
	}
    	
	private static void initShieldRunes() {
	 	Thing t=Lib.extend("base shield rune","base rune");
    	t.set("IsShieldRune",1);
    	t.set("Frequency",40);
    	t.set("AlterationType","IsShield");
    	t.set("ValueBase",600);
    	Lib.add(t);
    	
     	t=Lib.extend("shield enhancement rune","base shield rune");
    	t.set("LevelMin",1);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.bonus("Armour",4));
        addRune(t);
        
     	t=Lib.extend("willpower rune","base shield rune");
    	t.set("LevelMin",5);
    	t.set("RuneCount",3);
       	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.linear("WP",100,RPG.d(3))));
        addRune(t);
        
     	t=Lib.extend("defensive shield rune","base shield rune");
    	t.set("LevelMin",13);
    	t.set("RuneCount",3);
       	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.linear(Skill.DEFENCE,0,1)));
        addRune(t);
	}
	
	private static void initMissileRunes() {
	 	Thing t=Lib.extend("base missile rune","base rune");
    	t.set("IsMissileRune",1);
    	t.set("Frequency",40);
    	t.set("AlterationType","IsItem");
	 	Lib.add(t);
	 	
     	t=Lib.extend("returning tendency rune","base weapon rune");
    	t.set("LevelMin",1);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.bonus("MissileReturns",15));
     	addRune(t);
     	
     	t=Lib.extend("returning rune","base weapon rune");
    	t.set("LevelMin",25);
    	t.set("RuneCount",4);
    	t.additem("CarriedModifiers",Modifier.bonus("MissileReturns",100));
     	addRune(t);
	}
	
	private static void initWeaponRunes() {
	 	Thing t=Lib.extend("base weapon rune","base rune");
    	t.set("IsWeaponRune",1);
    	t.set("Frequency",40);
    	t.set("AlterationType","IsWeapon");
    	t.set("ValueBase",500);
    	Lib.add(t);
    	
     	t=Lib.extend("flame damage rune","base weapon rune");
    	t.set("LevelMin",1);
    	t.set("RuneCount",2);
     	t.additem("CarriedModifiers",Modifier.constant("Adjective","smouldering"));
    	t.additem("CarriedModifiers",Modifier.constant("ExtraDamageType","fire"));
    	t.additem("CarriedModifiers",Modifier.bonus("ExtraASTBonus",RPG.d(2,4)));
        addRune(t);
        
     	t=Lib.extend("defence rune","base weapon rune");
    	t.set("LevelMin",4);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.bonus("DSKBonus",RPG.d(2,4)));
        addRune(t);
        
     	t=Lib.extend("strength rune","base weapon rune");
    	t.set("LevelMin",14);
    	t.set("RuneCount",4);
       	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.linear("ST",100,RPG.d(3))));
        addRune(t);
        
     	t=Lib.extend("ice damage rune","base weapon rune");
    	t.set("LevelMin",6);
    	t.set("RuneCount",3);
     	t.additem("CarriedModifiers",Modifier.constant("Adjective","icy"));
    	t.additem("CarriedModifiers",Modifier.constant("ExtraDamageType","ice"));
    	t.additem("CarriedModifiers",Modifier.bonus("ExtraASTBonus",RPG.d(2,5)));
        addRune(t);
        
     	t=Lib.extend("flaming rune","base weapon rune");
    	t.set("LevelMin",5);
    	t.set("RuneCount",2);
     	t.additem("CarriedModifiers",Modifier.constant("Adjective","flaming"));
    	t.additem("CarriedModifiers",Modifier.constant("ExtraDamageType","fire"));
    	t.additem("CarriedModifiers",Modifier.bonus("ExtraASTBonus",RPG.d(3,6)));
        addRune(t);
        
     	t=Lib.extend("poison tip rune","base weapon rune");
    	t.set("LevelMin",8);
    	t.set("RuneCount",2);
     	t.additem("CarriedModifiers",Modifier.constant("Adjective","poison-dripping"));
    	t.additem("CarriedModifiers",Modifier.constant("ExtraDamageType","poison"));
    	t.additem("CarriedModifiers",Modifier.bonus("ExtraASTBonus",RPG.d(3,6)));
        addRune(t);
        
    	t=Lib.extend("accuracy rune","base weapon rune");
    	t.set("LevelMin",10);
    	t.set("RuneCount",1);
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","glowing"));
    	t.additem("CarriedModifiers",Modifier.linear(RPG.ST_ASKMULTIPLIER,200,0));
        addRune(t);
       
    	t=Lib.extend("smiting rune","base weapon rune");
    	t.set("LevelMin",15);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","glowing"));
    	t.additem("CarriedModifiers",Modifier.linear(RPG.ST_ASTMULTIPLIER,200,0));
        addRune(t);
       
    	t=Lib.extend("rune of quick striking","base weapon rune");
    	t.set("LevelMin",20);
    	t.set("RuneCount",2);
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","glowing"));
    	t.additem("CarriedModifiers",Modifier.linear(RPG.ST_ATTACKCOST,80,0));
        addRune(t);
     
    	t=Lib.extend("rune of fearsome appearance","base weapon rune");
    	t.set("LevelMin",20);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","glowing"));
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.linear(RPG.ST_FEARFACTOR,0,1)));
        addRune(t);
       
    	t=Lib.extend("rune of terror","base weapon rune");
    	t.set("LevelMin",35);
    	t.set("RuneCount",4);
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","shrieking"));
    	t.additem("CarriedModifiers",Modifier.addModifier("WieldedModifiers",Modifier.linear(RPG.ST_FEARFACTOR,0,3)));
        addRune(t);
      
    	t=Lib.extend("whirlwind rune","base weapon rune");
    	t.set("LevelMin",30);
    	t.set("RuneCount",3);
    	t.additem("CarriedModifiers",Modifier.constant("Adjective","smoking"));
    	t.additem("CarriedModifiers",Modifier.linear(RPG.ST_ATTACKCOST,60,0));
        addRune(t);
	}
}
