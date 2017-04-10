package tyrant.mikera.tyrant;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

// A ring which can be worn by the hero on either the right or left hand
// this may confer a number of possible advantages/disadvantages
//

public class Ring  {
    private static String[] types = {"gold", "silver", "ruby","sapphire","emerald","skull","moon","diamond","crystal","brass","black skull"};
    private static int[]   images = {  200,  201,      202,   203,       204,      205,    206,   207,      208,      209,    210};
    
    private static void addRing(Thing t) {
        int type=RPG.r(types.length);
        
        t.set("ImageSource","Items");
        t.set("Image",images[type]);
        t.set("UName",types[type]+" ring");
        
        if (types[type].equals("black skull")) {
        	t.set("IsCursed",1);
        }
        
        // fix up plural name if needed
        String name=t.getstring("Name");
        if (name.indexOf("ring")==0) {
            String s="rings"+name.substring(4);
            t.set("NamePlural",s);
        }
        
        Lib.add(t);
    }
    
    public static void init() {
        Thing t = Lib.extend("plain ring", "base item");
        t.set("IsRing", 1);
        t.set("WieldType", RPG.WT_LEFTRING);
        t.set("IsMagicItem", 1);
        t.set("Image", 200);
        t.set("HPS", 36);
        t.set("ItemWeight", 100);
        t.set("ValueBase",500);
        t.set("LevelMin", 1);
        t.set("Frequency", 30);
        t.set("ASCII","=");
        addRing(t);
        
        initBaseRings();
        initRings();
    }
    
    public static void initBaseRings() {
        Thing t;
        
        t=Lib.extend("gold ring","plain ring");
        t.set("Image",200);
        t.set("ValueBase",1200);
        t.set("ItemWeight", 120);
        Lib.add(t);
        
        t=Lib.extend("silver ring","plain ring");
        t.set("Image",201);
        t.set("ValueBase",300);
        t.set("ItemWeight", 120);
        Lib.add(t);

        t=Lib.extend("ornate mithril ring","silver ring");
        t.set("ValueBase",400);
        Lib.add(t);

    }
        
