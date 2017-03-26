package tyrant.mikera.tyrant;

import java.util.*;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;


/**
 * 
 * A potion represents a magical liquid which can be drunk with
 * various effects. Some may be good, some may be bad so drinking
 * an unidentified Potion may be dangerous.....
 *
 * TODO:
 * make all potion effects into spells and change Potion so that
 * it is in effect casting the spell on the drinker. This will also
 * allow potion effects to be re-used as spells or special effects for
 * other items.
 */ 
public class Potion  {
    private static String[] potionstyles = {"frothy", "bubbling", "oily",
    "small", "tiny", "smelly", "fragrant", "boiling", "glowing",
    "milky", "murky", "cloudy", "sizzling", "flaming", "fuming",
    "shimmering"};
    private static String[] potioncolours = {"red", "burgundy", "orange",
    "yellow", "green", "blue", "aquamarine", "cyan", "sky blue",
    "brown", "indigo", "violet", "purple", "pink", "grey", "white",
    "black","rainbow"};
    private static int[] images = {245, 245, 240, 240, 247, 246, 248, 248, 248,
    243, 244, 244, 244, 250, 243, 242, 241, 251};
    
    // function to add randomly generated potions
    public static void addPotion(Thing t) {
        int style=RPG.r(potionstyles.length);
        int colour=RPG.r(potioncolours.length);
        
        t.set("ImageSource","Items");
        t.set("Image",images[colour]);
        t.set("UName",potionstyles[style]+" "+potioncolours[colour]+" potion");
        
        // fix up plural name if needed
        String name=t.getstring("Name");
        if (name.indexOf("potion")==0) {
            String s="potions"+name.substring(6);
            t.set("NamePlural",s);
        }
        
        Lib.add(t);
        
        Recipe.register(Skill.ALCHEMY,t,2);
    }
    
    public static Thing createPotion() {
        return createPotion(Game.level());
    }
    
    public static Thing createPotion(int level) {
        return Lib.createType("IsPotion",level);
    }
    
    public static void dip(Thing user, Thing target, Thing potion) {

    	if (potion.handles("OnDip")) {
    		// Game.warn("Dip "+target.name()+" in "+potion.name());
			Event de=new Event("Dip");
			de.set("Target",target);
			de.set("User",user);
			potion.handle(de);
		}
    }
    
    public static void drink(Thing b, Thing potion) {
        b.incStat("APS", - Being.actionCost(b));
        if (potion.getFlag("IsDrinkable")) {
            Item.identify(potion);
            Thing p=potion.remove(1);
            b.message("You drink "+p.getTheName());
            // do potion effect here
            
            if (potion.handles("OnDrunk")) {
                Event ee=new Event("Drunk");
                ee.set("Target",b);
                if (potion.handle(ee)) return;
            }
            
        } else {
            Game.warn("Potion.drink: drinking problem");
        }
    }
    
    private static class DipScript extends Script {
    	private static final long serialVersionUID = 3258133544223061043L;

        public boolean handle(Thing t, Event e) {
    		Thing user=e.getThing("User");
    		
    		Thing it=Game.selectItem("Select an item to apply the potion to:",user.getItems());
    		
    		if (it==null) {
    			return false;
    		}
    		
    		t.remove(1);
    		
    		if (it.getStat("Number")>1) {
    			it=it.separate(RPG.d(4));
    		}
    		
    		Potion.dip(user,it,t);
    		
    		return true;
    	}
    }
    
    private static class WaterDip extends Script {
    	private static final long serialVersionUID = 3688784773950550576L;

        public boolean handle(Thing t, Event e) {
    		Thing target=e.getThing("Target");
    		Item.identify(t);
    		
    		if (t.getFlag("IsBlessed")) {
    			Item.bless(target);
    		} else if (t.getFlag("IsCursed")){
    			Item.curse(target);
    		}
    		
    		return true;
    	}
    }
	
    public static void init() {
        Thing t = Lib.extend("base potion", "base item");
        t.set("IsPotion", 1);
        t.set("IsDrinkable", 1);
        t.set("IsMagicItem", 1);
        t.set("IsFragile",1);
        t.set("RES:impact",-5); // vulnerable to impact
        t.set("Image", 240);
        t.set("HPS", 2);
        t.set("LevelMin",1);
        t.set("ValueBase", 200);
        t.set("ItemWeight", 500);
        t.set("OnUse",new DipScript());
        t.set("Frequency", 60);
        t.set("ASCII","!");
        t.set("IsAlchemyIngredient",1);
       // t.set("MissileRecovery",0);
        Lib.add(t);
        
        initSpecialPotions();
        initRandomPotions();
        initSpellPotions();
    }
    
