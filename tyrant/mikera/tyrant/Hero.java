package tyrant.mikera.tyrant;

import java.util.*;

import javelin.model.BattleMap;

import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;



/**
 * Hero-specific code and routines
 * 
 * Most importantly, hero creation through Hero.createHero(...)
 * 
 * Note that experience is currently only applicable to the hero,
 * but this may change in future revisions.
 * 
 * @author Mike
 *
 */
public class Hero {
	public static Thing createBaseHero(String race) {
		Thing h=Lib.extend("you",race);
		h.set("IsHero",1);
		h.set("Gods", Lib.instance().getObject("Gods"));
		
		h.set("IsGiftReceiver",0); // don't count as gift receiver
		h.set("Image", 7);
		h.set("Z", Thing.Z_MOBILE+5);
		h.set(RPG.ST_RECHARGE, 60);
		h.set(RPG.ST_REGENERATE, 20);
		h.set("HungerThreshold", 300000);
		h.set("NameType", Description.NAMETYPE_PROPER);
		h.set("OnAction",new HeroAction());
		h.set("Seed",RPG.r(1000000000));
		h.set("ASCII","@");
		h.set("IsDisplaceable",0);
		
		// Starting Game stats
		h.set(RPG.ST_LEVEL, 1);
		h.incStat(RPG.ST_SKILLPOINTS,1);
		
		// dummy map
		BattleMap m=new BattleMap(1,1);
		m.addThing(h,0,0);
		
		return h;
	}
	
	public static void addDebugModifications(Thing h) {
		h.set("IsImmortal",1);
		h.set("IsDebugMode",1);
		
		if ("QuickTester".equals(h.getString("HeroName"))) {
			h.addThing(Spell.create("Annihilation"));
			h.addThing(Spell.create("Blaze"));
			h.addThing(Spell.create("Ultimate Destruction"));
			Wish.makeWish("skills",100);
		}
		
	}
	
	public static void setHeroName(Thing h, String name) {
		h.set("HeroName",name);
	}
	
	public static Thing createHero(String name, String race, String profession) {	

		Thing h=createBaseHero(race);
		Game.instance().initialize(h);
        
		if ((name==null)||(name.equals(""))) name="Tester";
		setHeroName(h,name);
		
		// Debug mode modifications
		if (Game.isDebug()) {
			addDebugModifications(h);
		}
		
		// Race Modifications
		applyRace(h,race);

		// Professions
		applyProfession(h,profession);
		
		// Bonus items based on skills
		applyBonusItems(h);

		// set up HPS and MPS
		h.set(RPG.ST_HPSMAX, h.getBaseStat(RPG.ST_TG)+RPG.d(6));
		h.set(RPG.ST_MPSMAX, h.getBaseStat(RPG.ST_WP)+RPG.d(6));

		h.set(RPG.ST_HPS, h.getStat(RPG.ST_HPSMAX));
		h.set(RPG.ST_MPS, h.getStat(RPG.ST_MPSMAX));
		
		Being.utiliseItems(h);
		
		h.addThing(Lib.create("portal stone"));
		
		Wish.makeWish("id",100);

		// score starts at zero
		h.set("Score",0);
		
		// religion
		ArrayList gods=Gods.getPossibleGods(h);
		int gl=gods.size();
		if (gl>0) {
			h.set("Religion",gods.get(RPG.r(gl)));
		} else {
			Game.warn("No religion available for "+race+" "+profession);
		}
		
		createHeroHistory(h);
		
		// performance improvement with flattened properties
		h.flattenProperties();
		
		return h;
	}


	public static void improveSlightly(Thing h) {
		h.incStat(RPG.ST_MOVESPEED, 100);
		h.incStat(RPG.ST_ATTACKSPEED, 100);
		h.incStat("ARM", 10);
		h.incStat(RPG.ST_REGENERATE, 50);
		h.incStat(RPG.ST_RECHARGE, 50);
	}


	public static int getHunger(Thing t) {
		int hunger = t.getStat(RPG.ST_HUNGER);
		return (hunger >= t.getStat("HungerThreshold")) ? 1 : 0;
	}


	// add a Quest to the hero
	public static void addQuest(Quest q) {
		Quest.getQuests().add(q);
	}

	public static BattleMap worldMap() {
		return (BattleMap)Game.hero().get("WorldMap");
	}

	/**
	 * This function awards some bonus items based on the hero's 
	 * professional skills.
	 * 
	 * @param h
	 */
	private static void applyBonusItems(Thing h) {
		if (h.getFlag(Skill.PRAYER)) {
			Thing t=Lib.create("potion of holy water");
			t.set("Number",h.getStat(Skill.PRAYER));
			h.addThingWithStacking(t);
		}
		
		if (h.getFlag(Skill.TRADING)) {
			Thing t=Lib.create("sovereign");
			t.set("Number",h.getStat(Skill.TRADING));
			h.addThingWithStacking(t);
		}
		
		if (h.getFlag(Skill.WEAPONLORE)) {
			Thing t=Lib.createType("IsWeapon",RPG.d(2,6));
			h.addThingWithStacking(t);
		}
		
		if (h.getFlag(Skill.COOKING)) {
			Thing t=Lib.createType("IsFood",25);
			t.set("Number",h.getStat(Skill.COOKING));
			h.addThingWithStacking(t);
		}
		
		if (h.getFlag(Skill.ARCHERY)) {
//			 a reanged weapon + ammo
			Thing t=Lib.createType("IsRangedWeapon",RPG.d(h.getStat(Skill.ARCHERY),6));
			Thing ms=RangedWeapon.createAmmo(t,RPG.d(h.getStat(Skill.ARCHERY),6));
			h.addThing(t);
			h.addThing(ms);
		}
		
		Thing[] ws=h.getFlaggedContents("IsWeapon");
		if (ws.length==0) {
			h.addThing(Lib.createType("IsWeapon",1));
		}
	}
	
