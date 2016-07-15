/*
 * Created on 27-Jul-2004
 *
 * By Mike Anderson
 */
package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Modifier;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Equipment {
	public static void init() {
		Thing t;
		
		t=Lib.extend("base equipment","base item");
		t.set("Frequency",0);
		t.set("IsEquipment",1);
		t.set("LevelMin",1);
		t.set("ASCII","]");
		Lib.add(t);
		
		
		initBoats();
		initKeys();
		initTools();
		initBarrels();
        initDiggingTools();
        initContainers();
		
	}

	private static class KeyUse extends Script {
		private static final long serialVersionUID = 1L;

        public boolean handle(Thing t, Event e) {
			Thing u=e.getThing("User");
			u.message("Select direction:");
			Point d=Game.getDirection();
			if (d==null) {
				u.message("");
				return false;
			}
			BattleMap m=u.getMap();
			Thing door=m.getFlaggedObject(u.x+d.x,u.y+d.y,"IsDoor");
			if (door==null) {
				u.message("There is no door to lock in that direction");
			} else {
				if (!Door.keyFits(t,door)) {
					u.message(t.getYourName()+" does not seem to fit "+door.getTheName());
					return false;
				}
				if (!door.getFlag("IsLocked")) {
					if (Door.canLock(t,door)) {
						u.message("You lock "+door.getTheName());
						door.set("IsLocked",1);
					} else {
						u.message("You seem unable to lock "+door.getTheName());
					}
				} else {
					if (Door.unlock(t,door)) {
						u.message("You unlock "+door.getTheName());
					}
				}
			}
			
			return false;
		}
		
	}
	
	private static class GoFish extends Script {
		private static final long serialVersionUID = 3833466197629743666L;
        private static String[] theFish=new String[] {"tiny fish","small fish","fish","large fish","trout","salmon","large salmon"};
		public boolean handle(Thing t, Event e) {
			Thing u=e.getThing("User");
			BattleMap m=u.getMap();
			
			u.incStat("APS",-1000);
			
			int fc=0;
			for (int x=u.x-1; x<=u.x+1; x++) {
				for (int y=u.y-1; y<=u.y+1; y++) {
					switch (m.getTile(x,y)) {
						case Tile.RIVER: fc+=2; break;
						case Tile.SEA: fc+=3; break;
						case Tile.STREAM: fc+=1; break;
					}
				}
			}
			
			if (fc<=0) {
				u.message("There is nowhere good to fish here!");
				return true;
			}
			
			int fskill=u.getStat("CR")*(1+u.getStat(Skill.SURVIVAL));
			int res=-4;
			for (int i=0; i<10; i++) {
				if (RPG.test(fc*fskill,500)) res++;
			}
			
			if (res<0) {
				u.message("You don't manage to catch anything");
				return true;
			}
			
			Thing fish=Lib.create(theFish[res]);
			u.message("You catch "+fish.getAName()+"!");
			u.addThingWithStacking(fish);
			
			return true;
		}
	}
	
	private static void initTools() {
		Thing t;
		
		t=Lib.extend("base tool","base equipment");
		t.set("IsTool",1);
        t.set("ValueBase", 5);
		t.set("Frequency",10);
		Lib.add(t);
		
		t=Lib.extend("fishing rod","base tool");
		t.set("Image",441);
		t.set("ItemWeight",1600);
		t.set("OnUse",new GoFish());
		Lib.add(t);
		
		t=Lib.extend("lock pick","base tool");
		t.set("HPS",2);
        //CBG Note sure if this is exactly right but 442 does not exist.
		//t.set("Image",442);
		t.set("Image",464);
		t.set("ItemWeight",200);
		Lib.add(t);
	}
	
	private static void initDiggingTools() {
		Thing t;
		
		t=Lib.extend("base digging tool","base tool");
		t.set("Image",52);
		t.set("IsDiggingTool",1);	
		t.set("IsWeapon",1);
		t.set("AttackCost",100);
		t.set("WeaponDamageType",RPG.DT_NORMAL);
		t.set("WieldType",RPG.WT_MAINHAND);
		t.set("HitVerb","hit/hits");
		Lib.add(t);
		
		t=Lib.extend("wooden spade","base digging tool");
		t.set("Image",52);
		t.set("ItemWeight",3000);
		t.set("HPS",10);
		t.set("DigDamage",5);
		t.set("DigCost",1000);
		t.set("DamageLevels",2);
		t.set("LevelMin",5);
		Weapon.setStats(t,60,0,50,0,30,1);
		Lib.add(t);
		
		t=Lib.extend("sturdy spade","wooden spade");
		t.set("HPS",40);
		t.set("LevelMin",17);
		Lib.add(t);
		
		t=Lib.extend("pickaxe","base digging tool");
		t.set("Image",51);
		t.set("ItemWeight",2500);
		t.set("IsWeapon",1);
		t.set("WieldType",RPG.WT_MAINHAND);
		t.set("HPS",20);
		t.set("DigDamage",2);
		t.set("IsDiggingTool",1);
		t.set("DigCost",1300);
		t.set("LevelMin",10);
		Weapon.setStats(t,60,0,60,0,20,0);
		Lib.add(t);
		
		t=Lib.extend("old pickaxe","pickaxe");
		t.set("HPS",1);
		Weapon.setStats(t,50,0,50,0,10,0);
		t.set("LevelMin",1);
		Lib.add(t);
		
	}
	
	private static void initBoats() {
		Thing t;
		
		t=Lib.extend("base boat","base equipment");
		t.set("IsBoat",1);
		t.set("Frequency",10);
        t.set("ValueBase", 10);
		t.add("CarriedModifiers",Modifier.bonus(Skill.SWIMMING,1));
		Lib.add(t);

		t=Lib.extend("canoe","base boat");
		t.set("LevelMin",8);
		t.set("Image",440);
		t.set("ItemWeight",20000);
		Lib.add(t);
		
		t=Lib.extend("old canoe","canoe");
		t.set("LevelMin",1);
		t.set("Image",440);
		Lib.add(t);
		
	}
	
	private static void initBarrels() {
		Thing t;
		
		t=Lib.extend("base barrel","base equipment");
		t.set("IsBarrel",1);
		t.set("Image",263);
		t.set("Frequency",10);
		t.set("ItemWeight",20000);
		Lib.add(t);
		
		t=Lib.extend("water barrel","base barrel");
		t.set("Uname","barrel");
		t.set("LevelMin",1);
		t.set("DeathDecoration","water pool,5% [IsRing]");
		Lib.add(t);
		
		t=Lib.extend("powder keg","base barrel");
		t.set("Uname","barrel");
		t.set("LevelMin",10);
		t.set("DeathDecoration","Blaze");
		Lib.add(t);
		
		t=Lib.extend("food barrel","base barrel");
		t.set("Uname","barrel");
		t.set("LevelMin",10);
		t.set("DefaultThings","50% ham,50% steak,50% juicy apple,50% mushroom");
		Lib.add(t);
		
		t=Lib.extend("mouldy keg","base barrel");
		t.set("Uname","barrel");
		t.set("LevelMin",3);
		t.set("DeathDecoration","Poison Cloud");
		Lib.add(t);
		
		t=Lib.extend("barrel of woe","base barrel");
		t.set("Uname","black barrel");
		t.set("LevelMin",7);
		t.set("DeathDecoration","haunted skull");
		Lib.add(t);
		
		t=Lib.extend("plague casket","base barrel");
		t.set("Uname","barrel");
		t.set("LevelMin",14);
		t.set("DeathDecoration","plague cloud");
		Lib.add(t);
	}
	
	private static void initKeys() {
		Thing t;
		
		t=Lib.extend("base key","base equipment");
		t.set("IsKey",1);
		t.set("ItemWeight",200);
		t.set("LevelMin",1);
		t.set("ValueBase",1000);
		t.set("Frequency",10);
		t.set("OnUse",new KeyUse());
		t.set("Image",460);
		Lib.add(t);
		
		t=Lib.extend("copper key","base key");
		t.set("Image",460);
		t.set("LevelMin",1);
		Lib.add(t);
		
		t=Lib.extend("brass key","base key");
		t.set("Image",460);
		t.set("LevelMin",1);
		Lib.add(t);
		
		t=Lib.extend("bronze key","base key");
		t.set("Image",460);
		t.set("LevelMin",1);
		Lib.add(t);
		
		t=Lib.extend("golden key","base key");
		t.set("Image",461);
		t.set("LevelMin",10);
		Lib.add(t);
		
		t=Lib.extend("ornate key","base key");
		t.set("Image",461);
		t.set("LevelMin",5);
		Lib.add(t);
		
		t=Lib.extend("skull key","base key");
		t.set("Image",462);
		t.set("LevelMin",15);
		Lib.add(t);
		
		t=Lib.extend("iron key","base key");
		t.set("Image",463);
		t.set("LevelMin",3);
		Lib.add(t);
		
		t=Lib.extend("dull key","base key");
		t.set("LevelMin",8);
		t.set("Image",463);
		Lib.add(t);
		
		t=Lib.extend("runic key","base key");
		t.set("Image",463);
		t.set("LevelMin",20);
		Lib.add(t);
		
    }

    /**
     * The simplest container just serves to organize their contents.  At higher
     * levels, containers offer various kinds of protection from damage and theft.
     * Containers exist for keys, scrolls, potions, books, food, ingredients,
     * arrows, jewelry (rings and amulets), runes and wands.  Weapons, armour and
     * tools have no item specific container.
     */
    private static void initContainers() {
        Thing t;

        t = Lib.extend("base container","base equipment");
        t.set("IsContainer",1);
        t.set("IsStoreItem",0); // not for sale
        t.set("ItemWeight",200);
        t.set("LevelMin",1);
        t.set("ValueBase",1000);
        t.set("Frequency",5);
        Lib.add(t);

        t = Lib.extend("key ring","base container");
        t.set("Image",463);
        t.set("WieldType", RPG.WT_KEYRING);
        t.set("Holds","IsKey");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof key ring","key ring");
        t.set("Uname","key ring");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof key ring","key ring");
        t.set("Uname","key ring");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof key ring","key ring");
        t.set("Uname","key ring");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof key ring","key ring");
        t.set("Uname","key ring");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof key ring","key ring");
        t.set("Uname","key ring");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable key ring","key ring");
        t.set("Uname","key ring");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);
        
        t = Lib.extend("scroll case","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_SCROLLCASE);
        t.set("Holds","IsScroll");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof scroll case","scroll case");
        t.set("Uname","scroll case");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof scroll case","scroll case");
        t.set("Uname","scroll case");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof scroll case","scroll case");
        t.set("Uname","scroll case");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof scroll case","scroll case");
        t.set("Uname","scroll case");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof scroll case","scroll case");
        t.set("Uname","scroll case");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable scroll case","scroll case");
        t.set("Uname","scroll case");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("book bag","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_BOOKBAG);
        t.set("Holds","IsBook");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof book bag","book bag");
        t.set("Uname","book bag");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof book bag","book bag");
        t.set("Uname","book bag");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof book bag","book bag");
        t.set("Uname","book bag");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof book bag","book bag");
        t.set("Uname","book bag");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof book bag","book bag");
        t.set("Uname","book bag");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable book bag","book bag");
        t.set("Uname","book bag");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("potion case","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_POTIONCASE);
        t.set("Holds","IsDrinkable");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof potion case","potion case");
        t.set("Uname","potion case");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof potion case","potion case");
        t.set("Uname","potion case");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof potion case","potion case");
        t.set("Uname","potion case");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof potion case","potion case");
        t.set("Uname","potion case");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof potion case","potion case");
        t.set("Uname","potion case");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable potion case","potion case");
        t.set("Uname","potion case");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("food sack","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_FOODSACK);
        t.set("Holds","IsEdible");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof food sack","food sack");
        t.set("Uname","food sack");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof food sack","food sack");
        t.set("Uname","food sack");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof food sack","food sack");
        t.set("Uname","food sack");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof food sack","food sack");
        t.set("Uname","food sack");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof food sack","food sack");
        t.set("Uname","food sack");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable food sack","food sack");
        t.set("Uname","food sack");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("ingredient pouch","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_INGREDIENTPOUCH);
        t.set("Holds","IsIngredient");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof ingredient pouch","ingredient pouch");
        t.set("Uname","ingredient pouch");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof ingredient pouch","ingredient pouch");
        t.set("Uname","ingredient pouch");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof ingredient pouch","ingredient pouch");
        t.set("Uname","ingredient pouch");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof ingredient pouch","ingredient pouch");
        t.set("Uname","ingredient pouch");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof ingredient pouch","ingredient pouch");
        t.set("Uname","ingredient pouch");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable ingredient pouch","ingredient pouch");
        t.set("Uname","ingredient pouch");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("quiver","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_QUIVER);
        t.set("Holds","IsArrow");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof quiver","quiver");
        t.set("Uname","quiver");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof quiver","quiver");
        t.set("Uname","quiver");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof quiver","quiver");
        t.set("Uname","quiver");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof quiver","quiver");
        t.set("Uname","quiver");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof quiver","quiver");
        t.set("Uname","quiver");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable quiver","quiver");
        t.set("Uname","quiver");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("jewelry case","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_JEWELRYCASE);
        t.set("Holds","IsRing");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof jewelry case","jewelry case");
        t.set("Uname","jewelry case");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof jewelry case","jewelry case");
        t.set("Uname","jewelry case");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof jewelry case","jewelry case");
        t.set("Uname","jewelry case");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof jewelry case","jewelry case");
        t.set("Uname","jewelry case");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof jewelry case","jewelry case");
        t.set("Uname","jewelry case");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable jewelry case","jewelry case");
        t.set("Uname","jewelry case");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("rune bag","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_RUNEBAG);
        t.set("Holds","IsRune");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof rune bag","rune bag");
        t.set("Uname","rune bag");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof rune bag","rune bag");
        t.set("Uname","rune bag");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof rune bag","rune bag");
        t.set("Uname","rune bag");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof rune bag","rune bag");
        t.set("Uname","rune bag");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof rune bag","rune bag");
        t.set("Uname","rune bag");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable rune bag","rune bag");
        t.set("Uname","rune bag");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("wand case","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_WANDCASE);
        t.set("Holds","IsWand");
        t.set("LevelMin",3);
        Lib.add(t);

        t = Lib.extend("waterproof wand case","wand case");
        t.set("Uname","wand case");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof wand case","wand case");
        t.set("Uname","wand case");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof wand case","wand case");
        t.set("Uname","wand case");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof wand case","wand case");
        t.set("Uname","wand case");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof wand case","wand case");
        t.set("Uname","wand case");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable wand case","wand case");
        t.set("Uname","wand case");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

        t = Lib.extend("bag of holding","base container");
        t.set("Image",142);
        t.set("WieldType", RPG.WT_HOLDING);
        t.set("IsFeatherWeight",1);
        t.set("Holds","IsItem");
        t.set("LevelMin",20);
        Lib.add(t);
        
        t = Lib.extend("waterproof bag of holding","bag of holding");
        t.set("Uname","bag of holding");
        t.set("RES:water",1000);
        t.set("LevelMin",4);
        Lib.add(t);

        t = Lib.extend("fireproof bag of holding","bag of holding");
        t.set("Uname","bag of holding");
        t.set("RES:fire",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("acidproof bag of holding","bag of holding");
        t.set("Uname","bag of holding");
        t.set("RES:acid",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("frostproof bag of holding","bag of holding");
        t.set("Uname","bag of holding");
        t.set("RES:ice",1000);
        t.set("RES:chill",1000);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("theftproof bag of holding","bag of holding");
        t.set("Uname","bag of holding");
        t.set("TheftProof",1);
        t.set("LevelMin",10);
        Lib.add(t);

        t = Lib.extend("invulnerable bag of holding","bag of holding");
        t.set("Uname","bag of holding");
        t.set("IsDestructible",0);
        t.set("LevelMin",20);
        Lib.add(t);

    }
}
