package tyrant.mikera.tyrant;

import java.util.*;

import javelin.model.BattleMap;

import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;



/**
 * Implements the Tyrant Skill System
 * 
 * Skills are measured by integer values>=0
 * 
 * The numbers can be thought of as a "years of training" in 
 * terms of their effectiveness. Of course, in Tyrant you can
 * obtain them much faster than that :-)
 * 
 * Skills are meant to be roughly balanced in value. e.g. 1 level
 * of ATTACK should be roughly equivalent in usefullness to 1 level
 * of SWIMMING. This tends to mean that you can do a lot more 
 * with one level of a rarely used skill!
 * 
 * Characters can gain about 1 Skill level per character level,
 * plus a few extra as special quest rewards. This puts a maximum
 * limit on the number of skill levels gained. Thus players must
 * choose wisely which skills they develop.
 * 
 * In general, Skills are represented as the properties of a Being,
 * and can be manipulated like any other property
 * 
 * Skills are maintained as this :
 * 1. Make sure that the skill is defined as a constant ( uppercase name ) pointing to a string
 * 2. Add the skill to the String[] names
 * 3. If the skill really is an ability, add it to the String[] abilities
 * 4. If the skill is not yet implemented, add it to the String[] todo
 * 
 * e.g. Game.hero.set(Skill.ARCHERY,2)
 * 
 * @author Mike
 */
public class Skill {
	private static final String[] names = {Skill.RIDING, Skill.ARCHERY,
			Skill.ATHLETICS, Skill.THROWING, Skill.TRACKING,
			Skill.CLIMBING, Skill.DODGE, Skill.SWIMMING, Skill.SURVIVAL,
			Skill.ATTACK, Skill.DEFENCE, Skill.PARRY, Skill.FEROCITY,
			Skill.UNARMED, Skill.MIGHTYBLOW, Skill.BRAVERY,
			Skill.WEAPONLORE, Skill.ALERTNESS, Skill.PICKPOCKET,
			Skill.PICKLOCK, Skill.DISARM, Skill.PRAYER, Skill.HOLYMAGIC,
			Skill.MEDITATION, Skill.HEALING, Skill.LITERACY,
			Skill.IDENTIFY, Skill.ALCHEMY, Skill.LANGUAGES, Skill.RUNELORE,
			Skill.HERBLORE, Skill.BLACKMAGIC, Skill.TRUEMAGIC, 
			Skill.MAGICRESISTANCE,
			Skill.CASTING, Skill.MUSIC, Skill.PERCEPTION, Skill.SLEIGHT,
			Skill.STORYTELLING, Skill.SEDUCTION, Skill.PAINTING,
			Skill.SMITHING, Skill.STEALTH, Skill.MINING, Skill.APPRAISAL, Skill.WOODWORK,
			Skill.ROPEWORK, Skill.CONSTRUCTION, Skill.TRADING,
			Skill.FARMING, Skill.COOKING, Skill.FOCUS
			};
	
	 private static final String[] abilities = {
	     Skill.BRAVERY, Skill.BLACKMAGIC , Skill.TRUEMAGIC, Skill.PERCEPTION, Skill.APPRAISAL,  
	 };
	 
	 private static final String[] todo = {
		    Skill.RIDING, Skill.MIGHTYBLOW, Skill.PARRY, Skill.MEDITATION, Skill.MUSIC, 
		    Skill.STORYTELLING, Skill.PAINTING, Skill.SEDUCTION, Skill.STEALTH, Skill.WOODWORK, 
		    Skill.ROPEWORK, Skill.CONSTRUCTION,  
	 };
	
	// ranger skills 21xx
	public static final String RIDING = "Riding";
	public static final String ARCHERY = "Archery";
	public static final String ATHLETICS = "Athletics";
	public static final String THROWING = "Throwing";
	public static final String TRACKING = "Tracking";
	public static final String CLIMBING = "Climbing";
	public static final String DODGE = "Dodge";
	public static final String SWIMMING = "Swimming";
	public static final String SURVIVAL = "Survival";
	public static final String DEFENCE = "Defence";
	
	// fighter skills 22xx
	public static final String ATTACK = "Attack";
	public static final String PARRY = "Parry";
	public static final String FEROCITY = "Ferocity";
	public static final String UNARMED = "Unarmed Combat";
	public static final String MIGHTYBLOW = "Mighty Blows";
	public static final String BRAVERY = "Bravery";
	public static final String WEAPONLORE = "Weapon Lore";
	public static final String TACTICS = "Tactics";
	
