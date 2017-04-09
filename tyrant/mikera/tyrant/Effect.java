package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.Thing;

// attributes are added to inventory of mobile
// grant special abilities, modifiers, duration effects etc.
public class Effect {
    
    public static void init() {
        Thing t=Lib.extend("base effect","base thing");
        t.set("IsEffect",1);
        t.set("NoStack",1);
        t.set("ImageSource","Effects");
        t.set("Image",45);
        t.set("LevelMin",1);
        t.set("EffectName","effect");
        Lib.add(t);
        
        t=Lib.extend("temporary effect","base effect");
        t.set("IsTemporaryEffect",1);
        t.set("IsActive",1);
        t.addHandler("OnAction",Scripts.decay(true));
        Lib.add(t);
        
        Poison.init();
        initCurses();
        initBlessings();
        initBadEffects();
    }
    
    private static void initCurses() {
    	Thing t;
    	
    	t=Lib.extend("base curse","temporary effect");
    	t.set("IsCurse",1);
        t.set("EffectName","cursed");
        t.set("AttributeAddMessage","You feel like this is going to be a bad day");
    	Lib.add(t);
    	
    	t=Lib.extend("curse","base curse");
        t.set("LifeTime",10000);
        t.additem("CarriedModifiers",Modifier.linear("Luck",100,-50));
    	Lib.add(t);
    	
    	t=Lib.extend("hex","base curse");
        t.set("LifeTime",3000);
        t.additem("CarriedModifiers",Modifier.linear("Luck",100,-100));
        t.set("AttributeAddMessage","You feel like this is going to be a terrible day");
    	Lib.add(t);
    	
    	t=Lib.extend("curse of doom","base curse");
        t.set("LifeTime",30000);
        t.set("EffectName","doomed");
        t.additem("CarriedModifiers",Modifier.linear("Luck",100,-150));
    	Lib.add(t);
    	
    	t=Lib.extend("curse of blindness","base curse");
        t.set("DecayRate",100);
        t.set("EffectName","blinded");
        t.additem("CarriedModifiers",Modifier.linear("IsBlind",0,1));
        t.set("AttributeAddMessage","You are blinded!");
    	Lib.add(t);
    }