	private static void applyProfession(Thing h, String p) {
		h.set("Profession",p);
		
		if (p.equals("fighter")) {
			h.set("Image",7);
			
			h.incStat(RPG.ST_SK, RPG.r(5));
			h.incStat(RPG.ST_ST, RPG.r(5));
			h.incStat(RPG.ST_AG, RPG.r(4));
			h.incStat(RPG.ST_TG, RPG.r(6));
			h.incStat(RPG.ST_IN, RPG.r(0));
			h.incStat(RPG.ST_WP, RPG.r(0));
			h.incStat(RPG.ST_CH, RPG.r(0));
			h.incStat(RPG.ST_CR, RPG.r(0));
			h.incStat(Skill.ATTACK, 1);
			h.incStat(Skill.DEFENCE, 1);
			h.incStat(Skill.UNARMED, RPG.d(2));
			h.incStat(Skill.WEAPONLORE, RPG.d(2));
			
		} else if (p.equals("wizard")) {
			h.set("Image",6);

			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(0)-1);
			h.incStat(RPG.ST_IN, RPG.r(7));
			h.incStat(RPG.ST_WP, RPG.r(5));
			h.incStat(RPG.ST_CH, RPG.r(2));
			h.incStat(RPG.ST_CR, RPG.r(4));
			h.incStat(Skill.IDENTIFY, RPG.r(2));
			h.incStat(Skill.LITERACY, RPG.d(3));
			h.incStat(Skill.TRUEMAGIC, RPG.d(2));
			h.incStat(Skill.CASTING, RPG.d(2));
			h.incStat(Skill.FOCUS,RPG.d(2));
			h.addThing(Spell.randomOffensiveSpell(Skill.TRUEMAGIC,3));
			
			h.incStat("Luck",-5);
			
			// book and scroll
			h.addThing(SpellBook.create(Skill.TRUEMAGIC,RPG.d(6)));
			h.addThing(Lib.create("[IsScroll]"));

		} else if (p.equals("shaman")) {
			h.set("Image",6);

			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(0));
			h.incStat(RPG.ST_IN, RPG.r(4));
			h.incStat(RPG.ST_WP, RPG.r(4));
			h.incStat(RPG.ST_CH, RPG.r(5));
			h.incStat(RPG.ST_CR, RPG.r(6));
			h.incStat(Skill.IDENTIFY, RPG.r(2));
			h.incStat(Skill.LITERACY, RPG.r(2));
			h.incStat(Skill.BLACKMAGIC, RPG.d(3));
			h.incStat(Skill.CASTING, RPG.d(2));
			h.incStat(Skill.HERBLORE, RPG.d(2));
			h.addThing(Spell.randomSpell(Skill.BLACKMAGIC,3));
			
			h.incStat("Luck",15);
			