	// thief skills 23xx
	public static final String ALERTNESS = "Alertness";
	public static final String PICKPOCKET = "Pickpocket";
	public static final String PICKLOCK = "Lockpicking";
	public static final String DISARM = "Disarm Traps";
	public static final String STEALTH = "Stealth";
	
	// priest skills 24xx
	public static final String PRAYER = "Prayer";
	public static final String HOLYMAGIC = "Holy Magic";
	public static final String MEDITATION = "Meditation";
	public static final String HEALING = "Healing";
	
	// Scholar skills 25xx
	public static final String LITERACY = "Literacy";
	public static final String IDENTIFY = "Item Lore";
	public static final String ALCHEMY = "Alchemy";
	public static final String LANGUAGES = "Languages";
	public static final String RUNELORE = "Rune Lore";
	public static final String HERBLORE = "Herb Lore";
	public static final String STRATEGY = "Strategy";
	
	// mage skills 26xx
	public static final String BLACKMAGIC = "Black Magic";
	public static final String TRUEMAGIC = "True Magic";
	public static final String MAGICRESISTANCE = "Magic Resistance";
	public static final String CASTING = "Spellcasting";
	public static final String FOCUS="Focus";
	
	// bard skills 27xx
	public static final String MUSIC = "Music";
	public static final String CON = "Deception";
	public static final String PERCEPTION = "Perception";
	public static final String SLEIGHT = "Sleight Of Hand";
	public static final String STORYTELLING = "Storytelling";
	public static final String SEDUCTION = "Seduction";
	
	// artisan skills 28xx
	public static final String PAINTING = "Painting";
	public static final String SMITHING = "Smithing";
	public static final String APPRAISAL = "Appraisal";
	public static final String WOODWORK = "Woodwork";
	public static final String ROPEWORK = "Ropework";
	public static final String CONSTRUCTION = "Construction";
	public static final String TRADING = "Trading";
	public static final String FARMING = "Farming";
	public static final String COOKING = "Cooking";
	public static final String MINING = "Mining";
	

	/**
	 * Returns true if the given string is a valid skill
	 * 
	 * @param s The string containing the possible skill name
	 * @return True if s is a valid skill
	 */
	public static boolean isSkill(String s) {
		for (int i=0; i<names.length; i++) {
			if (names[i].equals(s)) return true;
		}
		return false;
	}
	
	public static boolean isAbility(String s) {
		for (int i=0; i<abilities.length; i++) {
			if (abilities[i].equals(s)) return true;
		}
		return false;
	}
	
	public static boolean isNotUsed(String s) {
		for (int i=0; i<todo.length; i++) {
			if (todo[i].equals(s)) return true;
		}
		return false;
	}
	
	public static boolean isSkillIgnoreCase(String s) {
		for (int i=0; i<names.length; i++) {
			if (names[i].equalsIgnoreCase(s)) return true;
		}
		return false;
	}
	
	public static String ensureCase(String s) {
		for (int i=0; i<names.length; i++) {
			if (names[i].equalsIgnoreCase(s)) return names[i];
		}
		return s;
	}

