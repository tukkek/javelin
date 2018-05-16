

package tyrant.mikera.tyrant;

import java.util.HashSet;
import java.util.Set;

import javelin.controller.Movement;
import javelin.controller.old.Game;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;



/**
 * Item contains routines for managing all kinds of items in Tyrant
 * 
 * Items are generally recognised by the property "IsItem"==1
 */
public class Item {

	public static boolean isIdentified(Thing item) {
        HashSet identifiedItems = (HashSet) Game.hero().get("IdentifiedItems");
        if (identifiedItems == null)
            return false;
        return identifiedItems.contains(item.name());
    }
	
	public static boolean repair(Thing t, boolean perfect) {
		if (t.getStat(RPG.ST_HPS)<t.getStat(RPG.ST_HPSMAX)) {
			if (!perfect) {
				// some permanent damage if not perfect repair
				t.incStat(RPG.ST_HPSMAX,-1);
			}
			t.set(RPG.ST_HPS,t.getStat(RPG.ST_HPSMAX));
			return true;
		}
		
		return false;
	}
	
	public static boolean isDamaged(Thing t) {
		return t.getStat(RPG.ST_HPS)<t.getStat(RPG.ST_HPSMAX);
	}
	
	private static final String wpstring="watery potion";
    
	public static boolean isDisguisedName(Thing t) {
		String uname=t.getstring("UName");
		if (uname==null) return false;
		if (uname.equals(t.getstring("Name"))) return false;
		return true;
	}
	
	/**
	 * Mark a Thing as being identified
	 */
	public static void identify(Thing toIdentify) {
        Thing h=Game.hero();
        Set identifiedItems = (HashSet) h.get("IdentifiedItems");
        if (identifiedItems == null) {
            identifiedItems = new HashSet();
            h.set("IdentifiedItems", identifiedItems);
        }
        if (identifiedItems.add(toIdentify.name())) {
            Score.scoreIdentify(h, toIdentify);
        }
        
        // water potion special case
        if (wpstring.equals(toIdentify.getstring("UName"))) {
            //Game.warn("identified water: "+t.name());
            Lib.get(toIdentify.name()).set("IsStatusKnown",1);
        }
    }
	
	public static void fullIdentify(Thing t) {
		identify(t);
		t.set("IsStatusKnown",1);
	}

	public static boolean isOwned(Thing item) {
		return item.getFlag("IsOwned")||item.getFlag("IsShopOwned");
	}
	
	public static void clearOwnership(Thing item) {
		item.set("IsOwned",0);
		item.set("IsShopOwned",0);
	}
	
	public static void steal(Thing being, Thing item) {
		item.getMap().areaNotify(item.getMapX(), item.getMapY(), 10, AI.EVENT_THEFT, 0, being);
		if (being.isHero()) {
			Gods.checkTheft(being);
		}
	}
	
	public static boolean pickup(Thing person, Thing item) {
		item.remove();
		
		tryIdentify(person,item);

		int lift=Being.maxCarryingWeight(person)-person.getInventoryWeight();
		int stackWeight=item.getWeight();
		if ((stackWeight>0)&&(lift<stackWeight)) {
			if (lift>=item.getStat("ItemWeight")) {
				int num=lift/item.getStat("ItemWeight");
				person.message("You are only able to pick up "+num+" of "+item.getTheName());
				Thing leave=item.separate(item.getNumber()-num);
							
				person.getMap().addThing(leave, person.getMapX(), person.getMapY());
			} else {
			
				person.message("You can't handle any more kit!");
				
				// place item on map
				person.getMap().addThing(item, person.getMapX(), person.getMapY());
				return false;
			}
		}
		
		boolean stealing=false;
		if (isOwned(item)) {
			Item.clearOwnership(item);
			stealing=true;
		} 
		
		if (stealing) {
			// theft pickup
			person.message("You sneakily grab " + item.getTheName(null));
			person.addThingWithStacking(item);
			Item.steal(person,item);
		} else {
			person.message("You pick up " + item.getTheName(null));				
			person.addThingWithStacking(item);
		}
		
		// item touches hands or gloves if worn
		Thing g=person.getWielded(RPG.WT_HANDS);
		Item.touch((g == null) ? person : g, item);

		return true;
	}
	
	public static void drop(Thing h, Thing o) {
		Movement.moveTo(o, h.getMap(), h.x, h.y);
	}
	
