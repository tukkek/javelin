package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

public class Food {
    
    public static Thing createFood(int level) {
        return Lib.createType("IsFood",level);
    }
    
    public static boolean isVegetarian(Thing food) {
    	return food.getFlag("IsVegetarianFood");
    }
    
    public static void eat(Thing user, Thing t) {
        if (!t.getFlag("IsEdible")) {
            if (user.isHero()) Game.messageTyrant("You can't eat that!");
            return;
        }
        
        // separate out a single food item
        t=t.separate(1);
        
        // nutrition for hero only!
        Thing h = Game.hero();
        if (user != h)
            return;
 
        h.incStat("APS", -Being.actionCost(h));
        boolean hungerAlreadySet = false;
        int currentHunger = h.getStat(RPG.ST_HUNGER);
        int nutritionValue = nutritionValue(t, h);
        // check whether hero is too bloated to eat the food....
        if ((currentHunger - nutritionValue(t, h) / 2) < -h.getStat(RPG.ST_HUNGERTHRESHOLD)) {
            //CBG If so only eat part of it, since being over full effects encumbrance only eat up to satiated.
            if(currentHunger > 0) {
                hungerAlreadySet = true;
                int toEat = Math.min(currentHunger, nutritionValue);
                t.incStat("FoodValue", -toEat);
                if(!t.getFlag("IsCursed"))
                    h.incStat(RPG.ST_HUNGER, -toEat);
            }
        	h.message("You eat part of " + t.getTheName());
        } else {
            h.message("You eat " + t.getTheName());
        }        
        
        if (t.handles("OnEaten")) {
			// Game.warn("OnEaten for "+t.name());
            Event ee=new Event("Eaten");
            ee.set("Target",user);
            
            //CBG?? do we care if it was handled or not? doesn't it just execute and we don't care about what it did?
            // We don't really care right now...
            // but in long run might want a strange effect to 
            // to be handled - in which case we break.
            if (t.handle(ee)) return;
            
            // remove the "OnEaten" property
            // so that it only works once
            t.remove("OnEaten");
        }
        
        // default food handling
        int hunger = h.getStat(RPG.ST_HUNGER);
        
        if (t.getFlag("IsCursed")) {
            Game.messageTyrant("That tasted pretty foul!");
        } else {
            if(!hungerAlreadySet) {
                hunger = RPG.max(-h.getStat(RPG.ST_HUNGERTHRESHOLD), hunger - nutritionValue);
                t.incStat("FoodValue", -nutritionValue);
                h.set(RPG.ST_HUNGER, hunger);
            }
            Game.messageTyrant("Yum yum!  You feel "+Hero.hungerString(h));
            // RPG.percentile(hunger, h.getStat("HungerThreshold")) + "%.");
        }
        if(nutritionValue(t,h) <= 0)
            t = t.remove(1);
    }
    
    public static int nutritionValue(Thing t, Thing h) {
        int foodLeft = t.getStatIfAbsent("FoodValue", -1);
        if(foodLeft == -1) {
            foodLeft = pureNutrition(t, h);
            t.set("FoodValue", foodLeft);
        }
    	return foodLeft;
    }

    private static int pureNutrition(Thing t, Thing h) {
        double nut= t.getStat("ItemWeight") * t.getStat("Nutrition");
        if (t.getFlag("IsBlessed")) nut=nut*2;
        nut=nut*(1+0.1*h.getStat(Skill.SURVIVAL));
        return (int)nut;
    }
    
    public static void init() {
        Thing t;
        
        t = Lib.extend("base food", "base item");
        t.set("Z", Thing.Z_ITEM-1);
        t.set("IsEdible", 1);
        t.set("Image", 226);
        t.set("IsFood", 1);
        t.set("ValueBase", 10);
        t.set("HPS", 4);
        t.set("Nutrition", 20);
        t.set("IsVegetarianFood",1);
        t.set("ItemWeight", 1000);
        t.set("Frequency", 100);
        t.set("ASCII","%");
        Lib.add(t);
        
        initFood();
        initFruit();
        initBerries();
        initStoneFruit();
        initMeat();
        initPoultry();
        initFish();
        initPies();
        initMushrooms();
        initBread();
        initEggs();
        initVegetables();
        initHerbs();
        initMonsterParts();
        initSkulls();
    }
        