    public static void initRings() {
        Thing t;
        
        // TODO - more rings!
        
        t = Lib.extend("ring of speed", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("MoveSpeed",RPG.d(40)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of strength", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("ST",RPG.d(4)));
        t.set("LevelMin",1);
        addRing(t);
        
        t = Lib.extend("ring of skill", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("SK",RPG.d(4)));
        t.set("LevelMin",1);
        addRing(t);
        
        t = Lib.extend("ring of agility", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("AG",RPG.d(2,4)));
        t.set("LevelMin",10);
        addRing(t);
        
        t = Lib.extend("ring of toughness", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("TG",RPG.d(2,4)));
        t.set("LevelMin",15);
        addRing(t);
        
        t = Lib.extend("ring of intelligence", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("IN",RPG.d(4)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of willpower", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("WP",RPG.d(2,4)));
        t.set("LevelMin",15);
        addRing(t);
        
        t = Lib.extend("ring of charisma", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("CH",RPG.d(2,4)));
        t.set("LevelMin",1);
        addRing(t);
        
        t = Lib.extend("ring of craft", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("CR",RPG.d(2,4)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of protection", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("ARM",RPG.d(3,4)));
        t.set("LevelMin",3);
        addRing(t);
        
        t = Lib.extend("ring of reading", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.LITERACY,RPG.d(3)));
        t.set("LevelMin",8);
        addRing(t);
        
        t = Lib.extend("ring of the great trader", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.TRADING,RPG.d(2,3)));
        t.set("LevelMin",10);
        addRing(t);
 
        t = Lib.extend("ring of thieves", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.DODGE,RPG.r(2)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.STEALTH,RPG.r(2)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.PICKLOCK,RPG.r(2)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.PICKPOCKET,RPG.r(2)));
        t.set("LevelMin",15);
        addRing(t);
		
        t = Lib.extend("ring of the warrior", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.ATTACK,RPG.r(3)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.DEFENCE,RPG.r(3)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.ARCHERY,RPG.r(3)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.THROWING,RPG.r(3)));
        t.set("LevelMin",25);
        addRing(t);
       
        t = Lib.extend("ring of armour reduction", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("ARM",-1));
        t.set("IsCursed",1);
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of short sight", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear("VisionRange",60,0));
        t.set("IsCursed",1);
        t.set("LevelMin",1);
        addRing(t);
		
        t = Lib.extend("ring of long sight", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear("VisionRange",100,2));
        t.set("LevelMin",3);
        addRing(t);
		
        t = Lib.extend("ring of far sight", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear("VisionRange",150,2));
        t.set("LevelMin",23);
        addRing(t);
        
        t = Lib.extend("ring of fire resistance", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("RES:fire",RPG.d(2,6)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of ice resistance", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("RES:ice",RPG.d(2,6)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of poison resistance", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("RES:poison",RPG.d(2,6)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of antivenom", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("RES:poison",RPG.d(2,8)));
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of ferocity", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("AttackSpeed",RPG.d(40)));
        t.additem("WieldedModifiers",Modifier.bonus(Skill.ATTACK,1));
        t.set("LevelMin",13);
        addRing(t);
        
        t = Lib.extend("ring of survival", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.SURVIVAL,RPG.d(3)));
        t.set("LevelMin",4);
        addRing(t);
        
        t = Lib.extend("ring of throwing", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.THROWING,RPG.d(3)));
        t.set("LevelMin",7);
        addRing(t);
        
        t = Lib.extend("ring of stealth", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(Skill.STEALTH,RPG.d(2)));
        t.set("LevelMin",22);
        addRing(t);
        
        t = Lib.extend("wedding ring", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("CH",RPG.r(3)));
        t.set("LevelMin",9);
        addRing(t);
        
        t = Lib.extend("ring of ugliness", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("CH",-RPG.d(2,6)));
        t.set("IsCursed",1);
        t.set("LevelMin",2);
        addRing(t);
        
        t = Lib.extend("ring of hunger", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear(RPG.ST_HUNGERTHRESHOLD,50,0));
        t.set("IsCursed",1);
        t.set("LevelMin",6);
        addRing(t);
        
        t = Lib.extend("ring of starvation", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear(RPG.ST_HUNGERTHRESHOLD,25,0));
        t.set("IsCursed",1);
        t.set("LevelMin",15);
        addRing(t);
        
        t = Lib.extend("ring of slow digestion", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear(RPG.ST_HUNGERTHRESHOLD,150,0));
        t.set("LevelMin",14);
        addRing(t);
        
        t = Lib.extend("ring of divine sustenance", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear(RPG.ST_HUNGERTHRESHOLD,300,0));
        t.set("LevelMin",22);
        addRing(t);
        
        t = Lib.extend("ring of slowness", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("MoveSpeed",-RPG.d(2,20)));
        t.additem("WieldedModifiers",Modifier.bonus(RPG.ST_AG,-RPG.d(4)));
        t.set("IsCursed",1);
        t.set("LevelMin",5);
        addRing(t);
        
        t = Lib.extend("ring of weakness", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus(RPG.ST_ST,-RPG.d(4)));
        t.additem("WieldedModifiers",Modifier.bonus(RPG.ST_TG,-RPG.d(4)));
        t.set("IsCursed",1);
        t.set("LevelMin",9);
        addRing(t);
        
        t = Lib.extend("ring of weak will", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear(RPG.ST_WP,50,0));
        t.set("IsCursed",1);
        t.set("LevelMin",12);
        addRing(t);
        
        t = Lib.extend("ring of fortune", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("Luck",30));
        t.set("LevelMin",1);
        addRing(t);
        
        t = Lib.extend("ring of great fortune", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("Luck",50));
        t.set("LevelMin",26);
        addRing(t);
        
        t = Lib.extend("ring of misfortune", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("Luck",-50));
        t.set("IsCursed",1);
        t.set("LevelMin",12);
        addRing(t);
        
        t = Lib.extend("ring of doom", "plain ring");
        t.additem("WieldedModifiers",Modifier.bonus("Luck",-100));
        t.set("IsCursed",1);
        t.set("LevelMin",27);
        addRing(t);
        
        t = Lib.extend("ring of prevent blindness", "plain ring");
        Modifier blm=Modifier.linear("IsBlind",0,0);
        blm.incStat("Priority",10);
        t.additem("WieldedModifiers",blm);
        t.set("LevelMin",6);
        addRing(t);
        
        t = Lib.extend("ring of blindness", "plain ring");
        t.additem("WieldedModifiers",Modifier.linear("IsBlind",0,1));
        t.set("IsCursed",1);
        t.set("LevelMin",25);
        addRing(t);
    }
}