	public static void touch(Thing person, Thing item) {
		if (person.handles("OnTouch")) {
			Event te=new Event("Touch");
			te.set("Target",item);
			person.handle(te);
		}
		
		if (item.handles("OnTouch")) {
			Event te=new Event("Touch");
			te.set("Target",person);
			item.handle(te);
		}
	}

	/** 
	 * Calculate a seed for a particular item type
	 */ 
	public static double identitySeed(Thing t) {
		return (t.getStat("Seed")%123456)/123456.0;
	}
	
	public static void tryIdentify(Thing h, Thing[] ts) {
		for (int i=0; i<ts.length; i++) {
			tryIdentify(h,ts[i]);
		}
	}

	public static boolean tryStatusDetect(Thing viewer, Thing item) {
		if (item.getFlag("IsStatusKnown")) return true;
		// perhaps recognise status?
		int holyskill=RPG.min(viewer.getStat(Skill.PRAYER),viewer.getStat(Skill.HOLYMAGIC)*2);
		if (holyskill>=2) {
			if (item.getStat("Level")<=(holyskill-1)*6) {
				item.set("IsStatusKnown",1);
				return true;
			}
		}
		return false;
	}
		
	// TODO: refactor this into a chance to identify using "Seed"
	// property
	public static boolean tryIdentify(Thing viewer, Thing item) {
		if (viewer!=Game.hero()) return false; 

		tryStatusDetect(viewer,item);

		if (isIdentified(item)) {
			return true;
		}
		
		int skill=viewer.getStat(Skill.IDENTIFY);
		int intel=viewer.getStat("IN");
		int ability=intel*skill;
		int level=item.getLevel();
		
		double seed=identitySeed(item);
		double id=(10000*ability)/1000000.0;
		
		// adjust for level
		id-=level*0.02;
		
		if (!Item.isDisguisedName(item)) id+=1000;
		
		if (id<=seed) {
			if (item.getFlag("IsRune")) {
				id+=viewer.getStat(Skill.RUNELORE)*0.1;
			} 
			if (item.getFlag("IsWeapon")||item.getFlag("IsArmour")) {
				id+=viewer.getStat(Skill.WEAPONLORE)*0.2;	
				id+=viewer.getStat(Skill.SMITHING)*0.1;
			} 
			if (item.getFlag("IsThrowingWeapon")) {
				id+=viewer.getStat(Skill.THROWING)*0.2;				
			} 
			if (item.getFlag("IsRangedWeapon")||item.getFlag("IsMissile")) {
				id+=viewer.getStat(Skill.ARCHERY)*0.2;				
			} 
			if (item.getFlag("IsPotion")) {
				id+=viewer.getStat(Skill.ALCHEMY)*0.2;				
			} 
			if (item.getFlag("IsHerb")) {
				id+=viewer.getStat(Skill.HERBLORE)*0.3;				
			} 
			if (item.getFlag("IsFood")) {
				id+=viewer.getStat(Skill.COOKING)*0.3;				
			} 
			if (item.getFlag("IsBook")||item.getFlag("IsScroll")) {
				id+=(viewer.getStat(Skill.LITERACY))*0.2;				
			}
		}
		
		if (id>seed) {
			identify(item);
			return true;
		} 

		return false;
	}

	/*
	 * public static String getAdjectives(Thing t) { Thing h=Game.hero; boolean
	 * isidentified=isIdentified(); int q=getQuality();
	 * 
	 * String n=super.getAdjectives();
	 * 
	 * if ((q>0)) { if (isidentified||(h.getStat(RPG.ST_APPRAISAL)>=3)) {
	 * n=Lib.qualitystrings[q]+" "+n; } else if (h.getStat(RPG.ST_APPRAISAL)>=1) {
	 * switch (q) { case 1: case 2: case 3: case 4: n="dodgy "+n; break; case 5:
	 * case 6: case 7: n="reasonable "+n; break; case 8: case 9: case 10: case
	 * 11: case 12: case 13: n="nice "+n; break; } } }
	 * 
	 * if ( getFlag("IsCursed") && (isidentified||(h.getStat(RPG.ST_PRIEST)>0)) ) {
	 * n="cursed "+n; }
	 * 
	 * if ( getFlag("IsBlessed") && (isidentified||(h.getStat(RPG.ST_PRIEST)>0)) ) {
	 * n="blessed "+n; }
	 * 
	 * if ( getFlag("IsDamaged")) { n="damaged "+n; }
	 * 
	 * if ( getFlag("IsCharmed") && (isidentified||(h.getStat(RPG.ST_MAGE)>0)) ) {
	 * n="charmed "+n; }
	 * 
	 * return n; }
	 */