    private static void addFood(Thing t) {
    	Lib.add(t);
    }
    

    
    private static void initMonsterParts() {
    	Thing t=Lib.extend("base monster part","base food");
    	t.set("IsMonsterPart",1);
    	t.set("IsMeat",1);
    	t.set("Image",481);
    	t.set("Frequency",20);
    	t.set("Nutrition",80);
    	t.set("ItemWeight",400);
    	t.set("IsBlackIngredient",1);
    	t.set("IsVegetarianFood",0);
    	t.set("ValueBase",50);
    	Lib.add(t);
 
    	t=Lib.extend("mouse tail","base monster part");
    	t.set("LevelMin",1);
    	t.set("ItemWeight",400);
    	Lib.add(t);
    	
    	t=Lib.extend("rat tail","base monster part");
    	t.set("LevelMin",2);
    	Lib.add(t);
    	
    	t=Lib.extend("frog leg","base monster part");
    	t.set("LevelMin",5);
        t.addHandler("OnEaten",Scripts.statGain("Target","AG",10,50));
    	Lib.add(t);
    	
    	t=Lib.extend("toad leg","base monster part");
    	t.set("LevelMin",5);
        t.addHandler("OnEaten",Scripts.statGain("Target","WP",20,50));
    	Lib.add(t);
    	
    	t=Lib.extend("snake skin","base monster part");
    	t.set("LevelMin",3);
        t.addHandler("OnEaten",Scripts.statGain("Target","CH",10,20));
    	Lib.add(t);
    	
    	t=Lib.extend("cat whisker","base monster part");
    	t.set("LevelMin",4);
    	Lib.add(t);
    	
    	t=Lib.extend("red snake skin","base monster part");
    	t.set("LevelMin",7);
    	Lib.add(t);
    	

    	t=Lib.extend("scorpion tail","base monster part");
    	t.set("LevelMin",9);
        t.addHandler("OnEaten",Scripts.statGain("Target","TG",25,50));
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("strong poison")));
    	Lib.add(t);
    	
    	t=Lib.extend("base feather","base monster part");
    	Lib.add(t);
    	
    	t=Lib.extend("kestrel feather","base feather");
        t.addHandler("OnEaten",Scripts.statGain("Target","AG",10,30));
    	t.set("LevelMin",3);
    	Lib.add(t);
    	
    	t=Lib.extend("hawk feather","base feather");
        t.addHandler("OnEaten",Scripts.statGain("Target","AG",20,30));
    	t.set("LevelMin",7);
    	Lib.add(t);
    	
    	t=Lib.extend("vulture feather","base feather");
        t.addHandler("OnEaten",Scripts.statGain("Target","SK",20,100));
    	t.set("LevelMin",13);
    	Lib.add(t);
    	
    	t=Lib.extend("eagle feather","base feather");
        t.addHandler("OnEaten",Scripts.statGain("Target","SK",30,100));
    	t.set("LevelMin",17);
    	Lib.add(t);
    	
    	t=Lib.extend("bear paw","base feather");
    	t.set("LevelMin",18);
        t.addHandler("OnEaten",Scripts.statGain("Target","ST",30,100));
    	Lib.add(t);
    	
    	t=Lib.extend("phoenix feather","base feather");
    	t.set("LevelMin",37);
    	Lib.add(t);
    
        initTeeth();
        initDeadInsects();
    }
    
    private static void initDeadInsects() {
        
        Thing t = Lib.extend("base dead insect","base monster part");
        t.set("ValueBase",0);
        t.set("IsStoreItem",0); // not for sale
        t.set("IsVegetarianFood",0);
        t.set("Nutrition",20);
        Lib.add(t);

        t = Lib.extend("dead bug", "base dead insect");
        t.set("Nutrition",10);
        t.set("LevelMin",1);
        Lib.add(t);

        t = Lib.extend("squished bug", "base dead insect");
        t.set("Nutrition",3);
        t.set("LevelMin",1);
        Lib.add(t);

        t = Lib.extend("squished roach", "base dead insect");
        t.set("Nutrition",3);
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("dead roach", "base dead insect");
        t.set("LevelMin",7);
        Lib.add(t);

        t = Lib.extend("dead beetle", "base dead insect");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("dead scorpion", "base dead insect");
        t.set("LevelMin",9);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("strong poison")));
        Lib.add(t);

        t = Lib.extend("dead fly", "base dead insect");
        t.set("Nutrition",1);
        t.set("ItemWeight", 4);
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("dead bee", "base dead insect");
        t.set("Nutrition",2);
        t.set("ItemWeight", 5);
        t.set("LevelMin",5);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("poison")));
        Lib.add(t);

        t = Lib.extend("dead wasp", "base dead insect");
        t.set("Nutrition",1);
        t.set("ItemWeight", 6);
        t.set("LevelMin",7);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("poison")));
        Lib.add(t);

        t = Lib.extend("dead hornet", "base dead insect");
        t.set("Nutrition",1);
        t.set("ItemWeight", 7);
        t.set("LevelMin",11);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("poison")));
        Lib.add(t);

        t = Lib.extend("dead fire wasp", "base dead insect");
        t.set("Nutrition",1);
        t.set("ItemWeight", 5);
        t.set("LevelMin",9);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("strong poison")));
        Lib.add(t);

        t = Lib.extend("squished spider", "base dead insect");
        t.set("Nutrition",2);
        t.set("LevelMin",1);
        Lib.add(t);

        t = Lib.extend("dead spider", "base dead insect");
        t.set("Nutrition",5);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("dead wolf spider", "base dead insect");
        t.set("Nutrition",5);
        t.set("LevelMin",6);
        Lib.add(t);

        t = Lib.extend("dead red spider", "base dead insect");
        t.set("Nutrition",5);
        t.set("LevelMin",8);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("poison")));
        Lib.add(t);

        t = Lib.extend("dead black widow", "base dead insect");
        t.set("Nutrition",5);
        t.set("LevelMin",10);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("strong poison")));
        Lib.add(t);

        t = Lib.extend("dead tarantula", "base dead insect");
        t.set("Nutrition",25);
        t.set("LevelMin",21);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("deadly poison")));
        Lib.add(t);
    }

    private static void initTeeth() {
      	Thing t;
		
		t=Lib.extend("tooth","base monster part");
		t.set("NamePlural","teeth");
		t.set("Image",490);
		t.set("LevelMin",1);
		t.set("ItemWeight",10);
        t.set("IsVegetarianFood",0);
		t.set("ValueBase",30);
		Lib.add(t);
		
		t=Lib.extend("human tooth","base monster part");
		t.set("NamePlural","human teeth");
		t.set("Image",490);
		t.set("LevelMin",6);
		Lib.add(t);
		
		t=Lib.extend("giant tooth","base monster part");
		t.set("NamePlural","giant teeth");
		t.set("Image",490);
		t.set("ItemWeight",200);
		t.set("LevelMin",16);
		Lib.add(t);
		
		t=Lib.extend("dragon tooth","base monster part");
		t.set("NamePlural","dragon teeth");
		t.set("Image",490);
		t.set("ItemWeight",60);
		t.set("LevelMin",26);
		Lib.add(t);
 
    }
    
    private static void initSkulls() {
    	Thing t;
		
		t=Lib.extend("base skull","base monster part");
    	t.set("Image",301);
    	t.set("ItemWeight",1000);
    	t.set("Nutrition",5);
    	t.set("Frequency",20);
    	t.set("LevelMin",10);
    	t.set("IsSkull",1);
    	t.set("IsBlackIngredient",1);
    	t.set("HPS",8);
    	Lib.add(t);
    	
    	t=Lib.extend("skull","base skull");
    	t.set("UName","skull");
    	t.set("ValueBase",10);
    	Lib.add(t);
    	
    	t=Lib.extend("human skull","base skull");
    	Lib.add(t);
    	
    	t=Lib.extend("orc skull","base skull");
    	t.set("LevelMin",7);
    	Lib.add(t);
    	
    	t=Lib.extend("haunted skull","base skull");
    	t.set("Frequency",2);
    	t.set("DecayRate",100);
    	t.set("LevelMin",12);
    	t.set("DecayType","haunted skeleton");
    	t.set("OnAction",Scripts.decay());
    	Lib.add(t);
    	
    	t=Lib.extend("dragon skull","base skull");
    	t.set("LevelMin",20);
    	Lib.add(t);
    	
    	t=Lib.extend("haunted dragon skull","dragon skull");
    	t.set("Frequency",2);
    	t.set("LevelMin",26);
    	t.set("DecayRate",10);
    	t.set("DecayType","skeletal dragon");
    	t.set("OnAction",Scripts.decay());
    	t.set("HPS",800);
    	Lib.add(t);
    }
    
    private static void initHerbs() {
        Thing t = Lib.extend("base herb", "base food");
        t.set("UName","strange herb");
        t.set("IsHerb",1);
        t.set("Nutrition", 2);
        t.set("Frequency",20);
        t.set("IsAlchemyIngredient",1);
        t.set("IsHerbIngredient",1);
        t.set("IsVegetarianFood",1);
        t.set("Image",486);
        t.addHandler("OnTouch",new Script() {
        	private static final long serialVersionUID = 1L;

            public boolean handle(Thing t, Event e) {
        		Thing tt=e.getThing("Target");
        		if ((RPG.d(5)==1)&&tt.isHero()&&!tt.getFlag(Skill.HERBLORE)) {
        			Item.curse(t,false);
        		}
        		return false;
        	}
        });
        addFood(t);    
        
        t=Lib.extend("wayflower herb","base herb");
        t.set("LevelMin",1);
        Lib.add(t);
        
        t=Lib.extend("dervish flower","base herb");
        t.set("LevelMin",2);
        t.addHandler("OnEaten",Scripts.addEffect("Target",Lib.create("strong poison")));
        Lib.add(t);
        
        t=Lib.extend("pixiedance flower","base herb");
        t.set("LevelMin",3);
        t.addHandler("OnEaten",Scripts.spellEffect("Target","Teleport Self",10));
        Lib.add(t);
        
        t=Lib.extend("pipeweed leaf","base herb");
        t.set("LevelMin",4);
        t.addHandler("OnEaten",Scripts.statusSwitch(
        		Scripts.incStat("Target",RPG.ST_MPS,10,"You feel much more lively"),
				Scripts.incStat("Target",RPG.ST_MPS,2,"You feel more lively"),
				Scripts.incStat("Target",RPG.ST_MPS,-20,"You feel really weak")
        	));
        Lib.add(t);
        
        t=Lib.extend("arwenflower leaf","base herb");
        t.set("LevelMin",5);
        t.addHandler("OnEaten",Scripts.statusSwitch(
        		Scripts.incStat("Target",RPG.ST_EXP,100,"You feel like you are learning a lot"),
        		Scripts.incStat("Target",RPG.ST_EXP,20,"You feel like you are learning something"),
        		Scripts.incStat("Target",RPG.ST_EXP,-100,"You feel forgetful")
				));
        Lib.add(t);
        
        t=Lib.extend("kingsweed leaf","base herb");
        t.set("LevelMin",6);
        Lib.add(t);
        
        t=Lib.extend("kahnflower","base herb");
        t.set("LevelMin",7);
        t.addHandler("OnTouch",Scripts.damage("poison",RPG.d(2,4),"painfully stung by the herb!",100));
        Lib.add(t);
        
        t=Lib.extend("krom leaf","base herb");
        t.set("LevelMin",8);
        t.set("OnEaten",Scripts.statusSwitch(
        		Scripts.statGain("Target","IN",30,100),
           		Scripts.statGain("Target","IN",15,100),
				Scripts.incStat("Target","IN",-1,"Your head aches badly")
				));
        Lib.add(t);
        
        t=Lib.extend("fire nettle","base herb");
        t.set("LevelMin",9);
        t.addHandler("OnTouch",Scripts.damage("poison",RPG.d(4,4),"painfully stung by the herb!",100));
        Lib.add(t);
    }
    
    private static void initFood() {    
    	Thing t;
    	
        t = Lib.extend("beefcake", "base food");
        t.set("Image", 225);
        t.set("Nutrition", 30);
        t.set("ItemWeight", 2000);
        t.set("IsVegetarianFood",0);
        t.set("LevelMin", 3);
        addFood(t);
 
    }
 
    /**
     * Create vegetables
     *
     */
    private static void initVegetables() {
        Thing t;   
        t = Lib.extend("base vegetable", "base food");
        t.set("IsVegetable",1);
        t.set("Nutrition", 10);
        t.set("Frequency",30);
        t.set("IsShopFood",1);
        t.set("IsVegetarianFood",1);
        t.set("Image",480);
        addFood(t);
        
        t=Lib.extend("spinach leaf","base vegetable");
        t.set("Image",480);
        t.set("ItemWeight",200);
        t.addHandler("OnEaten",Scripts.statGain("Target","ST",15,15));
        t.set("LevelMin",4);
        addFood(t);
        
        t=Lib.extend("clover leaf","base vegetable");
        t.set("NamePlural","clover leaves");
		t.set("Image",493);
        t.set("ItemWeight",100);
        t.addHandler("OnEaten",Scripts.statGain("Target","CH",40,40));
        t.add("LocationModifiers",Modifier.bonus("Luck",3));
        t.set("LevelMin",15);
        addFood(t);
        
        t=Lib.extend("carrot","base vegetable");
        t.set("Image",482);
        t.set("ItemWeight",500);
        t.set("Nutrition", 15);
        t.set("LevelMin",1);
        addFood(t);
        
        t=Lib.extend("prize carrot","base vegetable");
        t.set("Image",482);
        t.set("ItemWeight",500);
        t.set("Nutrition", 15);
        t.set("LevelMin",7);
        t.set("OnEaten",Scripts.statGain("Target","SK",12,100));
        addFood(t);
        
        t=Lib.extend("parsnip","base vegetable");
        t.set("Image",483);
        t.set("ItemWeight",400);
        t.set("Nutrition", 12);
        t.set("LevelMin",4);
        t.addHandler("OnEaten",Scripts.statGain("Target","CH",15,100));
        addFood(t);
        
        t=Lib.extend("ginger root","base vegetable");
        t.set("Image",485);
        t.set("ItemWeight",600);
        t.set("Nutrition", 10);
        t.set("LevelMin",14);
        t.addHandler("OnEaten",Scripts.statGain("Target","AG",20,100));
        addFood(t);
        
        t=Lib.extend("ginseng root","base vegetable");
        t.set("Image",485);
        t.set("ItemWeight",800);
        t.set("Nutrition", 30);
        t.set("LevelMin",18);
        t.addHandler("OnEaten",Scripts.incStat("Target","APS",100,"You feel a burst of energy fill your body"));
        addFood(t);
    
    }    
    
    /**
     * Create fish
     *
     */
    private static void initFish() {
        Thing t;

        t = Lib.extend("base fish", "base food");
        t.set("IsFish",1);
        t.set("Nutrition", 100);
        t.set("Frequency",30);
        t.set("IsGoblinIngredient",1);
        t.set("IsVegetarianFood",1);
        t.set("Image",236);
        t.addHandler("OnEaten",Scripts.statGain("Target","IN",10,30));
        addFood(t);   

        t = Lib.extend("tiny fish","base fish");
        t.set("NamePlural",t.getString("Name"));
        t.set("LevelMin",1);
        t.set("Nutrition", 100);
        t.set("ItemWeight",200);
        addFood(t);
        
        t = Lib.extend("small fish","base fish");
        t.set("NamePlural",t.getString("Name"));
        t.set("LevelMin",3);
        t.set("Nutrition", 120);
        t.set("ItemWeight",400);
        addFood(t);
        
        t = Lib.extend("fish","base fish");
        t.set("NamePlural",t.getString("Name"));
        t.set("LevelMin",6);
        t.set("Nutrition", 140);
        t.set("ItemWeight",800);
        addFood(t);
        
        t = Lib.extend("rotten fish","fish");
        t.set("UName","fish");
        t.set("NamePlural",t.getString("Name"));
        t.set("UNamePlural",t.getString("UName"));
        t.set("LevelMin",5);
        t.set("Nutrition", 30);
        t.set("ItemWeight",760);
        addFood(t);
        
        t = Lib.extend("large fish","base fish");
        t.set("NamePlural",t.getString("Name"));
        t.set("LevelMin",9);
        t.set("Nutrition", 160);
        t.set("Image",238);
        t.set("ItemWeight",1600);
        addFood(t);
        
        t = Lib.extend("trout","base fish");
        t.set("LevelMin",12);
        t.set("Nutrition", 170);
        t.set("ItemWeight",2000);
        t.set("Image",238);
        addFood(t);
        
        t = Lib.extend("salmon","base fish");
        t.set("LevelMin",16);
        t.set("Nutrition", 200);
        t.set("ItemWeight",2500);
        t.set("Image",239);
        addFood(t);
        
        t = Lib.extend("large salmon","base fish");
        t.set("LevelMin",16);
        t.set("Nutrition", 180);
        t.set("ItemWeight",4000);
        t.set("Image",239);
        addFood(t);
        
        t = Lib.extend("red snapper","base fish");
        t.set("NamePlural",t.getString("Name"));
        t.set("LevelMin",7);
        t.set("Nutrition", 230);
        t.set("Image",237);
        t.set("ItemWeight",500);
        t.addHandler("OnEaten",Scripts.statGain("Target","CR",25,25));
        addFood(t);
        
    }
    
	/**
	 * Create meat-based food items
	 *
	 */
    private static void initMeat() {
        Thing t;

        t = Lib.extend("base meat", "base food");
        t.set("IsMeat",1);
        t.set("Nutrition", 60);
        t.set("Frequency",20);
        t.set("IsShopFood",1);
        t.set("IsVegetarianFood",0);
        t.set("Image", 220);
        addFood(t);

        t = Lib.extend("meat ration", "base meat");
        t.set("Image", 220);
        t.set("Nutrition", 60);
        t.set("ItemWeight", 1500);
        t.set("LevelMin", 6);
        addFood(t);
        
        t = Lib.extend("steak", "base meat");
        t.set("Image", 230);
        t.set("Nutrition", 100);
        t.set("ItemWeight", 2000);
        t.set("LevelMin", 8);
        addFood(t);
        
        t = Lib.extend("tough steak", "base meat");
        t.set("Image", 230);
        t.set("Nutrition", 50);
        t.set("ItemWeight", 2500);
        t.set("LevelMin", 4);
        t.addHandler("OnEaten",Scripts.statGain("Target","TG",10,20));
        addFood(t);

        t = Lib.extend("escargot steak", "base meat");
        t.set("Image", 230);
        t.set("Nutrition", 180);
        t.set("ItemWeight", 500);
        t.set("LevelMin", 10);
        t.addHandler("OnEaten",Scripts.statGain("Target","SK",10,30));
        addFood(t);      
        
        t = Lib.extend("piece of rabbit meat", "base meat");
        t.set("NamePlural","pieces of rabbit meat");
        t.set("Image", 230);
        t.set("Nutrition", 180);
        t.set("ItemWeight", 200);
        t.set("LevelMin", 7);
        t.addHandler("OnEaten",Scripts.statGain("Target","AG",10,30));
        addFood(t);     
        
        t = Lib.extend("piece of hare meat", "base meat");
        t.set("NamePlural","pieces of hare meat");
        t.set("Image", 230);
        t.set("Nutrition", 120);
        t.set("ItemWeight", 300);
        t.set("LevelMin", 12);
        t.addHandler("OnEaten",Scripts.statGain("Target","AG",16,30));
        addFood(t);     
        
        t = Lib.extend("prime steak", "base meat");
        t.set("Image", 230);
        t.set("Nutrition", 150);
        t.set("ItemWeight", 1800);
        t.set("LevelMin", 13);
        t.set("OnEaten",Scripts.statGain("Target","ST",13,100));
        addFood(t);
        
        t = Lib.extend("peppered steak", "base meat");
        t.set("Image", 230);
        t.set("Nutrition", 160);
        t.set("ItemWeight", 1600);
        t.set("LevelMin", 19);
        t.set("OnEaten",Scripts.statGain("Target","ST,TG",18,100));
        addFood(t);
        
        t = Lib.extend("ham", "base meat");
        t.set("Image", 220);
        t.set("Nutrition", 80);
        t.set("ItemWeight", 2600);
        t.set("LevelMin", 6);
        addFood(t);
        
        t = Lib.extend("sausage", "base meat");
        t.set("Image", 231);
        t.set("Nutrition", 120);
        t.set("ItemWeight", 1200);
        t.set("LevelMin", 11);
        addFood(t);
        
        t = Lib.extend("giant sausage", "base meat");
        t.set("Image", 231);
        t.set("Nutrition", 125);
        t.set("ItemWeight", 3000);
        t.set("LevelMin", 13);
        addFood(t);
        
        t = Lib.extend("spicy sausage", "base meat");
        t.set("Image", 231);
        t.set("Nutrition", 180);
        t.set("ItemWeight", 1800);
        t.set("LevelMin", 16);
        t.addHandler("OnEaten",Scripts.statGain("Target","TG",25,100));
        addFood(t);
        
        t = Lib.extend("dwarven sausage", "base meat");
        t.set("Image", 235);
        t.set("Nutrition", 240);
        t.set("ItemWeight", 3000);
        t.set("LevelMin", 22);
        t.addHandler("OnEaten",Scripts.statGain("Target","ST",30,100));
        addFood(t);

    }
    
    /**
	 * Create poultry-based food items
	 *
	 */
    private static void initPoultry() {
        Thing t;

        t = Lib.extend("base poultry", "base food");
        t.set("IsPoultry",1);
        t.set("Nutrition", 60);
        t.set("Frequency",20);
        t.set("IsShopFood",1);
        t.set("Image", 220);
        addFood(t);

        t = Lib.extend("chicken leg", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 40);
        t.set("ItemWeight", 500);
        t.set("LevelMin", 4);
        // TODO chicken legs can make you brave (for a while)
        addFood(t);

        t = Lib.extend("chicken wing", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 25);
        t.set("ItemWeight", 350);
        t.set("LevelMin", 4);
        // TODO chicken wings can make you agile (for a while)
        addFood(t);

        t = Lib.extend("chicken breast", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 60);
        t.set("ItemWeight", 700);
        t.set("LevelMin", 4);
        // TODO chicken wings can make you strong (for a while)
        addFood(t);

        t = Lib.extend("chicken thigh", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 40);
        t.set("ItemWeight", 500);
        t.set("LevelMin", 4);
        // TODO chicken wings can make you smart (for a while)
        addFood(t);

        t = Lib.extend("half a chicken", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 90);
        t.set("ItemWeight", 1400);
        t.set("LevelMin", 4);
        addFood(t);

        t = Lib.extend("roast chicken", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 150);
        t.set("ItemWeight", 2200);
        t.set("LevelMin", 4);
        addFood(t);

        t = Lib.extend("roast duck", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 120);
        t.set("ItemWeight", 2000);
        t.set("LevelMin", 4);
        addFood(t);

        t = Lib.extend("roast goose", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 160);
        t.set("ItemWeight", 2700);
        t.set("LevelMin", 4);
        addFood(t);

        t = Lib.extend("roast turkey", "base poultry");
        t.set("Image", 220);
        t.set("Nutrition", 200);
        t.set("ItemWeight", 3000);
        t.set("LevelMin", 5);
        addFood(t);

    }

    private static void initEggs() {
        Thing t;

        t = Lib.extend("egg", "base food");
        t.set("Image",491);
        t.set("LevelMin",1);
        t.set("HPS",1);
        t.set("Nutrition", 40);
        t.set("IsShopFood",1);
        t.set("ItemWeight",40);
        Lib.add(t);
        
        t=Lib.extend("snake egg","egg");
        t.set("Frequency",0);
        t.set("DeathDecoration","[IsSnake]");
        t.set("OnAction",Scripts.decay());
        t.set("DecayRate",400);
        Lib.add(t);
        
        t=Lib.extend("bad egg","egg");
        t.set("UName","egg");
        t.set("LevelMin",6);
        t.set("OnEaten",Scripts.addEffect("Target",Lib.create("curse")));
        Lib.add(t);
        
    }

    private static void initMushrooms() {
        Thing t;
        
    	/////////////////
        // Mushrooms
        t=Lib.extend("mushroom", "base food");
        t.set("IsMushroom",1);
        t.set("Image", 233);
        t.set("Nutrition", 30);
        t.set("ItemWeight", 700);
        t.set("LevelMin", 1);
        t.set("IsBlackIngredient",1);
        t.set("IsHerbIngredient",1);
        t.set("OnEaten",Scripts.statGain("Target","ST,CH",8,10));
        addFood(t);

        t=Lib.extend("blue mushroom", "mushroom");
        t.set("Image", 234);
        t.set("Nutrition", 50);
        t.set("ItemWeight", 800);
        t.set("LevelMin", 4);
        t.set("OnEaten",Scripts.statGain("Target","ST,CH",12,30));
        addFood(t);
        
        t=Lib.extend("magic mushroom", "mushroom");
        t.set("Image", 229);
        t.set("IsMagicItem",1);
        t.set("Nutrition", 50);
        t.set("ItemWeight", 800);
        t.set("LevelMin", 16);
        t.set("OnEaten",Scripts.statGain("Target","SK,AG",16,30));
        addFood(t);    	
    	
        t=Lib.extend("psilocybin mushroom", "mushroom");
        t.set("Image", 229);
        t.set("IsMagicItem",1);
        t.set("Nutrition", 50);
        t.set("ItemWeight", 800);
        t.set("LevelMin", 16);
        t.set("OnEaten",Scripts.statGain("Target","IN,WP",16,30));
        addFood(t);    	

    }    
    
    private static void initPies() {
        Thing t;
        
        t=Lib.extend("base pie","base food");
        t.set("Image", 225);
        t.set("Nutrition", 88);
        t.set("ItemWeight", 2000);
        t.set("IsShopFood",1);
        addFood(t);
        
    	t = Lib.extend("pork pie", "base pie");
        t.set("Image", 225);
        t.set("Nutrition", 95);
        t.set("ItemWeight", 2500);
        t.set("IsVegetarianFood",0);
        t.set("LevelMin", 5);
        addFood(t);
        
    	t = Lib.extend("meat pie", "base pie");
        t.set("Image", 225);
        t.set("Nutrition", 125);
        t.set("ItemWeight", 2200);
        t.set("IsVegetarianFood",0);
        t.set("LevelMin", 7);
        addFood(t);

        t = Lib.extend("numble pie", "base pie");
        t.set("Image", 225);
        t.set("Description","This pie was made from animal innards");
        t.set("Nutrition", 55);
        t.set("ItemWeight", 2100);
        t.set("LevelMin", 3);
        addFood(t);

        t = Lib.extend("apple pie", "base pie");
        t.set("Image", 225);
        t.set("Nutrition", 100);
        t.set("ItemWeight", 2200);
        t.set("LevelMin", 1);
        addFood(t);   	
    }
    
    private static void initBread() {
        Thing t;
        
        ///////////////
        // bread
        t = Lib.extend("loaf of bread", "base food");
        t.set("NamePlural","loaves of bread");
        t.set("Image", 222);
        t.set("Nutrition", 60);
        t.set("ItemWeight", 3000);
        t.set("IsShopFood",1);
        t.set("IsBread",1);
        t.set("LevelMin", 1);
        addFood(t);
        
        t = Lib.extend("loaf of stale bread", "loaf of bread");
        t.set("UName","loaf of bread");
        t.set("UNamePlural","loaves of bread");
        t.set("NamePlural","loaves of stale bread");
        t.set("Nutrition", 35);
        t.set("LevelMin", 1);
        addFood(t);
        
        t = Lib.extend("loaf of hobbit bread", "loaf of bread");
        t.set("UName","loaf of bread");
        t.set("UNamePlural","loaves of bread");
        t.set("NamePlural","loaves of hobbit bread");
        t.set("Nutrition", 125);
        t.set("ItemWeight", 2000);
        t.set("LevelMin", 17);
        addFood(t);
        
        t = Lib.extend("loaf of dwarf bread", "loaf of bread");
        t.set("UName","loaf of bread");
        t.set("UNamePlural","loaves of bread");
        t.set("NamePlural","loaves of dwarf bread");
        t.set("Nutrition", 100);
        t.set("LevelMin", 12);
        addFood(t);    	
        
        t = Lib.extend("loaf of elven bread", "loaf of bread");
        t.set("UName","loaf of bread");
        t.set("UNamePlural","loaves of bread");
        t.set("NamePlural","loaves of elven bread");
        t.set("Nutrition", 260);
        t.set("ItemWeight", 1000);
        t.set("LevelMin", 22);
        addFood(t);   
        
        t = Lib.extend("loaf of spider bread", "loaf of bread");
        t.set("UName","loaf of bread");
        t.set("UNamePlural","loaves of bread");
        t.set("NamePlural","loaves of spider bread");
        t.set("Nutrition", 220);
        t.set("ItemWeight", 1000);
        t.set("LevelMin", 19);
        addFood(t); 
        
        t = Lib.extend("loaf of elven waybread", "loaf of bread");
        t.set("UName","loaf of bread");
        t.set("UNamePlural","loaves of bread");
        t.set("NamePlural","loaves of elven waybread");
        t.set("Nutrition", 360);
        t.set("ItemWeight", 500);
        t.set("LevelMin", 27);
        addFood(t);   
    }
     
    public static void initFruit() {
    	Thing t;
        
    	// base fruit
    	t = Lib.extend("base fruit", "base food");
    	t.set("IsFruit",1);
        t.set("Image", 221);
        t.set("IsActive",1);
        t.set("ValueBase",5);
        t.set("DecayRate", 5);
        t.set("IsShopFood",1);
        t.multiplyStat("Frequency",0.3);
        t.set("DecayMessage", "rots");
        t.addHandler("OnAction",Scripts.decay());
        t.set("LevelMin", 1);
        addFood(t);
     	
    	
        // apples
        t = Lib.extend("apple", "base fruit");
        t.set("Image", 221);
        t.set("Nutrition", 45);
        t.set("ItemWeight", 800);
        t.set("DecayRate", 2);
        t.set("DecayMessage", "rots away");
        t.set("LevelMin", 1);
        addFood(t);

        t = Lib.extend("cooking apple", "apple");
        t.set("Image", 221);
        t.set("ItemWeight", 1300);
        t.set("Nutrition", 35);
        addFood(t);
        
        t = Lib.extend("red apple", "apple");
        t.set("Image", 228);
        t.set("Nutrition", 70);
        addFood(t);
        
        t = Lib.extend("maggoty apple", "apple");
        t.set("UName", "red apple");
        t.set("Image", 228);
        t.set("LevelMin", 3);
        t.set("LevelMax", 20);
        t.set("Nutrition", 25);
        t.addHandler("OnAction",Scripts.generator("fly swarm",2));
        addFood(t);
        
        t = Lib.extend("juicy apple", "apple");
        t.set("Image", 228);
        t.set("Nutrition", 95);
        t.addHandler("OnAction",Scripts.generator("wasp swarm",2));
        addFood(t);
        
        t = Lib.extend("crab apple", "apple");
        t.set("Image", 221);
        t.set("Nutrition", 20);
        t.set("ItemWeight", 300);
        t.set("IsMissile",1);
        t.set("MissileType","bullet");
        Missile.setStats(t,60,0,16,0);
        addFood(t);

        // Citrus fruits
        t = Lib.extend("base citrus fruit", "base fruit");
        t.set("Image", 520);
        t.set("Nutrition", 45);
        t.set("ItemWeight", 800);
        t.set("DecayRate", 4);
        t.set("DecayMessage", "rots away");
        t.set("LevelMin", 11);
        t.set("OnEaten",Scripts.cure(5));
        addFood(t);

        t = Lib.extend("orange", "base citrus fruit");
        t.set("Image", 520);
		t.addHandler("OnEaten",Scripts.heal("Target",1,100));
        addFood(t);

        t = Lib.extend("large orange", "base citrus fruit");
        t.set("Image", 520);
        t.set("Nutrition", 70);
        t.set("ItemWeight", 1300);
        addFood(t);

        t = Lib.extend("tangerine", "base citrus fruit");
        t.set("Image", 520);
        addFood(t);

        t = Lib.extend("grapefruit", "base citrus fruit");
        t.set("Image", 525);
        t.set("Nutrition", 70);
        t.set("ItemWeight", 1600);
        addFood(t);

        t = Lib.extend("lemon", "base citrus fruit");
        t.set("Image", 526);
        t.set("Nutrition", 35);
        t.set("ItemWeight", 400);
        addFood(t);

        t = Lib.extend("lime", "base citrus fruit");
        t.set("Image", 527); 
        t.set("Nutrition", 25);
        t.set("ItemWeight", 300);
        addFood(t);

        t = Lib.extend("key lime", "lime");
        t.set("Frequency",20);
        t.set("ItemWeight", 300);
        // TODO eat a key lime next to a locked door and ...
        addFood(t);

 
        // Other fruits



        t = Lib.extend("bunch of grapes", "base fruit");
        t.set("NamePlural","bunches of grapes");
        t.set("Image", 522);
        t.set("Nutrition", 140);
        t.set("ItemWeight", 400);
        t.set("LevelMin",13);
        // TODO make wine
        addFood(t);

        /*
         * TODO: get images for these, then add
        t = Lib.extend("kiwi fruit", "base fruit");
        t.set("Image", 221);
        t.set("Nutrition", 30);
        t.set("ItemWeight", 500);
        addFood(t);

        
        t = Lib.extend("pineapple", "base fruit");
        t.set("Image", 221);
        t.set("Nutrition", 65);
        t.set("ItemWeight", 1200);
        addFood(t);
        */
    }
    
    public static void initStoneFruit() {
    	Thing t;
    	
        // Stone fruits
        t = Lib.extend("base stone fruit", "base fruit");
        t.set("Image", 521);
        t.set("Nutrition", 70);
        t.set("ItemWeight", 800);
        t.set("DecayRate", 6);
        t.set("DecayMessage", "rots away");
        t.set("LevelMin", 5);
        // TODO eat the fruit, get the pit, throw the pit
        addFood(t);

        t = Lib.extend("peach", "base stone fruit");
        t.set("Image", 520);
        t.set("LevelMin", 15);
        addFood(t);

        t = Lib.extend("apricot", "base stone fruit");
        t.set("Image", 520);
        t.set("ItemWeight", 600);
        t.set("LevelMin", 17);
        addFood(t);

        t = Lib.extend("plum", "base stone fruit");
        t.set("Image", 521);
        t.set("ItemWeight", 400);
        t.set("LevelMin", 6);
        addFood(t);

        t = Lib.extend("cherry", "base stone fruit");
        t.set("Image", 521);
        t.set("Nutrition", 100);
        t.set("ItemWeight", 100);
        t.set("LevelMin", 9);
        // TODO the cherry bomb variety.  What a pit.
        addFood(t);

        t = Lib.extend("ranier cherry", "base stone fruit");
        t.set("Image", 521);
        t.set("Nutrition", 130);
        t.set("Frequency",20);
        t.set("ItemWeight", 100);
        t.set("LevelMin", 13);
        addFood(t);

    }
    
    public static void initBerries() {
    	Thing t;
    	
    	t = Lib.extend("base berry", "base fruit");
        t.set("Image", 523);
        t.set("Nutrition", 15);
        t.set("ItemWeight", 100);
        t.set("LevelMin", 8);
        addFood(t);
        
        t = Lib.extend("strawberry", "base berry");
        t.set("Image", 523);
        t.set("Nutrition", 15);
        t.set("ItemWeight", 100);
        addFood(t);

        t = Lib.extend("raspberry", "base berry");
        t.set("Image", 523);
        t.set("Nutrition", 15);
        t.set("ItemWeight", 100);
        addFood(t);

        t = Lib.extend("blueberry", "base berry");
        t.set("Image", 524);
        t.set("Nutrition", 15);
        t.set("ItemWeight", 50);
        addFood(t);
    }
    
}