    private static void initBadEffects() {
    	Thing t;
    
    	t=Lib.extend("base bad effect","temporary effect");
    	t.set("IsBadEffect",1);
        t.set("EffectName","bad effect");
    	Lib.add(t);

        t=Lib.extend("confusion","base bad effect");
        t.set("IsActive",20000);
        t.set("LifeTime",2000);
        t.set("ResistStat","WP");
        t.set("ResistDifficulty",30);
        t.set("EffectName","confused");
        t.set("AttributeAddMessage","You feel very confused!");
        t.set("ResistMessage","You feel dizzy but manage to stay clear-headed");
        t.additem("CarriedModifiers",Modifier.linear("IsConfused",0,1));
        Lib.add(t);
        
    	t=Lib.extend("slow","base bad effect");
        t.set("LifeTime",3000);
    	t.set("EffectName","slowed");
    	t.set("CancelEffect","haste");
        t.additem("CarriedModifiers",Modifier.linear("Speed",66,0));
    	Lib.add(t);
    	
    	t=Lib.extend("web","base bad effect");
    	t.set("LifeTime",4000);
    	t.set("EffectName","webbed");
        t.additem("CarriedModifiers",Modifier.linear("MoveSpeed",80,0));
        t.additem("CarriedModifiers",Modifier.linear("AttackSpeed",80,0));
        t.additem("CarriedModifiers",Modifier.linear("AG",50,0));
        t.additem("CarriedModifiers",Modifier.linear("SK",75,0));
    	Lib.add(t);
    }

    
    private static void initBlessings() {
    	Thing t;
    	
    	t=Lib.extend("base blessing","temporary effect");
    	t.set("IsBlessing",1);
    	t.set("IsActive",1);
        t.set("EffectName","blessed");
    	Lib.add(t);    	
    	
    	t=Lib.extend("blessing","base blessing");
        t.set("LifeTime",5000);
        t.additem("CarriedModifiers",Modifier.linear("Luck",100,30));
    	Lib.add(t);
    	
    	t=Lib.extend("stone skin","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","stone skinned");
        t.additem("CarriedModifiers",Modifier.bonus("ARM",30));
        t.set("AttributeAddMessage","Your skin seems to harden");
    	Lib.add(t);
    	
    	t=Lib.extend("fire protection","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","fire protected");
        t.additem("CarriedModifiers",Modifier.bonus("ARM:fire",30));
        t.additem("CarriedModifiers",Modifier.bonus("RES:fire",15));
        t.set("AttributeAddMessage","Your blood starts to boil");
    	Lib.add(t);
    	
    	t=Lib.extend("ice protection","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","ice protected");
        t.additem("CarriedModifiers",Modifier.bonus("ARM:ice",30));
        t.additem("CarriedModifiers",Modifier.bonus("RES:ice",15));
        t.set("AttributeAddMessage","You feel seriously cool");
    	Lib.add(t);
    	
    	t=Lib.extend("poison resistance","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","poison protected");
        t.additem("CarriedModifiers",Modifier.bonus("RES:poison",20));
        t.set("AttributeAddMessage","You feel fit and healthy");
    	Lib.add(t);
    	
    	t=Lib.extend("haste","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","hasted");
    	t.set("CancelEffect","slow");
        t.additem("CarriedModifiers",Modifier.linear("Speed",100,80));
        t.set("AttributeAddMessage","You feel very energetic");
    	Lib.add(t);
    	
    	t=Lib.extend("accelerate","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","accelerated");
    	t.set("CancelEffect","slow");
        t.additem("CarriedModifiers",Modifier.linear("MoveSpeed",150,0));
        t.set("AttributeAddMessage","You feel like running around");
    	Lib.add(t);
    	
    	t=Lib.extend("berserk","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","berserk");
    	t.set("CancelEffect","calm");
        t.additem("CarriedModifiers",Modifier.bonus(Skill.ATTACK,2));
        t.additem("CarriedModifiers",Modifier.bonus(Skill.FEROCITY,1));
		t.additem("CarriedModifiers",Modifier.linear("Defence",50,-1));
        t.additem("CarriedModifiers",Modifier.constant("IsBerserk",1));
        t.set("AttributeAddMessage","You are filled with rage!");
        Lib.add(t);
    	
    	t=Lib.extend("calm","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","calm");
    	t.set("CancelEffect","berserk");
        t.additem("CarriedModifiers",Modifier.linear("CH",110,3));
        t.additem("CarriedModifiers",Modifier.linear("IN",110,3));
        t.set("AttributeAddMessage","You feel calm");
    	Lib.add(t);
    	
    	t=Lib.extend("fearsome","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","fearsome");
     	t.additem("CarriedModifiers",Modifier.linear("FearFactor",100,2));
        t.additem("CarriedModifiers",Modifier.linear("CH",60,0));
        t.set("AttributeAddMessage","You feel the will to dominate weaker beings");
        Lib.add(t);
        
    	t=Lib.extend("ethereality","base blessing");
        t.set("LifeTime",1000);
    	t.set("EffectName","ethereal");
     	t.additem("CarriedModifiers",Modifier.linear("IsEthereal",0,1));
        t.set("AttributeAddMessage","You feel that you are disconnected from the world");
        Lib.add(t);
        
    	t=Lib.extend("flight","base blessing");
        t.set("LifeTime",5000);
    	t.set("EffectName","flying");
     	t.additem("CarriedModifiers",Modifier.linear("IsFlying",0,1));
        t.set("AttributeAddMessage","You start to fly");
        Lib.add(t);
    }
    
    public static Thing temporary(Modifier m, int time) {
        Thing a=Lib.create("temporary effect");
        a.set("LifeTime",time);
        a.additem("CarriedModifiers",m);
        return a;
    }
}