	/*
	 * // modify effective quality public int getEffectiveQuality() { int
	 * q=getQuality(); if ((flags&ITEM_CURSED)>0) q=q-2; if
	 * ((flags&ITEM_BLESSED)>0) q=q+1; if ((flags&ITEM_MAGIC)>0) q=q+1; if
	 * ((flags&ITEM_CHARMED)>0) q=q+1;
	 * 
	 * if ((flags&ITEM_DAMAGED)>0) q=q-1; if ((flags&ITEM_RUSTY)>0) q=q-2; if
	 * ((flags&ITEM_BROKEN)>0) q=0; return RPG.middle(0,q,13); }
	 */

	public static String inspect(Thing t) {
		t=t.separate(1);
		Thing h=Game.hero();
		String s="";
		
		// basic description
		if (t.getFlag("IsArtifact")&&Item.isIdentified(t)) {
			s=s+"This is the legendary artifact known as \""+t.name()+"\"\n";
		} else {
			String des=t.getstring("Description");
			if (des==null) des="This looks like a normal "+t.getSingularName()+".";
			des=des+"\n";
			s=s+des;
		}
		
		// unidentified item info
		if ((!Item.isIdentified(t))) {
			String ids="You have not identified it yet.";
			if (Item.isDisguisedName(t)&&h.getFlag(Skill.PERCEPTION)) {
		
				ids=ids+" You feel that there may be more to it than meets the eye.\n";
		
			}
			s=s+ids+"\n";
		}
		
		if (h.getFlag(Skill.APPRAISAL)) {
			s=s+"You estimate that it may be worth up to "+Coin.valueString(Item.value(t))+"\n";
		}
		
		if (h.getFlag(Skill.CASTING)&&(t.getFlag("IsMagicItem"))) {
			s=s+"You sense the presence of magical energies.\n";
		}
		
		if (h.getFlag("Literacy")&&t.getFlag("IsBook")) {
			//TODO: know what book is about?
		}
		
		if (t.getFlag("IsWeapon")) {
			String handed="single handed";
			switch (t.getStat("WieldType")) {
				case RPG.WT_TWOHANDS: handed="two handed"; break;
			}
			String ws="It can be used as a "+handed+" weapon, with an attack skill of "+Weapon.getASK(t,h)+" and an attack strength of "+Weapon.getAST(t,h)+". ";
			ws=ws+" It contributes "+Weapon.getDSK(t,h)+" to your defence skill.";
			if (h.getFlag(Skill.WEAPONLORE)) {
				ws=ws+" It inflicts "+t.getstring("WeaponDamageType")+" damage. ";
				ws=ws+" It has a base attack cost of "+t.getStat("AttackCost")+". ";
				int extra=Weapon.getExtraAST(t,h);
				if (extra>0) ws=ws+"It inflicts an additional "+extra+" points of "+t.getstring("ExtraDamageType")+" damage. ";
			}
			s=s+ws+"\n";
		}
		
		// display runes
		if (h.getFlag(Skill.RUNELORE)) {
			Thing[] ts=t.getInventory();
			for (int i=0; i<t.invCount(); i++) {
				Thing r=ts[i];
				if (r.getFlag("IsRune")) {
					s=s+"It is inscribed with "+r.getAName()+".\n";
				}
			}
		} else {
			if (t.getFlag("IsRunic")) {
				s=s+"It is inscribed with mysterious runes.\n";
			}
		}
		
		s=s.replaceAll("\n","\n\n");
		t.restack();
		return s;
	}
	
	public static void use(Thing b, Thing t) {
		if (t.handles("OnUse")) {
			Event e = new Event("Use");
			e.set("User", b);
			t.handle(e);
		} else {
			b.message("You don't manage to do anything interesting");
		}
		b.incStat("APS", -Being.actionCost(b));

	}

	public static final double VALUEFACTOR=Math.sqrt(2);
	
	public static int value(Thing t) {
		return valueOfOne(t) * t.getStat("Number");
	}
	
	public static double levelValue(int level) {
		return Math.pow(VALUEFACTOR,level-1);
	}
	
    public static int valueOfOne(Thing t) {
        int v = t.getStat("Value");
        if (v == 0) {
            v = (int)(t.getStat("ValueBase") *
                      levelValue( t.getLevel() ));
        }
        
		Thing[] ts=t.getInventory();
		for (int i=0; i<t.invCount(); i++) {
			v+=Item.value(ts[i]);
		}
        
        return v;
    }

