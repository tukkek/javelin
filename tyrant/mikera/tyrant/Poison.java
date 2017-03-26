package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

public class Poison {
    
    public Thing create(int damage) {
        Thing t=Lib.create("poison");
        t.set("Damage",damage);
        return t;
    }
    
    public static class PoisonAction extends Script {
        private static final long serialVersionUID = 3258410638316287027L;

        public boolean handle(Thing t, Event e) {
            int time=e.getStat("Time");
            
            // set actor
            Game.actor=t;
            
            //Game.warn("**poison action**");
            Thing h=t.holder();
            
            int dam=0;
            int hits=RPG.po(time*t.getStat("Strength"),1000000);
            for (int i=0; i<hits; i++) {
                dam+=Damage.inflict(h,t.getStat("Damage"),t.getstring("DamageType"));
                //Game.warn("**poison damage**");
            }
            if (dam>0) h.message(t.getstring("DamageMessage"));
            
            // might be other effects....
            return false;
        }
    }
    
    public static void init() {
        Thing t;
        
        t=Lib.extend("base poison","temporary effect");
        t.set("IsActive",1);
        t.addHandler("OnAction",new PoisonAction());
        t.set("IsPoison",1);
        t.set("DamageType","poison");
        t.set("EffectName","poisoned");
        t.set("ResistStat","TG");
        t.set("ResistMessage",null);
        t.set("ResistDifficulty",10);
        t.set("CureDifficulty",5);
        Lib.add(t);
        
        t=Lib.extend("weakening poison","base poison");
        t.set("LifeTime",50000);
        t.set("EffectName","poisoned");
        t.set("Strength",100);
        t.set("Damage",2);
        t.set("DamageType","poison");
        t.set("DamageMessage","You feel sick...");
        t.set("ResistMessage","You manage to shake off a feeling of weakness");
        t.set("AttributeAddMessage","You feel weakened!");
        t.add("CarriedModifiers",Modifier.linear("ST",90,0));
        Lib.add(t);    
        
        t=Lib.extend("poison","base poison");
        t.set("LifeTime",20000);
        t.set("Strength",200);
        t.set("Damage",3);
        t.set("DamageType","poison");
        t.set("ResistMessage","You feel queasy for a moment");
        t.set("DamageMessage","You feel the effects of poison...");
        t.set("AttributeAddMessage","You feel poisoned!");
        Lib.add(t);

        t=Lib.extend("strong poison","base poison");
        t.set("LifeTime",30000);
        t.set("Strength",300);
        t.set("Damage",6);
        t.set("DamageType","poison");
        t.set("ResistMessage","You feel very queasy for a moment");
        t.set("DamageMessage","You feel the poison weakening you...");
        t.set("AttributeAddMessage","You feel badly poisoned!");
        t.set("CureDifficulty",15);
        Lib.add(t);   
        
        t=Lib.extend("deadly poison","base poison");
        t.set("LifeTime",40000);
        t.set("Strength",2000);
        t.set("Damage",10);
        t.set("DamageType","poison");
        t.set("ResistMessage","You feel very queasy for a moment");
        t.set("DamageMessage","You feel the poison weakening you...");
        t.set("AttributeAddMessage","You feel badly poisoned!");
        t.set("CureDifficulty",40);
        Lib.add(t);   
        
        t=Lib.extend("extreme poison","base poison");
        t.set("LifeTime",50000);
        t.set("Strength",6000);
        t.set("Damage",15);
        t.set("DamageType","poison");
        t.set("ResistMessage","You feel very queasy for a moment");
        t.set("DamageMessage","You feel the poison weakening you...");
        t.set("AttributeAddMessage","You feel badly poisoned!");
        t.set("CureDifficulty",100);
        Lib.add(t);  
        
        t=Lib.extend("ultimate poison","base poison");
        t.set("LifeTime",100000);
        t.set("Strength",12000);
        t.set("Damage",30);
        t.set("DamageType","poison");
        t.set("ResistMessage","You feel very queasy for a moment");
        t.set("DamageMessage","You feel the poison weakening you...");
        t.set("AttributeAddMessage","You feel badly poisoned!");
        t.set("CureDifficulty",300);
        Lib.add(t);  
        
        t=Lib.extend("sickness","base poison");
        t.set("LifeTime",50000);
        t.set("Strength",100);
        t.set("Damage",2);
        t.set("DamageType","poison");
        t.set("DamageMessage","You feel sick...");
        t.set("ResistMessage","You manage to shake off a feeling of illness");
        t.set("AttributeAddMessage","You feel sick!");
        t.add("CarriedModifiers",Modifier.bonus("ST",-2));
        t.add("CarriedModifiers",Modifier.bonus("SK",-2));
        t.set("CureDifficulty",15);
        Lib.add(t);        
        
        t=Lib.extend("pestilence","poison");
        t.set("LifeTime",120000);
        t.set("Strength",200);
        t.set("Damage",4);
        t.set("EffectName","pestilent");
        t.set("DamageType","poison");
        t.set("DamageMessage","You feel grotty...");
        t.set("AttributeAddMessage","You feel the touch of pestilence!");
        t.set("ResistDifficulty",20);
        t.addHandler("OnAction",Scripts.generator("fly swarm",100));
        t.set("CureDifficulty",25);
        Lib.add(t);
        
        t=Lib.extend("plague","poison");
        t.set("LifeTime",100000);
        t.set("Strength",200);
        t.set("Damage",8);
        t.set("EffectName","plague");
        t.set("DamageType","poison");
        t.set("DamageMessage","You feel sick...");
        t.set("AttributeAddMessage","You feel the touch of the plague!");
        t.set("ResistDifficulty",20);
        t.add("CarriedModifiers",Modifier.linear("CH",100,-10));
        t.addHandler("OnAction",Scripts.generator("plague cloud",30));
        t.set("CureDifficulty",100);
        Lib.add(t);
        

    }
    
    public static void cure(Thing t, int power) {
        Thing[] poisons=t.getFlaggedContents("IsPoison");
        boolean cured=false;
        for (int i=0; i<poisons.length; i++) {
            Thing p=poisons[i];
			Game.warn("Poison.cure(): "+power);
            if (RPG.test(power,poisons.length*p.getStat("CureDifficulty"))) {
                p.remove();
                cured=true;
            }
        }
        if (cured) {
            t.message("You feel refreshed");
        }
    }
}