			// herbs and monster parts
			for (int i=0; i<10; i++) h.addThingWithStacking(Lib.createType("IsHerb",RPG.d(10)));
			for (int i=0; i<6; i++) h.addThingWithStacking(Lib.createType("IsMonsterPart",RPG.d(10)));
			
		} else if (p.equals("witch")) {
			h.set("Image",32);

			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(0));
			h.incStat(RPG.ST_IN, RPG.r(5));
			h.incStat(RPG.ST_WP, RPG.r(4));
			h.incStat(RPG.ST_CH, RPG.r(4));
			h.incStat(RPG.ST_CR, RPG.r(6));
			h.incStat(Skill.LITERACY, 1);
			h.incStat(Skill.BLACKMAGIC, 1);
			h.incStat(Skill.CASTING, 1);
			h.incStat(Skill.HERBLORE, RPG.r(3));
			h.incStat(Skill.COOKING, RPG.r(3));
			
			for (int i=0; i<10; i++) h.addThingWithStacking(Lib.createType("IsHerb",RPG.d(10)));
			h.addThing(Spell.randomSpell(Skill.BLACKMAGIC,3));
			
			h.incStat("Luck",10);
			
			h.addThing(Lib.create("[IsScroll]"));
			h.addThing(SpellBook.create(Skill.BLACKMAGIC,RPG.d(8)));
			
		} else if (p.equals("war-mage")) {
			h.set("Image",6);

			h.incStat(RPG.ST_SK, RPG.r(2));
			h.incStat(RPG.ST_ST, RPG.r(2));
			h.incStat(RPG.ST_AG, RPG.r(2));
			h.incStat(RPG.ST_TG, RPG.r(2));
			h.incStat(RPG.ST_IN, RPG.r(2));
			h.incStat(RPG.ST_WP, RPG.r(4));
			h.incStat(RPG.ST_CH, RPG.r(2));
			h.incStat(RPG.ST_CR, RPG.r(4));
			h.incStat(Skill.LITERACY, 1);
			h.incStat(Skill.TRUEMAGIC, RPG.r(3));
			h.incStat(Skill.HEALING, RPG.d(2));
			h.incStat(Skill.CASTING, RPG.d(2));
			h.addThing(Spell.randomSpell(Skill.TRUEMAGIC,3));
			
			h.incStat("Luck",0);
			
		} else if (p.equals("runecaster")) {
			h.set("Image",6);

			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(0));
			h.incStat(RPG.ST_IN, RPG.r(6));
			h.incStat(RPG.ST_WP, RPG.r(5));
			h.incStat(RPG.ST_CH, RPG.r(4));
			h.incStat(RPG.ST_CR, RPG.r(8));
			h.incStat(Skill.ALCHEMY, RPG.r(3));
			h.incStat(Skill.HERBLORE, RPG.r(2));
			h.incStat(Skill.IDENTIFY, RPG.d(2));
			h.incStat(Skill.LITERACY, RPG.d(4) );
			h.incStat(Skill.RUNELORE, RPG.d(2));
			
			h.incStat("Luck",-10);
			
			{
				Thing n=Lib.create("scroll of Teleport Self");
				Item.identify(n);
				h.addThing(n);
			}
			
			{
				Thing n=Lib.createType("IsWeaponRunestone",RPG.d(17));
				Item.identify(n);
				h.addThing(n);
			}
			
			for (int i=RPG.d(6); i>0; i--) {
				Thing n=Lib.createType("IsRunestone",RPG.d(10));
				Item.identify(n);
				h.addThing(n);
			}
			
			for (int i=RPG.r(3); i>0; i--) {
				Thing n=Lib.createType("IsRuneRecipeScroll",RPG.d(10));
				Item.identify(n);
				h.addThing(n);
			}
			
		} else if (p.equals("priest")) {
			h.set("Image",11);


			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(3));
			h.incStat(RPG.ST_IN, RPG.r(4));
			h.incStat(RPG.ST_WP, RPG.r(8));
			h.incStat(RPG.ST_CH, RPG.r(5));
			h.incStat(RPG.ST_CR, RPG.r(0));

			h.incStat(Skill.PRAYER, RPG.d(2));
			h.incStat(Skill.LITERACY, RPG.d(2));
			h.incStat(Skill.HOLYMAGIC, RPG.d(2));
			h.incStat(Skill.HEALING, RPG.r(3));
			h.incStat(Skill.MEDITATION, RPG.r(2));
			h.incStat(Skill.FOCUS, RPG.r(2));
			h.incStat("Luck",5);

			
			h.addThing(Spell.randomSpell(Skill.HOLYMAGIC,5));
			
			Thing n=Lib.create("potion of healing");
			Item.identify(n);
			h.addThing(n);
			
		} else if (p.equals("healer")) {
			h.set("Image",11);


			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(3));
			h.incStat(RPG.ST_IN, RPG.r(4));
			h.incStat(RPG.ST_WP, RPG.r(4));
			h.incStat(RPG.ST_CH, RPG.r(5));
			h.incStat(RPG.ST_CR, RPG.r(4));

			h.incStat(Skill.IDENTIFY, RPG.d(2));
			h.incStat(Skill.LITERACY, RPG.d(2));
			h.incStat(Skill.HEALING, RPG.d(3));
			h.incStat(Skill.HERBLORE, RPG.d(2));
			h.incStat(Skill.MEDITATION, RPG.r(2));
			h.incStat(Skill.FOCUS, RPG.r(2));
			h.incStat("Luck",15);
			
			Thing n=Lib.create("potion of healing");
			Item.identify(n);
			h.addThing(n);
			h.addThing(Lib.create("potion of healing"));
			h.addThing(Lib.create("potion of healing"));
			h.addThing(Lib.createType("IsHerb",RPG.d(10)));
			h.addThing(Lib.createType("IsHerb",RPG.d(10)));
			h.addThing(Lib.createType("IsHerb",RPG.d(10)));
			h.addThing(Lib.createType("IsHerb",RPG.d(10)));
			h.addThing(Lib.createType("IsHerb",RPG.d(10)));
			
		} else if (p.equals("bard")) {
			h.set("Image",7);


			h.incStat(RPG.ST_SK, RPG.r(3));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(3));
			h.incStat(RPG.ST_TG, RPG.r(0));
			h.incStat(RPG.ST_IN, RPG.r(3));
			h.incStat(RPG.ST_WP, RPG.r(3));
			h.incStat(RPG.ST_CH, RPG.r(6));
			h.incStat(RPG.ST_CR, RPG.r(6));

			h.incStat(Skill.MUSIC, RPG.po(0.5));
			h.incStat(Skill.PERCEPTION, 1);
			h.incStat(Skill.SLEIGHT, RPG.po(0.5));
			h.incStat(Skill.STORYTELLING, RPG.po(0.5));
			h.incStat(Skill.SEDUCTION, RPG.po(1.2));
			h.incStat(Skill.LITERACY, RPG.po(0.8));		
			h.incStat("Luck",20);	
			
			Thing n=Lib.createType("IsRing",5);
			Item.identify(n);
			h.addThing(n);
			
		} else if (p.equals("paladin")) {
			h.set("Image",7);

			h.incStat(RPG.ST_SK, RPG.r(4));
			h.incStat(RPG.ST_ST, RPG.r(4));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(4));
			h.incStat(RPG.ST_IN, RPG.r(4));
			h.incStat(RPG.ST_WP, RPG.r(4));
			h.incStat(RPG.ST_CH, RPG.r(0));
			h.incStat(RPG.ST_CR, RPG.r(0));

			h.incStat(Skill.PRAYER, RPG.d(2));
			h.incStat(Skill.ATTACK, RPG.r(2));
			h.incStat(Skill.DEFENCE, RPG.r(2));
			h.incStat(Skill.BRAVERY, RPG.d(3));
			
			h.addThing(Weapon.createWeapon(RPG.d(4)));
			Thing n=Lib.createType("IsWand",RPG.d(4));
			Item.identify(n);
			h.addThing(n);

			
		} else if (p.equals("barbarian")) {
			h.set("Image",3);


			h.incStat(RPG.ST_SK, RPG.r(3));
			h.incStat(RPG.ST_ST, RPG.r(3));
			h.incStat(RPG.ST_AG, RPG.r(5));
			h.incStat(RPG.ST_TG, RPG.r(5));
			h.incStat(RPG.ST_IN, RPG.r(0));
			h.incStat(RPG.ST_WP, RPG.r(0));
			h.incStat(RPG.ST_CH, RPG.r(0));
			h.incStat(RPG.ST_CR, RPG.r(0));
			h.incStat(RPG.ST_ATTACKSPEED, 10);

			h.incStat(Skill.ATTACK, RPG.r(2));
			h.incStat(Skill.FEROCITY, 1);
			h.incStat(Skill.ATHLETICS, 1);
			h.incStat(Skill.ALERTNESS, RPG.r(3));
			h.incStat(Skill.SURVIVAL, RPG.d(2));
			h.incStat(Skill.PICKPOCKET, RPG.r(2));
			h.incStat(Skill.UNARMED, 1);
			h.incStat(Skill.TRACKING, RPG.r(2));

			Thing n=Lib.create("potion of speed");
			Item.identify(n);
			h.addThing(n);
			
		} else if (p.equals("thief")) {
			h.set("Image",10);


			h.incStat(RPG.ST_SK, RPG.r(5));
			h.incStat(RPG.ST_ST, RPG.r(0));
			h.incStat(RPG.ST_AG, RPG.r(6));
			h.incStat(RPG.ST_TG, RPG.r(0));
			h.incStat(RPG.ST_IN, RPG.r(0));
			h.incStat(RPG.ST_WP, RPG.r(0));
			h.incStat(RPG.ST_CH, RPG.r(4));
			h.incStat(RPG.ST_CR, RPG.r(3));
			h.incStat(RPG.ST_ATTACKSPEED, 120);

			h.incStat(Skill.ALERTNESS, RPG.d(3));
			h.incStat(Skill.PICKPOCKET, RPG.r(3));
			h.incStat(Skill.PICKLOCK, RPG.r(3));
			h.incStat(Skill.DISARM, RPG.r(2));

			Thing n=Lib.create("wand of Teleport Self");
			Item.identify(n);
			h.addThing(n);
			
		} else if (p.equals("ranger")) {
			h.set("Image",10);


			h.incStat(RPG.ST_SK, RPG.r(8));
			h.incStat(RPG.ST_ST, RPG.r(3));
			h.incStat(RPG.ST_AG, RPG.r(6));
			h.incStat(RPG.ST_TG, RPG.r(0));
			h.incStat(RPG.ST_IN, RPG.r(0));
			h.incStat(RPG.ST_WP, RPG.r(0));
			h.incStat(RPG.ST_CH, RPG.r(4));
			h.incStat(RPG.ST_CR, RPG.r(0));

			h.incStat(Skill.ARCHERY, RPG.r(3));
			h.incStat(Skill.THROWING, RPG.r(3));
			h.incStat(Skill.SURVIVAL, RPG.d(1));
			h.incStat(Skill.SWIMMING, RPG.r(2));
			h.incStat(Skill.RIDING, RPG.r(2));
			h.incStat(Skill.TRACKING, RPG.d(3));
			
			Thing n=Lib.create("healing potion");
			h.addThing(n);

		} else if (p.equals("farmer")) {
			h.set("Image",10);


			h.incStat(RPG.ST_SK, RPG.r(0));
			h.incStat(RPG.ST_ST, RPG.r(4));
			h.incStat(RPG.ST_AG, RPG.r(0));
			h.incStat(RPG.ST_TG, RPG.r(4));
			h.incStat(RPG.ST_IN, RPG.r(0));
			h.incStat(RPG.ST_WP, RPG.r(6));
			h.incStat(RPG.ST_CH, RPG.r(4));
			h.incStat(RPG.ST_CR, RPG.r(6));

			h.incStat(Skill.THROWING, RPG.r(3));
			h.incStat(Skill.SURVIVAL, RPG.r(3));
			h.incStat(Skill.SWIMMING, RPG.r(2));
			h.incStat(Skill.COOKING, RPG.r(3));
			h.incStat(Skill.HERBLORE, RPG.d(2));
			
			// a healing potion
			Thing n=Lib.create("potion of healing");
			Item.identify(n);
			h.addThing(n);
		} else {
			throw new Error("Profession ["+p+"] not recognised");
		}	
	}
	
	
	private static void applyRace(Thing h, String r) {
		if (r.equals("human")) {
			// humans are the most common inhabitants in the world of Tyrant
			// they are good all-round characters

			Coin.addMoney(h,10 * RPG.d(4, 10));
			h.addThing(Lib.create("[IsDagger]"));
			h.addThing(Lib.create("[IsFood]"));
		} else if (r.equals("dwarf")) {
			// dwarves are sturdy and industrious cave dwellers
			// they are famed for their skill in smithing and mining

			Coin.addMoney(h,10 * RPG.d(5, 10));

			h.addThing(Lib.create("iron hand axe"));
			h.addThing(Lib.create("loaf of dwarf bread"));
		} else if (r.equals("hobbit")) {
			// hobbits are just three feet high
			// they are peaceful folk, renowned as farmers
			Coin.addMoney(h,RPG.d(6, 10));
			h.addThing(Lib.create("stone knife"));
			h.addThing(Lib.create("[IsFood]"));
			h.addThing(Lib.create("[IsFood]"));
			h.addThing(Lib.create("[IsFood]"));
			h.addThing(Lib.create("[IsEquipment]"));
			h.addThing(Lib.create("sling"));
			h.addThing(Lib.create("10 pebble"));
		} else if (r.equals("high elf")) {
			// high elves are noble and wise

			Coin.addMoney(h,10 * RPG.d(6, 10));
			h.addThing(Lib.create("ornate mithril ring"));
			h.addThing(Lib.create("elven steel dagger"));
		} else if (r.equals("wood elf")) {
			// wood elves are shy of other races
			// they are agile and talented archers
			h.addThing(Lib.create("short bow"));
			h.addThing(Lib.create("lesser elven arrow"));
		} else if (r.equals("dark elf")) {
			// dark elves are vicious and powerful
			// they prefer throwing weapons, darts and shurikens

			h.addThing(Lib.create("iron dagger"));
			h.addThing(Lib.create("[IsPotion]"));
		} else if (r.equals("gnome")) {
			// gnomes are disadvantage by their small size
			// they make up for this with igenuity

			Thing n=Lib.createType("IsMagicItem",5);
			Item.identify(n);
			h.addThing(n);
			Coin.addMoney(h,100 * RPG.d(10, 10));

			
		} else if (r.equals("half orc")) {
			// half orcs are volatile and dangerous

			
			h.addThing(Lib.createType("IsWeapon",RPG.d(3)));
			h.addThing(Lib.create("[IsMeat]"));
			Coin.addMoney(h,RPG.d(4, 10));
		
		} else if (r.equals("half troll")) {
			// trolls are lumbering hunks of muscle
			// with fearsome regenerative powers
			// they are not very bright

			h.incStat(RPG.ST_SKILLPOINTS,-1);

			h.addThing(Lib.createType("IsClub",RPG.d(6)));
			h.addThing(Lib.create("[IsMeat]"));
			h.addThing(Lib.create("[IsMeat]"));
			h.addThing(Lib.create("meat ration"));
		
		} else if (r.equals("argonian")) {
			// some equipment
			// in line with argonian style
			h.addThing(Lib.create("[IsTrident]"));
			h.addThing(Lib.create("[IsMeat]"));
			h.addThing(Lib.create("[IsFish]"));
		
		} else if (r.equals("hawken")) {
			//some equipment
			// in line with hawken style
			h.addThing(Lib.create("[IsDagger]"));
			h.addThing(Lib.create("[IsMeat]"));
			Coin.addMoney(h,RPG.d(4, 10));	
		
        } else if (r.equals("pensadorian")) {
            //some equipment
            // in line with pensadorian style
            h.addThing(Lib.create("[IsDagger]"));
            h.addThing(Lib.create("[IsFruit]"));
            Coin.addMoney(h,RPG.d(4, 10));	

		} else {
			
			throw new Error("Race ["+r+"] not recognised");
		}		
	}


	private static final int baseLevelCost=40;
	private static final double experienceDecay=0.98;
	private static final double experienceLevelCost=Math.sqrt(2);
	private static final double experienceLevelMultiplier=experienceLevelCost*0.97;
	
	/*
	 * Gives a number of experience points to the hero
	 */
	public static void gainExperience(int x) {
		Thing h=Game.hero();
		// if (QuestApp.debug) Game.warn("You gain "+x+" experience points");
		int exp = h.getBaseStat(RPG.ST_EXP) + x;
		int level = h.getBaseStat(RPG.ST_LEVEL);
		
		int requiredForNextLevel = calcXPRequirement(level + 1);
        while (exp >= requiredForNextLevel) {
            if (level < 50) {
                Being.gainLevel(h);
                exp -= requiredForNextLevel;
                level++; 
                requiredForNextLevel = calcXPRequirement(level + 1);
            } else {
                exp = requiredForNextLevel - 1;
            }
        }
		h.set(RPG.ST_EXP, exp);
	}
	
	/**
	 * Calculate the number of experience points required 
	 * to gain a particular level
	 * 
	 * @param level Target level
	 * @return Number of experience points required
	 */
	public static int calcXPRequirement(int level) {
		double c=baseLevelCost;
		c=c*Math.pow(experienceLevelCost,level-2);
		return (int)c;
	}
	
	public static String reportKillData() {
		HashMap hm=getKillHashMap();
		
		ArrayList al=new ArrayList(hm.keySet());
		
		Collections.sort(al);
		
		StringBuffer sb=new StringBuffer();
		
		sb.append("You have killed the following creatures:\n");
		sb.append("\n");
		
		boolean uniques=true;
		
		for (Iterator it=al.iterator(); it.hasNext();) {
			String name=(String)it.next();
			
			// display line after uniques
			if (uniques&&(!Character.isUpperCase(name.charAt(0)))) {
				uniques=false;
				sb.append("\n");
			}
			
			Integer count=(Integer)(hm.get(name));
			String g=Text.centrePad(name,count.toString(),60);
			sb.append(g+"\n");
		}
		
		return sb.toString();
	}
	
	public static int getKillCount(Thing t) {
		HashMap hm=getKillHashMap();
		Integer i=(Integer)hm.get(t.name());
		
		if (i==null) return 0;
		return i.intValue();
	}
	
	private static HashMap getKillHashMap() {
		Thing h=Game.hero();
		HashMap hm=(HashMap)h.get("Kills");
		if (hm==null) {
			hm=new HashMap();
			h.set("Kills",hm);
		}
		return hm;
	}
	
	public static int incKillCount(Thing t) {
		HashMap hm=getKillHashMap();
		String name=t.name();
		Integer i=(Integer)hm.get(name);
		if (i==null) {
			i=new Integer(0);
		} 
		int newKillCount=i.intValue()+1;
		hm.put(name,new Integer(newKillCount));
		
		return newKillCount;
	}
	
	public static int gainKillExperience(Thing h,Thing t) {
		int hlevel=h.getLevel();
		int tlevel=t.getLevel();
		
		int killcount=0;
		if (h.isHero()) {
			killcount=incKillCount(t);
			if (killcount==1) {
				Score.scoreFirstKill(t);
			}
		}

		int base=t.getStat("XPValue");
		double xp=base;
		xp=xp*Math.pow(experienceDecay,killcount);
		xp=xp*Math.pow(experienceLevelMultiplier,tlevel-1);
		
		// decrease xp gain for killing lower level monsters
		if (hlevel>tlevel) xp=xp/(hlevel-tlevel);
		
		int gain=(int)xp;
		
		Hero.gainExperience(gain);
		return gain;
	}
	
	private static String[] hungerDecayStats = new String[] {
			"SK","ST","AG","TG","IN","WP","CH","CR"
	};
	
	
	// hero names used for search
	private static final String[] races = {"human", "hobbit",
			"dwarf", "high elf", "wood elf", "dark elf", "gnome", "half orc",
			"half troll","hawken","argonian","pensadorian"};
	
	public static String[] heroRaces() {
		return races;
	}
	
	public static String[] heroRaceDescriptions(){
		String[] races=heroRaces();
		String[] descs=new String[races.length];
		for (int i=0; i<descs.length; i++) {
			String d=Lib.create(races[i]).getString("RaceDescription");
			if (d==null) d="No description for "+races[i]+" yet...";
			descs[i]=d;
		}
		return descs;
	}
	
	public static String[] heroProfessionDescriptions(String race) {
		String [] profs=heroProfessions(race);
		
		java.util.HashMap pds=new java.util.HashMap();
		
		pds.put("fighter","Fighters are trained in all aspects of combat. Whether they work in the service of a powerful lord or travel alone as adventurers, they are formidable in battle and can expect to earn a good living from their valuable skills in turbulent times such as these. Fighters often join brotherhoods and guilds where they form bonds of loyalty and friendship.");
		pds.put("wizard","By the mastery of arcane forces, wizards are able to summon supernatural powers to their aid by casting spells. The secrets of their craft are closely guarded, and only a few wizards are ever able to master the most powerful spells. Many aspire to the ultimate honour of joining the Council of The Archmagi, and the ultimate prize of immortality.");
		pds.put("priest","Priests dedicate their lives to the service of their God. They seek to build temples, help the needy and convert followers to the glory of God, and are always ready to take up arms to strike down blasphemers and heretics. As priests gain power, they are often granted divine aid to aid them in their quests. To serve their God is the only reward they need.");
		pds.put("thief","Thieves are cunning and ruthless, willing to break a few rules to get what they want. Their skills are highly prized among adventurers, since not everything can be achieved by brute force alone. Their ability to enter guarded areas and obtain items unnoticed can be highly lucrative. Of course, theives would do well to conceal their profession in the company of civilized folk.");
		pds.put("ranger","Roaming the wilderness, rangers are happiest when they are in the great outdoors. Some serve armies and settlements as scouts, archers and skirmishers where their speed and mobility can give a crucial tactical adventage. Alternatively, many simply enjoy their freedom and live by hunting and trading in the wilds.");
		pds.put("paladin","Called to the service of their God, paladins are holy warriors tasked with a divine mission to destroy the forces of heretics and all other foes of their God. Like priests, they are able to pray for divine aid and are often answered. Driven by their dedication and promise of infinte rewards in paradise, they are suprememly brave and will willingly fight fiercely against impossible odds.");
		pds.put("barbarian","Raised far from civilization in tribes on the edge of the unknown, barbarians have long gravitated towards the riches of civilization, tempted by plunder and a thirst to gain immortal power and glory. Tough and agile, barbarians are more at home in the wilds than in towns, but their skills as adventurers are second to none.");
		pds.put("witch","Granted mysterious powers by the passing down of dark secrets through many generations, witches are able to practice Black Magic. Their spells and curses are cast with the aid of gathered ingredients and mystic rites, and can have devastating effects. Nevertheless, witches know the danger of dabbling too much and prefer to conceal their powers.");
		pds.put("war-mage","Some rare and talented individuals show an aptitude for both fighting and magic. This elite few are perhaps the most deadly foes to face, able to respond with both force and magic. Keen to exploit their talents, many become well-paid mercenaries, working for the highest bidder until they are able to build an army of their own.");
		pds.put("shaman","Trained from an early age in Black Magic by the elders of their tribes, shamen are marked out as spiritual leaders and holy warriors. As a last rite of passage, they must leave their tribe and travel alone as adventurers until their skills are perfected and they have cheated death a thousand times. Only the wisest and most skilled return, and they are deemed truly worthy of rule.");
		pds.put("runecaster","Since ancient times, runes have been revered for their mysterious powers. Runecasters have learnt to control them. But even they have little understanding of the mysteries behind the runes, on which subject even the Gods have remained strangely silent. Obsessed by their craft, Runecasters seek the perfection of their skills and the answer to the Ultimate Question.");
		pds.put("healer","Their remarkeble gift enables Healers to cure the sick and wounded. Beloved and revered by common folk for the selfless help that they give, healers can live well from the gratitude of those whose loved ones they save. They are also welcomed by armies and bands of adventurers who desperately need their skills after every battle. Even so, healers are motivated first and foremost by compassion.");
		pds.put("bard","Bards have always travelled and revelled in the company of those they meet upon the way. They are natural entertainers and the best of them can be showered in gold for displaying their talents. They often join bands of adventurers, enjoying the thrills of adventure, and more importantly, gaining new and fantastic tales to tell when next they find a hospiable inn to entertain.");
		
		String[] result=new String[profs.length];
		for (int i=0; i<profs.length; i++) {
			String pd=(String)pds.get(profs[i]);
			if (pd==null) {
				pd="No description for "+profs[i]+" yet...";
			}
			result[i]=pd;
		}
		return result;
	}
	
	public static String[] heroProfessions(String race) {
		String[] ps=new String[1000];
		int i=0;

		ps[i++]="fighter";
		ps[i++]="wizard";
		ps[i++]="priest";
		
		if (race.equals("hobbit")) {
			ps[i++]="thief";
			ps[i++]="farmer";
			ps[i++]="ranger";
			ps[i++]="healer";
		}
		
		if (race.equals("human")) {
			ps[i++]="bard";
			ps[i++]="thief";
			ps[i++]="paladin";
			ps[i++]="runecaster";
			ps[i++]="barbarian";
			ps[i++]="ranger";
			ps[i++]="witch";
		}
		
		if (race.equals("dwarf")) {
			ps[i++]="thief";
			ps[i++]="runecaster";
			ps[i++]="barbarian";
			ps[i++]="ranger";
		}
		
		if (race.equals("gnome")) {
			ps[i++]="thief";
			ps[i++]="runecaster";
			ps[i++]="bard";
			ps[i++]="healer";
		}
		
		if (race.equals("high elf")) {
			ps[i++]="paladin";
			ps[i++]="runecaster";
			ps[i++]="ranger";
			ps[i++]="healer";
		}	
		
		if (race.equals("wood elf")) {
			ps[i++]="bard";
			ps[i++]="ranger";
			ps[i++]="war-mage";
			ps[i++]="witch";
			ps[i++]="healer";
		}
		
		if (race.equals("dark elf")) {
			ps[i++]="paladin";
			ps[i++]="bard";
			ps[i++]="thief";
			ps[i++]="runecaster";
			ps[i++]="barbarian";
			ps[i++]="ranger";
			ps[i++]="witch";
		}
		
		if (race.equals("half orc")) {
			ps[i++]="shaman";
			ps[i++]="barbarian";
			ps[i++]="ranger";
			ps[i++]="war-mage";
		}
		
		if (race.equals("half troll")) {
			ps[i++]="shaman";
			ps[i++]="barbarian";
			ps[i++]="war-mage";
		}
		
		if (race.equals("argonian")) {
			ps[i++]="runecaster";
			ps[i++]="thief";
			ps[i++]="witch";
		}
		
		if (race.equals("hawken")) {
			ps[i++]="ranger";
			ps[i++]="barbarian";
			ps[i++]="war-mage";
			ps[i++]="bard";
		}
		
        if (race.equals("pensadorian")) {
            ps[i++]="runecaster";
            ps[i++]="shaman";
            ps[i++]="thief";
            ps[i++]="witch";
        }

		String[] result=new String[i];
		System.arraycopy(ps,0,result,0,i);
		return result;
	}
	
	private static String[] months={
			null,
			"Windalmar",
			"Ferrentir",
			"Antalyir",
			"Foolsdawn",
			"Whispertide",
			"Evermoor",
			"Brightsun",
			"Kingsmoon",
			"Harvestmoon",
			"Eberfest",
			"Witherfrost",
			"Hogmandark"
	};
	
	public static void createHeroHistory(Thing h) {
		StringBuffer sb=new StringBuffer();
		
		///////////////
		// Birth-day
		Calendar today = Calendar.getInstance();
		int day = today.get(Calendar.DAY_OF_MONTH);
		int month = today.get(Calendar.MONTH) + 1;
		String birthDay = (day) + "/" + (month);
		String dayString = Text.ordinal(day);
		String monthString = months[month];

		
		String r=h.getString("Race");
		sb.append("You are born "+(Text.isVowel(r.charAt(0))?"an":"a")+" "+r+" on the "+dayString+" of "+monthString+". ");
		
		if (birthDay.equals("14/2")) {
			sb.append("Everyone agreed you were a charming baby. ");
			h.incStat("CH",RPG.d(6));
			h.incStat(Skill.SEDUCTION,1);
		} 
		
		if (birthDay.equals("1/1")) {
			sb.append("A bright star shone in the sky when you were born. ");
			h.incStat("WP",RPG.d(6));
		} 
		
		sb.append("\n\n");
		
		//////////////////
		// childhood
		switch (RPG.d(7)) {
			case 1:
				sb.append("You had an unhappy childhood, finding it hard to relate to your peers.");
				h.incStat("WP",RPG.d(3));
				h.incStat("CH",-1);
				break;
			case 2:
				sb.append("You had a happy childhood, with supportive parents who taught you well.");
				h.incStat("IN",RPG.d(3));
				h.incStat("CH",-1);
				break;
			case 3:
				sb.append("You were always getting into trouble as a child, but somehow everything seemed to work out for you. Wise elders were convinced that fortune favoured you. ");
				h.incStat("Luck",5);
				break;
			case 4:
				sb.append("You enjoyed playing outdoors as a child, and excelled in particular at sports. Your peers developed a healthy respect for your talents.");
				h.incStat(Skill.ATHLETICS,1);
				h.incStat("CH",RPG.d(4));
				h.incStat("IN",-2);
				break;
			case 5:
				sb.append("You were badly behaved as a child. You bullied smaller children relentlessly. You came to lead an impressive gang, though they followed you more out of fear than respect.");
				h.incStat("CH",-1);
				h.incStat("IN",-3);
				h.incStat("ST",2);
				break;
			default:
				sb.append("You had an uneventful childhood, and yearned for adventure.");
				break;
		}
		sb.append("\n\n");
		
		/////////////
		// religion
		String god=h.getString("Religion");
		sb.append("You were brough up to worship "+god+". ");
		switch (RPG.d(5)) {
		case 1:
			sb.append("You avoided religious ceremonies, as you disliked your priest intensely. You even came to feel that he had laid a curse upon you. ");
			h.incStat("Luck",-4);
			break;
		case 2:
			sb.append("You were extremely devout. It caused you great anguish because you never felt that "+god+" was truly close to you. ");
			h.incStat("WP",1);
			h.incStat("CH",-1);
			
			break;
		case 3:
			sb.append("You were extremely devout, and your priest praised you for having earnt the blessing of "+god+". ");
			h.incStat("Luck",4);
			h.incStat(Skill.PRAYER,1);
			break;
		default:
			sb.append("You were not particularly devout, but still felt that "+god+" would protect you. ");
			break;
		}
		sb.append(Gods.get(god).getString("UpbringingText")+" ");
		sb.append("\n\n");
		
		////////////////
		// growing up
		switch (RPG.d(5)) {
			case 1:
				sb.append("As you grew up, you felt that you were destined for greatness. Everyone else thought that you were merely arrogant. ");
				h.incStat(RPG.ST_SKILLPOINTS,1);
				h.incStat("CH",-3);
				break;
			case 2:
				sb.append("As you grew up, you found yourself with the remarkable ability to learn creative skills. You had a tendency to dedicate too much time to creative pursuits at the expense of other activities. ");
				h.incStat(RPG.pick(new String[] {Skill.SMITHING,Skill.PAINTING,Skill.MUSIC,Skill.COOKING}),1);
				h.incStat("ST",-1);
				h.incStat("IN",-1);
			case 3:
				sb.append("Later in your youth, you fell hopelessly in love. Sadly, this was not returned. Heartbroken, you spent countless days wandering alone trying to fathom the meaning of life. ");
				h.incStat("CH",-1);
				h.incStat("ST",-1);
				h.incStat("IN",2);
				h.incStat(Skill.PERCEPTION,1);
				break;
			default:
				sb.append("You grew up without any particularly great events shaping your life. But you still knew that one day you would set out to achieve greatness. ");
				break;
		}
		sb.append("\n\n");
		
		/////////////
		// training
		String p=h.getString("Profession");
		sb.append("Determined to make something of your life, you began your "+p+" training as soon as you were old enough. ");
		switch (RPG.d(5)) {
			default:
				sb.append("You showed a good aptitude for your chosen career, and before too long your tutor proclaimed you as a fully trained "+p+".");
				break;
		}
		sb.append("\n\n");
		
		// ensure all stats are >=1
		String[] sks=Being.statNames();
		for (int i=0; i<sks.length; i++) {
			if (h.getStat(sks[i])<=0) {
				h.set(sks[i],1);
			}
		}
		
		h.set("HeroHistory",sb.toString());
	}
	
	public static class HeroAction extends Script {
		private static final long serialVersionUID = 3257571689436033328L;

        public boolean handle(Thing h, Event e) {
			int time=e.getStat("Time");
			Hero.action(h,time);
			Being.recover(h,time);

			return false;
		}
	}
	
	// can't do anything in monster action phase
	// but allow for hunger effects
	public static void action(Thing h,int t) {
		// hunger
		int hunger = h.getStat(RPG.ST_HUNGER);
		int hungerThreshold=h.getStat("HungerThreshold");
		hunger = RPG.min(hungerThreshold * 3, hunger + (t * 6)
				/ (6 + h.getStat(Skill.SURVIVAL)));
		h.set(RPG.ST_HUNGER, hunger);
		
		// bad things
		int hl=hunger/hungerThreshold;
		switch (hl) {
			case 0: case 1: 
				// OK
				break;
			case 2:
				for (int i=RPG.po(t,10000); i>0; i--) {
					Game.messageTyrant("You feel weak with hunger!");
					String stat=RPG.pick(hungerDecayStats);
					int sv=h.getBaseStat(stat);
					if (!h.getFlag("IsImmortal")) h.set(stat,RPG.max(sv-1,1));
				}
				break;
			case 3:
				// dying of hunger
				int loss=RPG.po(t/1000.0);
				if (loss>0) Game.messageTyrant("You are dying of hunger!!");
				if (!h.getFlag("IsImmortal")) h.incStat("HPSMAX",-loss);
				if (!h.getFlag("IsImmortal")) h.incStat("HPS",-loss*2);
				break;
		}

		// SPECIAL ABILITIES
		// thief searches
		for (int i = RPG.po(t * h.getStat(Skill.ALERTNESS)*h.getStat(RPG.ST_CR), 10000); i > 0; i--) {
			Secret.search();
		}

	}
	
	public static boolean hasHungerString(Thing h) {
		int threshold=h.getStat(RPG.ST_HUNGERTHRESHOLD);
		int hp=(threshold>0) ? (h.getStat(RPG.ST_HUNGER)*100)/threshold : 0;
	
		if ((hp<0)||(hp>=100)) return true;
		return false;
	}
	
	public static String hungerString(Thing h) {
		int threshold=h.getStat(RPG.ST_HUNGERTHRESHOLD);
		int hp=(threshold>0) ? (h.getStat(RPG.ST_HUNGER)*100)/threshold : 0;
		if (hp<-50) return "bloated";
		if (hp<0) return "satiated";
		if (hp<20) return "well fed";
		if (hp<70) return "contented";
		if (hp<100) return "peckish";
		if (hp<150) return "hungry";
		if (hp<200) return "very hungry";
		if (hp<250) return "ravenous";
		if (hp<300) return "starving";
		return "starving!";
	}
}