	public static Thing findShopkeeper(Thing t) {
		// identify shopkeeper
		BattleMap map=t.getMap();
		
		Thing sk=null;
		Thing sp=map.getFlaggedObject(t.x,t.y,"IsStockingPoint");
		if (sp!=null) {
			sk=sp.getThing("Shopkeeper");
		}
		int best=100;
		if (sk==null) {
			Thing[] ts=map.getObjects(t.x-8,t.y-8,t.x+8, t.y+8,"IsInhabitant");
			for (int i=0; i<ts.length; i++) {
				Thing s=ts[i];
				if (s.getFlag("IsShopkeeper")) return s;
				if (RPG.distSquared(s.x,s.y,t.x,t.y)<best) sk=s;
			}
		}
		return sk;
	}
	
	public static void bless(Thing target) {
		if (!target.getFlag("IsItem")) {
			Game.warn("Trying to bless a non-item!");
			return;
		}
		
		if (target.getFlag("IsArtifact")) {
			target.visibleMessage(target.getYourName()+" "+target.verb("seem")+" unaffected");
			return;
		}
		
		target.visibleMessage(target.getYourName()+" "+target.is()+" surrounded by a holy light");

		
		if (target.getFlag("IsCursed")) {
			target.set("IsCursed",0);  			
		} else {
			target.set("IsBlessed",1);
		}
		target.set("IsStatusKnown",1);		
		
		target.restack();
	}
	
	public static void curse(Thing target) {
		curse(target,true);
	}
	
	public static void curse(Thing target, boolean message) {
		if (!target.getFlag("IsItem")) {
			Game.warn("Trying to bless a non-item!");
			return;
		}	

		if (target.getFlag("IsArtifact")) {
			if (message) {
				target.visibleMessage(target.getYourName()+" "+target.verb("seem")+" unaffected");
			}
			return;
		}

		if (message) {
			target.visibleMessage(target.getYourName()+" "+target.is()+" surrounded by an unholy light");
		}
 		
		
		if (target.getFlag("IsBlessed")) {
			target.set("IsBlessed",0);  			
		} else {
			target.set("IsCursed",1);
		}
		target.set("IsStatusKnown",1);
	}
	
	public static int shopValue(Thing t, Thing h, Thing sk) {
		return shopValue(t,h,sk,t.getNumber());
	}
	
    public static int shopValue(Thing t, Thing h, Thing sk, int count) {
        int value = Item.valueOfOne(t);
        value=Coin.roundDownMoney(value);
        
        value = count*(int)(value*shopFactor(h,sk));

        return value;
    }

	private static double shopFactor(Thing h, Thing sk) {
		double hskill=h.getStat("CH")*h.getStat(Skill.TRADING);
		double sskill=sk.getStat("CH");
		return 0.4+0.6*(hskill/(hskill+sskill));
	}
	
	public static int shopPrice(Thing t, Thing h, Thing sk) {
		int value=Item.value(t);
		value=(int)(value/Math.sqrt(shopFactor(h,sk)));
		return Coin.roundMoney(value);
	}

	public static void init() {
        Thing t;
        
        t = Lib.extendCopy("base item", "base thing");
        t.set("IsItem", 1);
        t.set("IsStoreItem",1); // default is items are available
        t.set("IsOwned", 0);
        t.set("IsShopOwned",0);
        t.set("IsDestructible",1);
        t.set("IsPhysical",1);
        t.set("IsWishable",1);
        t.set("HPS",10);
        t.set("ImageSource", "Items");
        t.set("Image", 1);
        t.set("WeaponDamageType", RPG.DT_NORMAL);
        t.set("RES:poison",1000);
        t.set("RES:ice",13);
        t.set("RES:chill",1000);
        t.set("RES:drain",25);
        t.set("NameType", Description.NAMETYPE_NORMAL);
        t.set("Z", Thing.Z_ITEM);
        Lib.add(t);
        
        Food.init();
		Scroll.init();
		Missile.init();
		RangedWeapon.init();
		Potion.init();
		Wand.init();
		Ring.init();
		Coin.init();
		Armour.init();
		Weapon.init();
		Secret.init();
		SpellBook.init();
		Equipment.init();
		Amulet.init();
		Rune.init();
    Clothing.init();
    PortalStone.init();
	}
}