	/**
	 * Returns an ArrayList list of strings descibing a being's
	 * skills.
	 * 
	 * The format uses the skill name plus roman numerals for
	 * the skill level, e.g. "Archery III"
	 * 
	 * @param b Any being
	 * @return ArrayList containing 0 or more strings
	 */
	public static ArrayList getList(Thing b) {
		ArrayList al = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			String s = names[i];
			int level = b.getStat(s);
			if (level > 0) {
				if (level > 1)
					s = s + " "+Text.roman(level);
				al.add(s);
			}
		}
		return al;
	}

	public static String[] fullList() {
		return names;
	}
	
	/**
	 * Returns an ArrayList list of strings descibing a being's
	 * skills.
	 * 
	 * The format uses the skill name only
	 * 
	 * @param b Any being
	 * @return ArrayList containing 0 or more strings
	 */
	public static ArrayList getUnmarkedList(Thing b) {
		ArrayList al = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			String s = names[i];
			int level = b.getStat(s);
			if (level > 0) {
				al.add(s);
			}
		}
		return al;
	}


	// gets list of skills trainable 
	
	public static String[] getTrainableSkills( Thing b , boolean numerals ) {

		int skillcount = names.length;
		int level;
		Vector v = new Vector();
		String[] tmp =new String[1];
		
		  for (int i=0; i <skillcount; i++) {
		    String s = names[i];
		    if( !isAbility( s ) && !isNotUsed( s ) && ( level = b.getStat(s)) > 0){
      	  if ( level > 1 && numerals ){
					  s = s + " "+Text.roman(level);
					}
      	  v.add(s);
		    }
		  }
		return (String[])v.toArray(tmp);
	}

	public static String trim(String s) {
		s=s.trim();
		while ((!isSkill(s))&&(s.length()>0)) {
			s=s.substring(0,s.length()-1);
		}
		return s;
	}
	
	public static void add(Thing t, String s, int l) {
		s=Skill.trim(s);
		if (isSkill(s)) {
			t.incStat(s,l);
		} else {
			throw new Error("Not a skill: "+s);
		}
	}
	
	public static boolean train(Thing t, String s) {
		if (t.getStat(RPG.ST_SKILLPOINTS)<=0) {
			t.message("You don't seem able to learn any more");
			return false;
		}
		
		t.message("You train your "+s+" skill");
		Skill.add(t,s,1);
		t.incStat("APS",-1000);
		t.incStat(RPG.ST_SKILLPOINTS,-1);
		t.incStat(RPG.ST_SKILLPOINTSSPENT,1);	
		return true;
	}
	
	public static boolean apply(Thing b, String s) {
		s=Skill.trim(s);
		
		// Game.warn("applying skill: "+s);
		if (s.equals(Skill.PICKPOCKET)) {	
			return applyPickPocket(b);
		}
		
		if (s.equals(Skill.DISARM)) {
			return Trap.applyDisarmTraps(b);
		}
		
		if (Recipe.isRecipeSkill(s)) {
			return Recipe.apply(b,s);
		}
		
		b.message("The "+s+" skill is used automatically");
		if (descs.containsKey(s)) {
			b.message((String)descs.get(s));
		}
		
		return false;
	}
			
			
	public static boolean applyPickPocket(Thing h) {
			Game.messageTyrant("Pickpocket: select direction");
			Point p = Game.getDirection();
			BattleMap map = h.getMap();
			int level=h.getStat(Skill.PICKPOCKET);
			
			if (p != null) {
				Thing b = map
						.getFlaggedObject(h.x + p.x, h.y + p.y, "IsMobile");

				if (b==null) {
					Game.messageTyrant("");
					return false;
				}
				
				int skill = h.getStat(RPG.ST_SK) * level;

				if (RPG.test(skill, b.getStat(RPG.ST_SK),h,b)) {
					Thing[] stuff = b.getItems();
					if (stuff.length>0) {
						Thing nick = stuff[RPG.r(stuff.length)];
						if ((nick.y == 0)&&!nick.getFlag("IsTheftProof")) {
							h.addThingWithStacking(nick);
							Game.messageTyrant("You steal " + nick.getAName());
							Item.steal(h,nick);
						} else {
							Game.messageTyrant("You almost manage to steal "
									+ nick.getAName());
						}
					} else {
						Game.messageTyrant("You find nothing worth stealing");
					}
				} else if (RPG.test(skill, b.getStat(RPG.ST_IN),h,b)) {
					Game.messageTyrant("You are unable to steal anything");
				} else {
					Game.messageTyrant(b.getTheName() + " spots you!");
					AI.turnNasty(b);
				}
				return true;
			}
			return false;
	}
	
	private static final HashMap descs=new HashMap();
	
	public static void init() {
		descs.put(Skill.RIDING,"This skill allows you to ride horses and other riding animals");
		descs.put(Skill.ATTACK,"This skill improves your chance of hitting opponents and increases the amount of damage that you inflict in close combat");
		descs.put(Skill.DEFENCE,"This skill reduces your chance of being hit and damaged in close combat, particularly when using shields and armour");
		descs.put(Skill.UNARMED,"This skill improves your ability to fight in close combat without a weapon");
		descs.put(Skill.DODGE,"This skill improves your ability to dodge missile and avoid enemy attacks in hand to hand combat");
		descs.put(Skill.CASTING,"This skill enables you to cast magical spells with increased effectiveness");
		descs.put(Skill.SURVIVAL,"This skill enables you survive longer in the wild by finding food in unexpected places");
		descs.put(Skill.BRAVERY,"This skill enables you to resist the effects of fear and fight effectively when outnumbered");
		descs.put(Skill.IDENTIFY,"This skill enables you to identify items that you discover");
		descs.put(Skill.ALERTNESS,"This skill helps you to notice hidden items, traps and secret doors");
		descs.put(Skill.ARCHERY,"This skill improves your ability at firing ranged weapons such as bows");
		descs.put(Skill.THROWING,"This skill improves your ability with throwing weapons such as knives or darts");
		descs.put(Skill.TRACKING,"This skill improves your chances of hunting down prey in the wilderness");
		descs.put(Skill.FOCUS,"This skill improves your ability to recharge your mental energies");
	}
}