    private static void initSpellPotions() {
		ArrayList all=Spell.getSpellNames();
		
		Iterator it=all.iterator();
		while (it.hasNext()) {
			String name=(String)it.next();
			Thing s=Spell.create(name);
			
			if (s.getStat("SpellTarget")!=Spell.TARGET_SELF) {
				continue;
			}
			
			int level=s.getLevel();
			Thing t=Lib.extend("potion of "+name,"base potion");
	        t.set("NamePlural","potions of "+name);
	        t.set("LevelMin",level);
			t.set("OnDrunk",Scripts.spellEffect("Target",name,100));
		    addPotion(t);
		}
    }
	
    public static void initSpecialPotions() {
        Thing t;
        
        t=Lib.extend("potion of water","base potion");
        t.set("NamePlural","potions of water");
        t.set("UName","watery potion");
        t.set("Image", 243);
        t.set("LevelMin",1);
        t.set("OnDip",new WaterDip());
        Lib.add(t);

        t=Lib.extend("potion of dirty water","base potion");
        t.set("NamePlural","potions of dirty water");
        t.set("UName","watery potion");
        t.set("Image", 243);
        t.set("LevelMin",6);
        // sickness effect
        {	Thing effect=Lib.create("sickness");
                Script script=Scripts.addEffect("Target",effect,50);
                t.set("OnDrunk",script);}
        Lib.add(t);    
        
        t=Lib.extend("potion of holy water","potion of water");
        t.set("NamePlural","potions of holy water");
        t.set("IsBlessed",1);
        t.set("LevelMin",9);
        t.set("OnDrunk",Scripts.addEffect("Target",Lib.create("blessing")));
        Lib.add(t);
        
        t=Lib.extend("potion of unholy water","potion of water");
        t.set("NamePlural","potions of unholy water");
        t.set("LevelMin",7);
        t.set("IsCursed",1);
        t.set("OnDrunk",Scripts.addEffect("Target",Lib.create("curse")));
        Lib.add(t);
        
        t=Lib.extend("carrot juice","base potion");
        t.set("NamePlural","carrot juices");
        t.set("UName","orange potion");
        t.set("Image", 245);
        t.set("LevelMin",1);
        Lib.add(t);
    }
    
    public static class HealingScript extends Script {
        private static final long serialVersionUID = 3258130254312059186L;

        public boolean handle(Thing t, Event e) {
            Thing targ=(Thing)e.get("Target");
            if (t.getFlag("HealingPower")&&targ.getFlag("HPS")) {
                int heal=t.getStat("HealingPower");
                Being.heal(targ,heal);
            }
            if (t.getFlag("CurePoisonPower")) {
                Poison.cure(targ,t.getStat("CurePoisonPower"));
            }
            return false;
        }
    }

    public static class GainScript extends Script {
        private static final long serialVersionUID = 3688503307446268472L;

        public boolean handle(Thing t, Event e) {
            Thing targ=(Thing)e.get("Target");
            int max=t.getStat("GainMax");
            String stat=t.getstring("GainStat");
            if ((max>0)&&(targ.getBaseStat(stat)>=max)) return false;
            
            targ.incStat(stat,t.getStat("GainAmount"));
            return false;
        }
    }    
    
    public static void initRandomPotions() {
        Thing t;
        
        t=Lib.extend("healing salve","base potion");
        t.set("HealingPower",7);
        t.set("LevelMin",1);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("healing potion","base potion");
        t.set("HealingPower",13);
        t.set("LevelMin",3);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("potion of healing","base potion");
        t.set("HealingPower",20);
        t.set("LevelMin",7);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("strong healing potion","base potion");
        t.set("HealingPower",40);
        t.set("LevelMin",13);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("extra healing potion","base potion");
        t.set("HealingPower",80);
        t.set("LevelMin",20);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("ultra healing potion","base potion");
        t.set("HealingPower",125);
        t.set("LevelMin",27);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("potion of poison","base potion");
        t.set("LevelMin",4);
        // poison effect
        {	Thing effect=Lib.create("poison");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        t.set("DeathDecoration","poison cloud");
        addPotion(t);
        
        t=Lib.extend("potion of misfortune","base potion");
        t.set("LevelMin",6);
        // poison effect
        {	Thing effect=Lib.create("curse");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        t.set("DeathDecoration","cloud of misfortune");
        addPotion(t);
        
        t=Lib.extend("potion of pestilence","base potion");
        t.set("LevelMin",9);
        // poison effect
        {	Thing effect=Lib.create("pestilence");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        t.set("DeathDecoration","cloud of pestilence");
        addPotion(t);
        
        t=Lib.extend("potion of confusion","base potion");
        t.set("LevelMin",7);
        // poison effect
        {	Thing effect=Lib.create("confusion");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        t.set("DeathDecoration","cloud of confusion");
        addPotion(t);
        
        
        t=Lib.extend("potion of speed","base potion");
        t.set("LevelMin",2);
        // create speed up effect
        {	Thing effect=Effect.temporary(Modifier.bonus("MoveSpeed",100),2000);
			effect.set("EffectName","accelerated");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        addPotion(t);
        
        t=Lib.extend("potion of strength","base potion");
        t.set("LevelMin",8);
        // create bonus effect
        {	Thing effect=Effect.temporary(Modifier.bonus("ST",RPG.d(2,6)),2000);
			effect.set("EffectName","strengthened");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        addPotion(t);
        
        t=Lib.extend("potion of intelligence","base potion");
        t.set("LevelMin",6);
        // create bonus effect
        {	Thing effect=Effect.temporary(Modifier.bonus("IN",RPG.d(3,6)),2000);
 			effect.set("EffectName","brainy");
                Script script=Scripts.addEffect("Target",effect);
                t.set("OnDrunk",script);}
        addPotion(t);
        
        t=Lib.extend("potion of charisma","base potion");
        t.set("LevelMin",1);
        // create bonus effect
        {	
        	Thing effect=Effect.temporary(Modifier.bonus("CH",RPG.d(2,8)),2000);
     		effect.set("EffectName","charming");
            Script script=Scripts.addEffect("Target",effect);
            t.set("OnDrunk",script);
        }
        addPotion(t);
        
        t=Lib.extend("potion of ferocity","base potion");
        t.set("LevelMin",1);
        // create bonus effect
        {	
        	Thing effect=Effect.temporary(Modifier.bonus("AttackSpeed",RPG.d(2,30)),2000);
        	effect.set("EffectName","ferocious");
        	Script script=Scripts.addEffect("Target",effect);
            t.set("OnDrunk",script);
        }
        addPotion(t);
        
        t=Lib.extend("potion of ethereality","base potion");
        t.set("LevelMin",15);
        // create bonus effect
        {	
        	Script script=Scripts.addEffect("Target","ethereality");
            t.set("OnDrunk",script);
        }
        addPotion(t);
        
        t=Lib.extend("potion of survival","base potion");
        t.set("LevelMin",7);
        // create bonus effect
        {	
        	Thing effect=Effect.temporary(Modifier.linear("IsImmortal",0,1),1000);
            effect.set("EffectName","staying alive");
        	Script script=Scripts.addEffect("Target",effect);
            t.set("OnDrunk",script);
        }
        addPotion(t);
        
        t=Lib.extend("potion of insanity","base potion");
        t.set("LevelMin",11);
        // create bonus effect
        {	
        	Thing effect=Effect.temporary(Modifier.linear("IsInsane",0,1),30000);
            effect.set("EffectName","slghtly mad");
            effect.set("ResistStat","IN");
            effect.set("ResistDifficulty",10);
            effect.set("ResistMessage","You feel on the edge of sanity");
            effect.set("AddAttributeMessage","You're feeling slightly mad");
            Script script=Scripts.addEffect("Target",effect);
            t.set("OnDrunk",script);
        }
        addPotion(t);
       
        t=Lib.extend("potion of minor healing","base potion");
        t.set("HealingPower",3);
        t.set("LevelMin",6);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("potion of resilience","base potion");
        t.set("GainStat","HPSMAX");
        t.set("GainAmount",1);
        t.set("OnDrunk",new GainScript());
        t.set("LevelMin",1);
        addPotion(t);
        
        t=Lib.extend("potion of swift movement","base potion");
        t.set("GainStat","MoveSpeed");
        t.set("GainAmount",RPG.d(3));
        t.set("OnDrunk",new GainScript());
        t.set("LevelMin",10);
        addPotion(t);
        
        t=Lib.extend("potion of aggressive tendencies","base potion");
        t.set("GainStat","AttackSpeed");
        t.set("GainAmount",1);
        t.set("OnDrunk",new GainScript());
        t.set("LevelMin",15);
        addPotion(t);
        
        t=Lib.extend("potion of fate","base potion");
        t.set("GainStat",RPG.ST_PEITY);
        t.set("GainAmount",1);
        t.set("GainMax",100);
        t.set("OnDrunk",new GainScript());
        t.set("LevelMin",20);
        t.set("IsWishable",0); // can't wish for this!
        addPotion(t);
        
        t=Lib.extend("potion of cure poison","base potion");
        t.set("CurePoisonPower",15);
        t.set("LevelMin",1);
        t.set("OnDrunk",new HealingScript());
        addPotion(t);
        
        t=Lib.extend("potion of literacy","base potion");
        t.set("GainStat",Skill.LITERACY);
        t.set("GainAmount",1);
        t.set("GainMax",1);
        t.set("OnDrunk",new GainScript());
        t.set("LevelMin",10);
        addPotion(t);

        t=Lib.extend("potion of repair","base potion");
        t.set("LevelMin",3);
        t.addHandler("OnDip",new Script() {
        	private static final long serialVersionUID = 3257286915873911348L;

            public boolean handle(Thing t, Event e) {
        		Thing tt=e.getThing("Target");
        		if (t.getFlag("IsCursed")) {
        			Game.messageTyrant("The potion seems to damage "+tt.getYourName());
        			Damage.inflict(tt,tt.getStat("HPSMAX")/2,RPG.DT_SPECIAL);
        		} else {
        			if (Item.repair(tt,false)) {
        				Game.messageTyrant(tt.getYourName()+" is surrounded by a shining orange light");	
        			} else {
        				Game.messageTyrant(tt.getYourName()+" glows orange for a second");	
        			}
        		}
        		return false;
        	}
        });
        addPotion(t);

        t=Lib.extend("potion of perfect repair","base potion");
        t.set("LevelMin",23);
        t.multiplyStat("Frequency",0.3);
        t.addHandler("OnDip",new Script() {
        	private static final long serialVersionUID = 3906372640363917361L;

            public boolean handle(Thing t, Event e) {
        		Thing tt=e.getThing("Target");
        		if (t.getFlag("IsCursed")) {
        			Game.messageTyrant(tt.getYourName()+" is attacked by black magic!");
        			Damage.inflict(tt,tt.getStat("HPSMAX"),RPG.DT_SPECIAL);
        		} else {        		
        			if (Item.repair(tt,true)) {
        				Game.messageTyrant(tt.getYourName()+" is surrounded by a shining orange light");	
        			} else {
        				Game.messageTyrant(tt.getYourName()+" glows yellow for a second");	
        			}
        		}
        		return false;
        	}
        });
        addPotion(t);
        
        t=Lib.extend("potion of hunger","base potion");
        t.set("OnDrunk",new Script() {
        	private static final long serialVersionUID = 3257290244507383856L;

            public boolean handle(Thing t, Event e) {
        		Thing user=e.getThing("Target");
        		int h=user.getStat(RPG.ST_HUNGER);
        		int ht=user.getStat(RPG.ST_HUNGERTHRESHOLD);
        		
        		h=RPG.min(h,ht*(t.getFlag("IsCursed")?3:2));
        		
        		user.message("You feel hunger gnawing at your stomach");
        		return false;
        	}
        });
        t.set("IsCursed",RPG.r(2));
        t.set("LevelMin",8);
        Lib.add(t);
        
        t=Lib.extend("potion of unlearning","base potion");
        t.set("OnDrunk",new Script() {
        	private static final long serialVersionUID = 3833188016825840952L;

            public boolean handle(Thing t, Event e) {
        		Thing user=e.getThing("Target");
        		ArrayList skills=Skill.getList(user);
         		
        		String s=null;
        		
        		if (skills.size()>0) {
        			s=Skill.trim((String)skills.get(RPG.r(skills.size())));
        		}
        		
         		if ((s!=null)&&(user.getBaseStat(s)>0)) {
         			user.incStat(s,-1);
         			user.message("You seem to remember less about "+s);
         			
         			// alter skill points based on status
         			if (t.getFlag("IsCursed")) {
         				user.incStat("SkillPoints",-1);
         			} else if (t.getFlag("IsBlessed")||RPG.d(2)==1) {
         				if (RPG.d(6)>1) user.incStat("SkillPoints",1);
         			}
         		} else {
         			user.message("You feel pretty stupid");
         		}
         		
        		return false;
        	}
        });
        t.set("LevelMin",13);
        addPotion(t);

        t=Lib.extend("potion of minor talent","base potion");
        t.set("LevelMin",1);
        t.set("OnDrunk",Scripts.statGain("Target","SK,ST,AG,TG,IN,WP,CH,CR",12));
        addPotion(t);
        
        t=Lib.extend("potion of talent","base potion");
        t.set("LevelMin",10);
        t.set("OnDrunk",Scripts.statGain("Target","SK,ST,AG,TG,IN,WP,CH,CR",30));
        addPotion(t);
        
        t=Lib.extend("potion of greater talent","base potion");
        t.set("LevelMin",20);
        t.set("OnDrunk",Scripts.statGain("Target","SK,ST,AG,TG,IN,WP,CH,CR",50));
        addPotion(t);
        
        t=Lib.extend("potion of supreme talent","base potion");
        t.set("LevelMin",32);
        t.set("OnDrunk",Scripts.statGain("Target","SK,ST,AG,TG,IN,WP,CH,CR",90));
        addPotion(t);
        
        t=Lib.extend("potion of legendary talent","base potion");
        t.set("LevelMin",42);
        t.set("OnDrunk",Scripts.statGain("Target","SK,ST,AG,TG,IN,WP,CH,CR",170));
        addPotion(t);
        
        t=Lib.extend("potion of extra curing","potion of cure poison");
        t.set("CurePoisonPower",60);
        addPotion(t);
    }
}