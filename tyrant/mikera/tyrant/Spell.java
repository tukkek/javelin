//
// Implementation of all Spells and Special effects in Tyrant
//
//

package tyrant.mikera.tyrant;
import java.util.*;

import javelin.controller.Movement;
import javelin.model.BattleMap;

import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.util.Text;



/** 
 * Static class to manage magic spells
 * 
 * @author Mike
 *
 */
public class Spell {

	// These indicate the target type of the specified spell
	public static final int TARGET_NONE = 0;
	public static final int TARGET_SELF = 1;
	public static final int TARGET_LOCATION = 2;
	public static final int TARGET_DIRECTION = 3;
	public static final int TARGET_ITEM = 4;

	/* these tags notify the AI of the type of spell
	 * AI won't try to use any SPELL_USELESS
	 * Should try SPELL_DEFENCE if wounded
	 * Will shoot random SPELL_OFFENCE at enemy
	 * Won't generally use any SPELL_ENCHANT
	 */ 
	public static final int SPELL_USELESS = 0;
	public static final int SPELL_OFFENCE = 1;
	public static final int SPELL_DEFENCE = 2;
	public static final int SPELL_TACTICAL = 3;
	public static final int SPELL_ENCHANT = 4;
	public static final int SPELL_SUMMON = 5;

	/*
	 * List of all spell names
	 */
	private static ArrayList spellNames;
	
	private static final String[] orders={
			Skill.TRUEMAGIC, 
			Skill.HOLYMAGIC, 
			 
			Skill.BLACKMAGIC 
			 
			};
	
	
	public static boolean isOrder(String s) {
		for (int i=0; i<orders.length; i++) {
			if (orders[i].equals(s)) return true;
		}
		return false;
	}
	
	public static void learn(Thing h, String spell) {
		Thing s=Lib.create(spell);
		if (s==null) throw new Error("Spell doesn't exist: "+spell);
		
		// dublicate check
		Thing es=h.getContents(spell);
		if (es!=null) {
			h.message("You already know the "+s.getName(Game.hero())+" spell");
			return;
		}
		
		Game.messageTyrant("You mind is filled with mystic knowledge");
		h.addThing(s);
		Game.messageTyrant("You now know the "+s.getName(Game.hero())+" spell");	
	}
	
	public static Thing randomSpell(int level) {
		return Lib.createType("IsSpell",level);
	}
	
	public static Thing randomSpell(String order,int level) {
		for (int i=0; i<100; i++) {
			Thing s=randomSpell(level);
			if (order.equals(s.getString("Order"))) {
				return s;
			}
		}
		Game.warn("Unable to create level "+level+" spell for order ["+order+"]");
		return null;
	}

	public static Thing randomOffensiveSpell(String order,int level) {
		for (int i=0; i<10; i++) {
			Thing s=Spell.randomSpell(order,level);
			if (s.getStat("SpellUsage")==Spell.SPELL_OFFENCE) {
				return s;
			}
		}
		Game.warn("Unable to create level "+level+" offensive spell for order ["+order+"]");
		return Lib.create("Magic Missile");
	}

	public static int castTime(Thing caster) {
		// TODO - figure this out
		double time=200;
		time=time/(1.0+0.25*caster.getStat(Skill.CASTING));
		return (int)time;
	}

	// make caster cast spell automatically
	// make most effective use of spell
	// returns true if spell cast
	public static boolean castAI(Thing caster, Thing s) {
		// bail out if we have insufficient energy to cast
		if (!canCast(caster,s)) {
			return false;
		}
			

		BattleMap map = caster.getMap();

		if (s.getStat("SpellUsage") == SPELL_OFFENCE) {

			if (map == null)
				return false;

			// find enemy to shoot at
			Thing p = map.findNearestFoe(caster);

			if ((p!=null)) {
				// check if friend is too close!!
				// if not, then cast at target as planned
				Thing f = map.findNearestFoe( p);
				if ((f == null) || (RPG.distSquared(f.x, f.y, p.x, p.y) > s.getStat("Radius"))) {
					if (p.isVisible(Game.hero())||caster.isVisible(Game.hero())) {
						Game.messageTyrant(caster.getTheName()+ " casts "+s.name()+" at "+p.getTheName());
					}
							
					castAtLocation(s,caster, map, p.x, p.y);
					Spell.castCost(caster,s);
					return true;
				}
			}
			return false;

		} else if ((s.getStat("SpellUsage") == SPELL_DEFENCE)
				&& (caster.getStat(RPG.ST_HPS) < caster.getStat(RPG.ST_HPSMAX))) {

			// we're wounded, so cast a defensive spell
			caster.visibleMessage(caster.getTheName()
					+ " casts "+s.name());
			Spell.castAtSelf(s,caster);
			Spell.castCost(caster,s);
			return true;

		} else if ((s.getStat("SpellUsage") == SPELL_SUMMON) && caster.isVisible(Game.hero())) {

			// cast summon spell between caster and nearest foe
			Thing f = map.findNearestFoe(caster);
			if (f != null) {

				int tx = (caster.x + f.x) / 2;
				int ty = (caster.y + f.y) / 2;

				if (!map.isBlocked(tx, ty)) {
					Game.messageTyrant(caster.getTheName()
							+ " shouts words of summoning!");
					Spell.castAtLocation(s,caster, map, tx, ty);
					Spell.castCost(caster,s);
					return true;
				}

			}
		}
		return false;
	}

	// train the caster in the given spell
	public static void train(Thing caster, Thing s) {
		int cl = caster.getStat(RPG.ST_LEVEL);
		int level=s.getStat("SpellSkill");
		int cost=s.getStat("SpellCost");
		if (RPG.d(cl) <= level)
			return;

		int learning = caster.getStat(RPG.ST_IN) * caster.getStat(RPG.ST_IN);
		int difficulty = level * level * cost;

		if (RPG.test(learning, difficulty)) {
			s.incStat("SpellSkill", 1);
		}
	}

	public static void castAtSelf(Thing s, Thing caster) {
		if (caster == null)
			return;
		if (s.getStat("SpellTarget") == TARGET_LOCATION) {
			Spell.castAtLocation(s, caster, caster.getMap(), caster.x, caster.y);
			return;
		}

		Game.instance().doSpark(caster.x, caster.y, s.getStat("BoltImage"));
		doEffect(caster,s,caster);
	}

	public static void castAtLocation(Thing s, Thing caster, BattleMap map, int tx, int ty) {
		if ((map==null)||(s.getStat("SpellTarget") != TARGET_LOCATION)) {
			return;
		}

		int radius=s.getStat("Radius");
		int bolt=s.getStat("BoltImage");

		// special effects for location targetted spells
		if (map.isVisible(tx, ty)) {
			if (caster != null) {
				Game.instance().doSpellShot(caster.x, caster.y, tx, ty, s.getStat("BoltImage"),
						100,radius);
			} else {
				Game.instance().doExplosion(tx, ty, bolt, radius);
			}
		}

		// en route effects
		if (caster != null) {
			double sx = caster.x + 0.5;
			double sy = caster.y + 0.5;
			int px = caster.x;
			int py = caster.y;
			double d = Math.sqrt((px - tx) * (px - tx) + (py - ty) * (py - ty));
			while ((px != tx) || (py != ty)) {
				while ((px == (int) sx) && (py == (int) sy)) {
					sx += 0.5 * (tx - caster.x) / d;
					sy += 0.5 * (ty - caster.y) / d;
				}
				px = (int) sx;
				py = (int) sy;
				
				if (s.handles("OnPathEffect")) {
					Event e=new Event("PathEffect");
					e.set("TargetMap",map);
					e.set("TargetX",px);
					e.set("TargetY",py);
					if (s.handle(e)) return;			
				}
			}
		}

		// ball spell damage effects
		// rr contains squared radius
		if (radius > 0) {
			Spell.affectArea(s, caster, map, tx, ty, radius);
			return;
		}

		Spell.affectLocation(s, caster, map, tx, ty);
	}

	public static void castAtObject(Thing s, Thing caster, Thing target) {
		if (s.getStat("SpellTarget")==Spell.TARGET_LOCATION) {
			castAtLocation(s,caster,target.getMap(),target.getMapX(),target.getMapY());
			return;
		}
		doEffect(caster,s,target);
	}

	public static void castInDirection(Thing s, Thing caster, int dx, int dy) {
		int tx=caster.x+dx;
		int ty=caster.y+dy;
		int bolt=s.getStat("BoltImage");
		Game.instance().doSpark(tx, ty, bolt);
		affectLocation(s,caster,caster.getMap(),tx,ty);
	}

	/**
	 * 
	 * apply spell effect over map area with given radius
	 * area affected is a square
	 * 
	 */
	public static void affectArea(Thing s, Thing caster, BattleMap map, int tx, int ty, int radius) {
		int d = radius;
		for (int dx = -d; dx <= d; dx++) {
			for (int dy = -d; dy <= d; dy++) {
				if ((d*d+1)<(dx*dx+dy*dy)) continue;
				affectLocation(s, caster, map, tx + dx, ty + dy);
			}
		}
	}

	public static String getOrder(String spellName) {
		return (String)Lib.get(spellName).get("Order");
	}
	
	/**
	 * return true if being is able to learn this spell
	 */ 
	public static boolean canLearn(Thing b,String s) {
		String order=getOrder(s);
		return b.getStat(order)>0;
		//return RPG.test(b.getStat(RPG.ST_LEVEL), levels[type]);
	}
	
	/** 
	 * Determines whether a being can cast a given spell
	 * 
	 * Being must have learnt the spell, i.e. the spell must
	 * be in the being's inventory
	 * 
	 * @param b
	 * @param s
	 * @return
	 */
	public static boolean canCast(Thing b,Thing s) {
		if (s.place!=b) {
			Game.warn("Spell.canCast: spell not in inventory!");
			return false;
		}
		
		String order=s.getString("Order");
		if (order.equals(Skill.BLACKMAGIC)) {
			int cost=s.getStat("SpellCost");
			if (b.getStat(RPG.ST_MPS)<cost) return false;
			int us=Recipe.checkIngredients(b,s.getString("Ingredients"));	
			return (us>0);
		} 
		
		int c=s.getStat("Charges");
		return (c>0);

	}
	
	/**
	 * Implements the cost of casting a spell
	 * Either MPS or charges consumed depending on order
	 * 
	 * Assumes that canCast() has been called and returned true
	 * 
	 * @param b The being casting the spell
	 * @param s The spell to cast
	 */
	public static void castCost(Thing b, Thing s) {
		b.incStat("APS",-castTime(b));
		
		String order=s.getString("Order");
		if (order.equals(Skill.TRUEMAGIC)||order.equals(Skill.HOLYMAGIC)) {
			s.incStat("Charges",-1);
		} else {
			b.incStat("MPS",-s.getStat("SpellCost"));
		}
		
		// remove ingredients
		String ings=s.getString("Ingredients");
		if (ings!=null) {
			if (!Recipe.removeIngredients(b,ings)) {
				Game.warn("Bug: Not enough ingredients to cast!");
			}
		}
	}
	
	public static int maxCharges(Thing b, Thing s) {
		int cost=s.getStat("SpellCost");
		if (cost==0) return 0;
		int power=b.getStat("WP");
		return (1+(power-1)/cost);
	}
	
	public static String chargeString(Thing b, Thing s) {
		String order=s.getString("Order");
		
		if (order.equals(Skill.BLACKMAGIC)) {
			String ings=s.getString("Ingredients");
			int ch=Recipe.checkIngredients(b,ings);
			int cost=s.getStat("SpellCost");
			String cs=(cost>0)?("+ cost: "+cost):"";
			if (ch>0) return "[ units: "+ch+" "+cs+"]";
			return "[ needs: "+ings+" "+cs+"]";
		} 
		
		return "[ charges: "+s.getStat("Charges")+" / "+maxCharges(b,s)+" ]";
	}
	
	public static String powerString(Thing b, Thing s) {
		return "{ skill: "+Spell.calcMagicSkill(b,s)+" }";
	}
	
	private static String rechargeSkill(Thing s) {
		String order=s.getString("Order");
		if (order.equals(Skill.HOLYMAGIC)) {
			return Skill.PRAYER;
		}
        return Skill.FOCUS;
	}
	
	private static int chargeRate(Thing b, Thing s) {
		// base rate equal to WP
		int rate=b.getStat("WP");
		
		// bonus for appropraite recharge skill
		rate=rate*(1+b.getStat(rechargeSkill(s)));
		
		// spell specific charge rate
		// default is 100
		rate=(rate*s.getStat("ChargeRate"));
		
		// more costly spells take longer to recharge
		rate=rate/s.getStat("SpellCost");
		
		if (rate<=0) {
			Game.warn("Spell.chargeRate(...)==0 for "+s.name());
		}
		
		return rate;
	}
	
	/**
	 * Recharge all spells for a given being
	 * 
	 * @param b The being with spells
	 * @param time Time period over which to apply recharge
	 */
	public static void rechargeSpells(Thing b, int time) {
		Thing[] ss=b.getFlaggedContents("IsSpell");
		int n=ss.length;
		for (int i=0; i<n; i++) {
			Thing s=ss[i];
			
			int max=maxCharges(b,s);
			int c=s.getStat("Charges");
			int gap=max-c;
			
			if ((max>0)&&(gap>0)) {
				// rechargerate divided across all spells
				int rate=chargeRate(b,s)/n;
				
				// increase charge if test passed
				if (RPG.test(time*rate,1000000)) {
					s.incStat("Charges",1);
					if (s.getBaseStat("Charges")==1) {
						b.message("You have regained enough power to cast "+s.name()+" again");
					}
				}
			}
		}
	}

	// apply spell affect to individual square
	public static void affectLocation(Thing s, Thing caster, BattleMap map, int tx, int ty) {
		if (s.handles("OnLocationEffect")) {
			//Game.warn("loc effect!");
			Event e=new Event("LocationEffect");
			e.set("TargetMap",map);
			e.set("TargetX",tx);
			e.set("TargetY",ty);
			if (s.handle(e)) return;
		}
		
		Thing[] things=map.getThings(tx,ty);
		for (int i=0; i<things.length; i++) {
			doEffect(caster,s,things[i]);
		}
	}

	public static int getPower(Thing s) {
		Game.assertTrue(s.getFlag("IsSpell"));
		
		return s.getStat("Power");
	}
	
	public static String selectionString(Thing b, Thing s,int l) {
		return Text.centrePad(s.getString("Name"),Spell.powerString(b,s)+" "+Spell.chargeString(b,s),l);
	}
	
	public static Thing create(String spellName) {
		Thing s=Lib.create(spellName);
		
		if ((s==null)||!s.getFlag("IsSpell")) {
			throw new Error("["+spellName+"] is not a valid spell");
		}
		
		return s;
	}

	
	private static int calcAntiMagic(Thing t) {
		double am=t.getStat("AntiMagic");
		am=am+t.getStat("WP")*(1.0+0.7*t.getStat(Skill.MAGICRESISTANCE));
		return (int)am;
	}
	
	/**
	 * Gets the skill of the caster with a given spell
	 * 
	 * This is broadly independant of the spell and is
	 * based mainly on IN
	 * 
	 */ 
	private static int calcMagicSkill(Thing caster, Thing spell) {
		// TODO skill modification
		if (caster==null) {
			return (int)(5*Math.pow(spellPowerMultiplier,spell.getLevel()));
		}
		int skill=caster.getStat(spell.getString("Order"));
		int st=(int)(caster.getStat("IN")
				*(0.85+0.15*caster.getStat(Skill.CASTING))
				*(0.85+0.15*skill)); 
		return st;
	}
	
	/**
	 * Gets the power of the spell.
	 * 
	 * Differenr spells have different powers driven by the
	 * following spell properties:
	 * 	PowerMultiplier
	 * 	PowerBonus
	 * 
	 * These modifiers are applied to the caster's base skill
	 * to determine the power of the spell
	 * 
	 * @param caster
	 * @param spell
	 * @return
	 */
	private static int calcMagicPower(Thing caster, Thing spell) {
		Game.assertTrue(spell.getFlag("IsSpell"));
		double sk=0;
		if (caster!=null) {
			sk=calcMagicSkill(caster,spell);
		} 
		
		// minimum skill
		int skillMin=spell.getStat("SkillMin");
		if (sk<skillMin) sk=skillMin;
		
		sk=sk*spell.getStat("PowerMultiplier")/100.0 
				+spell.getStat("PowerBonus");
		
		if (sk<0) sk=0;
		return (int)sk;
	}
	
	private static class CreateEffect extends Script {
		private static final long serialVersionUID = 4051047475630388276L;

        public CreateEffect(String s, int chance, boolean needspace) {
			this(s,chance);
			set("NeedSpace",needspace);
		}
		
		public CreateEffect(String s, int chance) {
			this(s);
			set("Chance",chance);
		}
		
		public CreateEffect(String s) {
			set("Chance",100);
			set("NeedSpace",1);
			set("SummonType",s);
		}
		
		public boolean handle(Thing spell, Event e) {
			BattleMap m=(BattleMap)e.get("TargetMap");
			if (m==null) return false;
			int x=e.getStat("TargetX");
			int y=e.getStat("TargetY");
			int chance=getStat("Chance");
			boolean needspace=getFlag("NeedSpace");
			
			// Game.warn("summon attempt at ("+x+","+y+")");
			
			if ((RPG.r(100)<chance)&&(!needspace||!m.isBlocked(x,y))) {
				String s=getString("SummonType");
				Thing t=Lib.create(s);
				m.addThing(t,x,y);
				//Game.warn("summon "+t.getName()+" success at ("+x+","+y+")");
			}
			return false;
		}
	}
	
	private static class TeleportEffect extends Script {
		private static final long serialVersionUID = 4049358595655349555L;

        public boolean handle(Thing spell, Event e) {
			Thing target=e.getThing("Target");
			BattleMap map=target.getMap();
			if (map==null) return false;
			
			Point p=map.findFreeSquare();
			
			if (p==null) return false;
				
			if (target.isHero()) {
				target.message("You teleport");
			} else {
				target.visibleMessage(target.getTheName()+" teleports");
			}
			Movement.teleport(target,map,p.x,p.y);
			
			return false;
		}		
	}
	
	private static class SummonEffect extends Script {
        private static final long serialVersionUID = 1L;

        public boolean handle(Thing spell, Event e) {
			int min = getStat("SummonMin");
			int max = getStat("SummonMax");
			int n=RPG.rspread(min,max);
			String type=getString("SummonType");
			
        	Thing summoner=e.getThing("Target");
        	BattleMap map=summoner.getMap();
			int x=summoner.getMapX();
			int y=summoner.getMapY();
			for (int i=0; i<n; i++) {
				Point p=map.findFreeSquare(x-1,y-1,x+1,y+1);
				if ((p==null)||!map.isVisible(p.x,p.y)) {
					p=map.findFreeSquare(x-2,y-2,x+2,y+2);
				}
				if ((p==null)||!map.isVisible(p.x,p.y)) {
					p=map.findFreeSquare(x-3,y-3,x+3,y+3);
				}
				if (p==null) continue;
				
				Thing t=Lib.create(type,summoner.getLevel()-2);
			
				if (t.getFlag("IsBeing")) {
					AI.setFollower(t,summoner);
				}
				Game.instance().doSpark(p.x,p.y,spell.getStat("BoltImage"));
				map.addThing(t,p.x,p.y);
				t.set("APS",-200);
			}
			
			return false;
		}		
	}
	
	private static Script createSummonEffect(String type, int min, int max) {
		Script s=new SummonEffect();
		s.set("SummonType",type);
		s.set("SummonMin",min);
		s.set("SummonMax",max);
		return s;
	}
	
	private static class OffensiveEffect extends Script {
		private static final long serialVersionUID = -1441648028619126968L;

        public boolean handle(Thing spell, Event e) {
			Thing target=e.getThing("Target");
			int eff=e.getStat("Strength");
			if (eff>0) {
				int dam=eff;
				// Game.warn("dam="+dam);
				boolean visible=target.isVisible(Game.hero());
				
				int damResult=Damage.inflict(target,dam,spell.getString("SpellDamageType"));
				if ((damResult>0)&&target.isDead()) {
					String verbed=(target.getFlag("IsLiving"))?"killed":"destroyed";
					if (visible) Game.messageTyrant(target.getTheName()+" is "+verbed+" by the "+spell.getString("HitName"));
				}
			} else {
				// spell has zero effect
			}
			
			return false;
		}
	}
	
    public static class HealingScript extends Script {
        private static final long serialVersionUID = 3257009860535989298L;

        public boolean handle(Thing t, Event e) {
			Thing target=e.getThing("Target");
			int eff=e.getStat("Strength");

            int heal=eff;
            Being.heal(target,heal);

            return false;
        }
    }
    
    public static class CureScript extends Script {
        private static final long serialVersionUID = 3978138846573377330L;

        public boolean handle(Thing t, Event e) {
			Thing target=e.getThing("Target");
			int eff=e.getStat("Strength");

            int heal=eff;
            Poison.cure(target,heal);

            return false;
        }
    }
    
	private static class AddScript extends Script {
		private static final long serialVersionUID = 3762529005907293232L;

        public boolean handle(Thing t, Event e) {
			Thing tt=e.getThing("Target");
			int chance=getStat("Chance");
			String thing=getString("Thing");
			if (chance==0) chance=100;
			if (RPG.r(100)<chance) {
				tt.addThing(Lib.create(thing));
			}
			return false;
		}
		
		public static Script create(String th, int c) {
			AddScript as=new AddScript();
			as.set("Thing",th);
			as.set("Chance",c);
			return as;
		}
	} 
	
	/**
	 * Causes the effect of a spell on a single target
	 * doEffect does this by calling the Spell's "OnEffect" script
	 * 
	 * @param caster The caster of the spell, possibly null
	 * @param spell The spell cast
	 * @param target The target to be affected
	 */
	private static void doEffect(Thing caster, Thing spell, Thing target) {
		int magicSkill=Spell.calcMagicSkill(caster,spell);
		int magicDefence=Spell.calcAntiMagic(target);
		int magicPower=Spell.calcMagicPower(caster,spell);
		
		// work out whether spell is effective
		boolean effective=true;
		boolean visible=target.isVisible(Game.hero());
		if ((magicDefence>0)&&spell.getStat("SpellUsage")==Spell.SPELL_OFFENCE) {
			magicSkill+=magicPower/5;
			Game.warn("Magic test: "+magicSkill+" vs. "+magicDefence);
			
			effective=magicSkill>=(magicDefence*RPG.luckRandom(caster,target));
		}
		
		if ((caster!=null)&&target.getFlag("IsBeing")&&(spell.getStat("SpellUsage")==Spell.SPELL_OFFENCE)) {
			AI.notifyAttack(target,caster);
		}
		
		String hitname=spell.getString("HitName");
		if (effective){
			if (hitname!=null) target.message("You are hit by the "+hitname);
			Event e=new Event("Effect");
			e.set("Strength",magicPower);
			e.set("Caster",caster);
			e.set("Target",target);
			spell.handle(e);
		} else {
			if (visible) Game.messageTyrant(target.getTheName()+" "+target.verb("resist")+" the "+hitname);
		}
	}


	private static final double spellPowerMultiplier=1.1;
	
	/**
	 * Add spell to library 
	 * This should be called for all spells
	 * @param t Spell to add to library
	 */ 
	private static void addSpell(Thing t) {
		String name=t.getString("Name");
		spellNames.add(name);
		
		Game.assertTrue(t.getFlag("IsSpell"));

		int level=t.getStat("Level");
		Game.assertTrue(level>0);
		t.set("LevelMin",level);

		int skillMin=(t.getStat("SpellCost"))*3;
		t.set("SkillMin",skillMin);
		
		t.set("Image",t.getStat("BoltImage"));
		t.set("ImageSource","Effects");
		
		//int power=(int)(6*Math.pow(spellPowerMultiplier,level));
		//Game.assertTrue(power>0);
		//t.set("Power",power);
		Lib.add(t);
	}
	
	public static void updateIngredients() {
		
		for (Iterator it=spellNames.iterator(); it.hasNext();) {
			updateIngredients(Lib.getLibraryInstance((String)it.next()));
		}
	}
	
	public static void updateIngredients(Thing t) {
		int level=t.getStat("Level");
		
		String order=t.getString("Order");
		if (order.equals(Skill.BLACKMAGIC)) {
			// black magic spells have zero cost
			t.set("SpellCost",0);
			String ings=t.getString("Ingredients");
			int count=t.getStat("RandomIngredientCount");
			
			if ((ings==null)&&(count<=0)) {
				Game.warn("Black Magic spell "+t.name()+" has no ingredient list!");
			}
			
			for(int i=0; i<count; i++) {
				Thing it=Lib.createType("IsBlackIngredient",level);
				ings=(ings==null)?it.name():ings+","+it.name();
			}
			t.set("Ingredients",ings);
		}
	}

	/**
	 * Make final item spell modifications
	 */ 
	private static void addItemSpell(Thing t) {
		addSpell(t);
	}
	
	/**
	 * Make final offensive spell modifications
	 */ 
	private static void addOffensiveSpell(Thing t) {
		if (t.getString("HitName")==null) {
			t.set("HitName",t.name()+" spell");
		}
		t.set("IsOffensiveSpell",1);
		addSpell(t);
	}
	
	/**
	 * Make final defensive spell modifications
	 */ 
	private static void addDefensiveSpell(Thing t) {
		t.set("IsDefensiveSpell",1);
		addSpell(t);
	}
	
	private static void addUtilitySpell(Thing t) {
		
		addSpell(t);
	}

	private static void initSummonSpells() {
		Thing t;
		
		t=Lib.extend("base summon spell","base defensive spell");
		t.set("IsSummonSpell",1);
		Lib.add(t);
		
		t=Lib.extend("Summon Creature","base summon spell");
		t.set("OnEffect",createSummonEffect("[IsBeast]",1,1));
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("Frequency",50);
		t.set("BoltImage",120);
		t.set("LevelMin",1);
		t.set("SpellCost",10);
		Lib.add(t);
		
		t=Lib.extend("Summon Undead","base summon spell");
		t.set("OnEffect",createSummonEffect("[IsUndead]",2,6));
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("SpellOrder",Skill.BLACKMAGIC);
		t.set("Frequency",50);
		t.set("BoltImage",120);
		t.set("LevelMin",8);
		t.set("SpellCost",30);
		Lib.add(t);
		
		t=Lib.extend("Summon Demons","base summon spell");
		t.set("OnEffect",createSummonEffect("[IsDemonic]",2,6));
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("SpellOrder",Skill.TRUEMAGIC);
		t.set("Frequency",50);
		t.set("BoltImage",120);
		t.set("LevelMin",12);
		t.set("SpellCost",50);
		Lib.add(t);
	}
	
	private static void initDefensiveSpells() {
		Thing t;
		
		t=Lib.extend("base defensive spell","base spell");
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("SpellUsage",Spell.SPELL_DEFENCE);
		t.set("BoltImage",121);
		t.set("Frequency",50);
		addDefensiveSpell(t);
		
		t=Lib.extend("Bless","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("Frequency",50);
		t.set("LevelMin",11);
		t.set("SpellCost",10);
		t.set("OnEffect",AddScript.create("blessing",100));
		t.set("Order",Skill.HOLYMAGIC);
		addDefensiveSpell(t);
		
		t=Lib.extend("Lucky Charm","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("Frequency",50);
		t.set("LevelMin",1);
		t.set("SpellCost",1);
		t.set("Ingredients","blue mushroom");
		t.set("OnEffect",AddScript.create("blessing",100));
		t.set("Order",Skill.BLACKMAGIC);
		addDefensiveSpell(t);
		
		t=Lib.extend("Stone Skin","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("Frequency",50);
		t.set("BoltImage",81);
		t.set("LevelMin",7);
		t.set("SpellCost",5);
		t.set("OnEffect",AddScript.create("stone skin",100));
		addDefensiveSpell(t);
		
		t=Lib.extend("God's Protection","Stone Skin");
		t.set("Order",Skill.HOLYMAGIC);
		addDefensiveSpell(t);
		
		t=Lib.extend("Cloak Of Stone","Stone Skin");
		t.set("LevelMin",6);
		t.set("Order",Skill.BLACKMAGIC);
		t.set("Ingredients","blue mushroom,rock");
		Lib.add(t);	
		
		t=Lib.extend("Inspire Fear","base defensive spell");
		t.set("LevelMin",8);
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("Frequency",50);
		t.set("SpellCost",8);
		t.set("OnEffect",AddScript.create("fearsome",100));
		addDefensiveSpell(t);
		
		t=Lib.extend("Holy Awe","Inspire Fear");
		t.set("Order",Skill.HOLYMAGIC);
		t.set("LevelMin",6);
		t.set("SpellCost",6);
		addDefensiveSpell(t);
		
		t=Lib.extend("Haste","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("Frequency",50);
		t.set("LevelMin",13);
		t.set("SpellPowerMultiplier",20);
		t.set("SpellPowerBonus",3);
		t.set("SpellCost",5);
		t.set("OnEffect",AddScript.create("haste",100));
		addDefensiveSpell(t);
		
		t=Lib.extend("God's Speed","Haste");
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("Order",Skill.HOLYMAGIC);
		t.set("SpellPowerMultiplier",30);
		addDefensiveSpell(t);
		
		t=Lib.extend("Haste Self","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("Frequency",50);
		t.set("LevelMin",11);
		t.set("SpellPowerMultiplier",20);
		t.set("SpellPowerBonus",3);
		t.set("SpellCost",5);
		t.set("OnEffect",AddScript.create("haste",100));
		addDefensiveSpell(t);
		
		t=Lib.extend("Teleport Self","base defensive spell");
		t.set("Level",18);
		t.set("SpellCost",40);
		t.set("SpellDamageMultiplier",0);
		t.set("BoltImage",2);
		t.set("OnEffect",new TeleportEffect());
		t.set("Order",Skill.TRUEMAGIC);

		addDefensiveSpell(t);

		
	}

	private static void initUtilitySpells() {
		Thing t;
		
		t=Lib.extend("base utility spell","base spell");
		t.multiplyStat("ChargeRate",0.2);
		t.set("SpellUsage",Spell.SPELL_TACTICAL);
		t.set("BoltImage",121);
		Lib.add(t);
		
		t=Lib.extend("Magic Shovel","base utility spell");
		t.set("SpellTarget",Spell.TARGET_DIRECTION);
		t.set("LevelMin",14);
		t.set("Frequency",30);
		t.set("SpellCost",40);
		t.set("Order",Skill.TRUEMAGIC);
		t.set("OnLocationEffect",new Script() {
			private static final long serialVersionUID = 3979270248400893753L;

            public boolean handle(Thing t, Event e) {
				BattleMap m=(BattleMap)e.get("TargetMap");
				if (m==null) return false;
				int x=e.getStat("TargetX");
				int y=e.getStat("TargetY");
				Tile.dig(m,x,y);
				
				return false;
			}
		});
		addUtilitySpell(t);	
		
		t=Lib.extend("Open Rock","Magic Shovel");
		t.set("LevelMin",14);
		t.set("Frequency",30);
		t.set("Order",Skill.HOLYMAGIC);
		addUtilitySpell(t);
		
	}
	
	private static void initCureSpells() {
		Thing t;

		t=Lib.extend("base cure spell","base defensive spell");
		t.multiplyStat("ChargeRate",0.5);
		t.set("BoltImage",21);
		Lib.add(t);
		
		t=Lib.extend("Cure Poison","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("LevelMin",8);
		t.set("Frequency",50);
		t.set("SpellPowerMultiplier",20);
		t.set("SpellPowerBonus",8);
		t.set("Order",Skill.HOLYMAGIC);
		t.set("OnEffect",new CureScript());
		addDefensiveSpell(t);
		
		t=Lib.extend("Curing Light","Cure Poison");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("LevelMin",10);
		t.set("Frequency",50);
		t.set("SpellCost",15);
		t.set("BoltImage",21);
		t.set("Order",Skill.TRUEMAGIC);
		t.set("OnEffect",new CureScript());
		addDefensiveSpell(t);

	}
	
	private static void initHealingSpells() {
		Thing t;
		
		// Holy Magic healing
		t=Lib.extend("Light Heal","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_SELF);
		t.set("Frequency",50);
		t.set("LevelMin",1);
		t.set("PowerMultiplier",10);
		t.set("PowerBonus",3);
		t.set("SpellCost",3);
		t.set("OnEffect",new HealingScript());
		t.set("Order",Skill.HOLYMAGIC);
		addDefensiveSpell(t);
		
		t=Lib.extend("Heal","base defensive spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("Frequency",50);
		t.set("LevelMin",3);
		t.set("PowerMultiplier",20);
		t.set("PowerBonus",3);
		t.set("SpellCost",5);
		t.set("OnEffect",new HealingScript());
		t.set("Order",Skill.HOLYMAGIC);
		addDefensiveSpell(t);
		
		// True magic versions
		t=Lib.extend("Heal Light Wounds","Light Heal");
		t.set("LevelMin",3);
		t.set("PowerMultiplier",10);
		t.set("PowerBonus",3);
		t.set("SpellCost",4);
		t.set("Order",Skill.TRUEMAGIC);
		Lib.add(t);
		
		t=Lib.extend("Heal Wounds","Heal Light Wounds");
		t.set("LevelMin",10);
		t.set("PowerMultiplier",20);
		t.set("PowerBonus",6);
		t.set("SpellCost",12);
		t.set("Order",Skill.TRUEMAGIC);
		Lib.add(t);
		
		// Black magic versions
		t=Lib.extend("Healing Charm","Heal");
		t.set("Order",Skill.BLACKMAGIC);
		t.set("RandomIngredientCount",1);
		t.set("LevelMin",1);
		addDefensiveSpell(t);		
	}
	
	public static ArrayList getSpellNames() {
		return (ArrayList)spellNames.clone();
	}
	
	private static void initCurseSpells() {
		Thing t;
		
		t=Lib.extend("base curse spell","base spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("BoltImage",141);
		t.set("SpellUsage",Spell.SPELL_OFFENCE);
		t.set("Order",Skill.BLACKMAGIC);
		t.set("Frequency",50);
		t.set("SpellRange",6);
		Lib.add(t);
		
		t=Lib.extend("Curse","base curse spell");
		t.set("Level",8);
		t.set("SpellCost",6);
		t.set("HitName","malevolent magic");
		t.set("Radius",0);
        {	
        	Thing effect=Lib.create("curse");
        	Script script=Scripts.addEffect("Target",effect);
        	t.set("OnEffect",script);
        }
		t.set("Order",Skill.HOLYMAGIC);
		addOffensiveSpell(t);
		
		t=Lib.extend("Blind","base curse spell");
		t.set("Level",10);
		t.set("SpellCost",15);
		t.set("HitName","malevolent magic");
		t.set("Radius",0);
        {	
        	Thing effect=Lib.create("curse of blindness");
        	Script script=Scripts.addEffect("Target",effect);
        	t.set("OnEffect",script);
        }
		t.set("Order",Skill.BLACKMAGIC);
		t.set("RandomIngredientCount",2);
		addOffensiveSpell(t);
		
		t=Lib.extend("Slow","base curse spell");
		t.set("Level",10);
		t.set("SpellCost",15);
		t.set("HitName","malevolent magic");
		t.set("Radius",0);
        t.set("OnEffect",AddScript.create("slow",100));
		t.set("Order",Skill.BLACKMAGIC);
		t.set("RandomIngredientCount",1);
		addOffensiveSpell(t);
		
		t=Lib.extend("Confuse","base curse spell");
		t.set("Level",16);
		t.set("SpellCost",15);
		t.set("HitName","malevolent magic");
		t.set("Radius",0);
        {	
        	Thing effect=Lib.create("confusion");
        	Script script=Scripts.addEffect("Target",effect);
        	t.set("OnEffect",script);
        }
		t.set("Order",Skill.BLACKMAGIC);
		t.set("RandomIngredientCount",3);
		addOffensiveSpell(t);
		
	}
	
	private static void initItemSpells() {
		Thing t;
		
		t=Lib.extend("base item spell","base spell");
		t.set("SpellTarget",Spell.TARGET_ITEM);
		t.set("SpellUsage",Spell.SPELL_USELESS);
		t.set("OnEffect",new OffensiveEffect());
		t.set("Frequency",50);
		Lib.add(t);
		
		t=Lib.extend("Repair","base item spell");
		t.set("Level",10);
		t.set("SpellCost",20);
		t.set("SpellPowerMultiplier",20);
		t.set("OnEffect",new Script() {
			private static final long serialVersionUID = 3256999964814293296L;

            public boolean handle(Thing t, Event e) {
				Thing target=e.getThing("Target");
				Thing caster=e.getThing("Caster");
				int eff=e.getStat("Strength");
				int dam=target.getStat(RPG.ST_HPSMAX)-target.getStat(RPG.ST_HPS);
				if ((dam>0)&&RPG.test(eff,dam)) {
					Item.repair(target,false);
					caster.message(target.getYourName()+" glows brightly as it is restored to excellent condition");			
				} else {
					caster.message(target.getYourName()+" glows faintly for a second but nothing else happens");
				}
				return true;
			}
		});
		addItemSpell(t);
		
		t=Lib.extend("Identify","base item spell");
		t.set("Level",11);
		t.set("SpellCost",50);
		t.set("SpellPowerMultiplier",20);
		t.set("OnEffect",new Script() {
			private static final long serialVersionUID = 3257002176856339760L;

            public boolean handle(Thing t, Event e) {
				Thing target=e.getThing("Target");
				Thing caster=e.getThing("Caster");
				Item.identify(target);
				target.set("IsStatusKnown",1);
				caster.message("You identify "+target.getTheName());			
				
				return true;
			}
		});
		addItemSpell(t);	
		
		t=Lib.extend("Destroy Item","base item spell");
		t.set("Level",15);
		t.set("SpellCost",100);
		t.set("OnEffect",new Script() {
			private static final long serialVersionUID = 3906363839908755504L;

            public boolean handle(Thing t, Event e) {
				Thing target=e.getThing("Target");
				Damage.inflict(target,5000,"normal");	
				if (target.place==null) {
					Game.messageTyrant(target.getTheName()+" "+target.is()+" turned to dust");
				} else {
					Game.messageTyrant(target.getTheName()+" trembles violently");
				}
				
				return true;
			}
		});
		addItemSpell(t);	
	}
	
	private static void initOffensiveSpells() {
		Thing t;
		
		t=Lib.extend("base offensive spell","base spell");
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("SpellUsage",Spell.SPELL_OFFENCE);
		t.set("BoltImage",1);
		t.set("PowerMultiplier",20);
		t.set("PowerBonus",2);
		t.set("SpellDamageType","normal");
		t.set("OnEffect",new OffensiveEffect());
		t.set("Frequency",50);
		t.set("SpellRange",6);
		Lib.add(t);
		
		t=Lib.extend("Spark","base offensive spell");
		t.set("Level",1);
		t.set("SpellCost",1);
		t.set("PowerMultiplier",20);
		t.set("PowerBonus",4);
		t.set("BoltImage",61);
		t.set("HitName","spark");
		addOffensiveSpell(t);

		t=Lib.extend("Flame Bolt","base offensive spell");
		t.set("Level",2);
		t.set("SpellCost",2);
		t.set("PowerMultiplier",20);
		t.set("PowerBonus",5);
		t.set("SpellDamageType","fire");
		t.set("BoltImage",1);
		t.set("HitName","flame");
		addOffensiveSpell(t);
		
		t=Lib.extend("Frost Bolt","base offensive spell");
		t.set("Level",4);
		t.set("SpellCost",4);
		t.set("PowerMultiplier",30);
		t.set("PowerBonus",5);
		t.set("SpellDamageType","ice");
		t.set("BoltImage",101);
		t.set("HitName","ice");
		addOffensiveSpell(t);
		
		t=Lib.extend("Force Bolt","base offensive spell");
		t.set("Level",6);
		t.set("SpellCost",6);
		t.set("PowerMultiplier",30);
		t.set("PowerBonus",4);
		t.set("SpellDamageType","impact");
		t.set("BoltImage",81);
		t.set("HitName","force bolt");
		addOffensiveSpell(t);

		t=Lib.extend("Magic Missile","base offensive spell");
		t.set("Level",3);
		t.set("SpellCost",3);
		t.set("PowerMultiplier",40);
		t.set("PowerBonus",5);
		t.set("BoltImage",2);
		t.set("HitName","impact");
		addOffensiveSpell(t);
		
		t=Lib.extend("Punishment","Magic Missile");
		t.set("Order",Skill.HOLYMAGIC);
		addOffensiveSpell(t);
		
		t=Lib.extend("Flame","base offensive spell");
		t.set("Level",5);
		t.set("SpellCost",6);
		t.set("PowerMultiplier",30);
		t.set("PowerBonus",6);
		t.set("SpellDamageType","fire");
		t.set("Order",Skill.BLACKMAGIC);
		t.set("OnLocationEffect",new CreateEffect("small fire",100,false));
		t.set("BoltImage",1);
		t.set("RandomIngredientCount",1);
		addOffensiveSpell(t);
		
		t=Lib.extend("Teleport Monster","base offensive spell");
		t.set("Level",13);
		t.set("SpellCost",20);
		t.set("PowerMultiplier",60);
		t.set("BoltImage",2);
		t.set("OnEffect",new TeleportEffect());
		addOffensiveSpell(t);

		t=Lib.extend("Fireball","base offensive spell");
		t.set("Level",9);
		t.set("SpellCost",6);
		t.set("PowerMultiplier",40);
		t.set("PowerBonus",10);
		t.set("SpellDamageType","fire");
		t.set("BoltImage",3);
		t.set("Radius",1);
		t.set("HitName","flames");
		addOffensiveSpell(t);
		
		t=Lib.extend("Wrath Of Fire","Fireball");
		t.set("Order",Skill.HOLYMAGIC);
		t.set("Level",10);
		t.set("SpellCost",8);
		addOffensiveSpell(t);
		
		t=Lib.extend("Ball Of Fire","base offensive spell");
		t.set("Level",9);
		t.set("SpellCost",5);
		t.set("PowerMultiplier",40);
		t.set("PowerBonus",15);
		t.set("SpellDamageType","fire");
		t.set("BoltImage",3);
		t.set("Radius",1);
		t.set("HitName","flames");
		t.set("Order",Skill.BLACKMAGIC);
		t.set("Ingredients","snake skin");
		addOffensiveSpell(t);
		
		t=Lib.extend("Ice Blast","base offensive spell");
		t.set("Level",13);
		t.set("SpellCost",10);
		t.set("PowerMultiplier",35);
		t.set("PowerBonus",15);
		t.set("SpellDamageType","ice");
		t.set("BoltImage",103);
		t.set("Radius",2);
		t.set("HitName","icy blast");
		t.set("Order",Skill.TRUEMAGIC);
		addOffensiveSpell(t);
		
		t=Lib.extend("Acid Ball","base offensive spell");
		t.set("Level",11);
		t.set("SpellCost",6);
		t.set("PowerMultiplier",40);
		t.set("PowerBonus",10);
		t.set("SpellDamageType","acid");
		t.set("BoltImage",23);
		t.set("Radius",1);
		t.set("HitName","burning acid");
		addOffensiveSpell(t);
		
		t=Lib.extend("Firepath","base offensive spell");
		t.set("Level",17);
		t.set("SpellCost",12);
		t.set("PowerMultiplier",50);
		t.set("PowerBonus",25);
		t.set("SpellDamageType","fire");
		t.set("BoltImage",3);
		t.set("Radius",1);
		t.set("HitName","searing flames");
		t.set("OnPathEffect",new CreateEffect("small fire",90));
		addOffensiveSpell(t);
		
		t=Lib.extend("Blast","base offensive spell");
		t.set("Level",22);
		t.set("SpellCost",10);
		t.set("PowerMultiplier",45);
		t.set("PowerBonus",30);
		t.set("SpellDamageType","impact");
		t.set("BoltImage",3);
		t.set("Radius",2);
		t.set("HitName","blast");
		t.set("Order",Skill.TRUEMAGIC);
		addOffensiveSpell(t);
		
		t=Lib.extend("Blaze","base offensive spell");
		t.set("Level",24);
		t.set("SpellCost",20);
		t.set("PowerMultiplier",50);
		t.set("PowerBonus",30);
		t.set("SpellDamageType","fire");
		t.set("BoltImage",3);
		t.set("Radius",2);
		t.set("HitName","searing flames");
		t.set("OnLocationEffect",new CreateEffect("small fire",50));
		addOffensiveSpell(t);
		
		t=Lib.extend("Firestorm","base offensive spell");
		t.set("Level",26);
		t.set("SpellCost",26);
		t.set("PowerMultiplier",55);
		t.set("PowerBonus",40);
		t.set("SpellDamageType","fire");
		t.set("BoltImage",3);
		t.set("Radius",3);
		t.set("HitName","roaring flames");
		t.set("OnLocationEffect",new CreateEffect("medium fire",70,false));
		addOffensiveSpell(t);

		
		t=Lib.extend("Poison Cloud","base offensive spell");
		t.set("Level",7);
		t.set("SpellCost",0);
		t.set("PowerMultiplier",25);
		t.set("PowerBonus",5);
		t.set("SpellDamageType","poison");
		t.set("Ingredients","red snake skin,rat tail");
		t.set("Order",Skill.BLACKMAGIC);
		t.set("BoltImage",42);
		t.set("Radius",1);
		t.set("HitName","poison spray");
		t.set("OnLocationEffect",new CreateEffect("poison cloud"));
		addOffensiveSpell(t);
		
		t=Lib.extend("Flame Cloud","base offensive spell");
		t.set("Level",12);
		t.set("SpellCost",0);
		t.set("PowerMultiplier",30);
		t.set("PowerBonus",10);
		t.set("SpellDamageType","fire");
		t.set("Order",Skill.TRUEMAGIC);
		t.set("BoltImage",2);
		t.set("Radius",1);
		t.set("HitName","flaming blast");
		t.set("OnLocationEffect",new CreateEffect("flame cloud"));
		addOffensiveSpell(t);
		
		t=Lib.extend("Acid Cloud","base offensive spell");
		t.set("Level",16);
		t.set("SpellCost",0);
		t.set("PowerMultiplier",30);
		t.set("PowerBonus",10);
		t.set("SpellDamageType","acid");
		t.set("Order",Skill.TRUEMAGIC);
		t.set("BoltImage",22);
		t.set("Radius",1);
		t.set("HitName","acidic blast");
		t.set("OnLocationEffect",new CreateEffect("acid cloud"));
		addOffensiveSpell(t);
		
		t=Lib.extend("Aruk's Poison Cloud","base offensive spell");
		t.set("Level",7);
		t.set("SpellCost",4);
		t.set("PowerMultiplier",30);
		t.set("PowerBonus",10);
		t.set("SpellDamageType","poison");
		t.set("BoltImage",42);
		t.set("Radius",1);
		t.set("HitName","poison spray");
		t.set("OnLocationEffect",new CreateEffect("poison cloud"));
		addOffensiveSpell(t);

		t=Lib.extend("Thunderbolt","base offensive spell");
		t.set("Level",21);
		t.set("Frequency",0);
		t.set("SpellCost",25);
		t.set("PowerMultiplier",70);
		t.set("PowerBonus",100);
		t.set("SpellDamageType",RPG.DT_SHOCK);
		t.set("HitName","thunder bolt");
		t.set("BoltImage",22);
		t.set("Order",Skill.TRUEMAGIC);
		addOffensiveSpell(t);	
		
		t=Lib.extend("Ball Lightning","base offensive spell");
		t.set("Level",28);
		t.set("Frequency",0);
		t.set("SpellCost",50);
		t.set("PowerMultiplier",60);
		t.set("PowerBonus",80);
		t.set("SpellDamageType",RPG.DT_SHOCK);
		t.set("HitName","lightning");
		t.set("Radius",1);
		t.set("BoltImage",62);
		t.set("Order",Skill.TRUEMAGIC);
		addOffensiveSpell(t);	
		
		t=Lib.extend("Sheet Lightning","base offensive spell");
		t.set("Level",32);
		t.set("Frequency",0);
		t.set("SpellCost",50);
		t.set("PowerMultiplier",70);
		t.set("PowerBonus",100);
		t.set("SpellDamageType",RPG.DT_SHOCK);
		t.set("HitName","lightning");
		t.set("Radius",2);
		t.set("BoltImage",63);
		t.set("Order",Skill.TRUEMAGIC);
		addOffensiveSpell(t);	
		
		t=Lib.extend("Annihilation","base offensive spell");
		t.set("Level",34);
		t.set("Frequency",0);
		t.set("SpellCost",100);
		t.set("PowerMultiplier",100);
		t.set("PowerBonus",200);
		t.set("SpellDamageType",RPG.DT_DISINTEGRATE);
		t.set("HitName","pure destructive energy");
		t.set("BoltImage",85);
		addOffensiveSpell(t);	
		
		t=Lib.extend("Gargash Vapouriser","base offensive spell");
		t.set("Level",42);
		t.set("Frequency",0);
		t.set("SpellCost",200);
		t.set("PowerMultiplier",100);
		t.set("PowerBonus",200);
		t.set("SpellDamageType",RPG.DT_DISINTEGRATE);
		t.set("HitName","pure destructive energy");
		t.set("BoltImage",44);
		t.set("Radius",1);
		addOffensiveSpell(t);	
		
		t=Lib.extend("Void","base offensive spell");
		t.set("Level",50);
		t.set("Frequency",0);
		t.set("SpellCost",100);
		t.set("PowerMultiplier",100);
		t.set("PowerBonus",200);
		t.set("SpellDamageType",RPG.DT_DISINTEGRATE);
		t.set("HitName","pure destructive energy");
		t.set("BoltImage",85);	
		t.set("OnLocationEffect",new Script() {
			private static final long serialVersionUID = 3979270248400893753L;

            public boolean handle(Thing t, Event e) {
				BattleMap m=(BattleMap)e.get("TargetMap");
				if (m==null) return false;
				int x=e.getStat("TargetX");
				int y=e.getStat("TargetY");
				m.setTile(x,y,Tile.VOID);
				
				return false;
			}
		});
		addOffensiveSpell(t);
		
		t=Lib.extend("Ultimate Destruction","base offensive spell");
		t.set("Level",50);
		t.set("Frequency",0);
		t.set("SpellCost",1000);
		t.set("PowerMultiplier",100);
		t.set("PowerBonus",2000);
		t.set("SpellDamageType",RPG.DT_SPECIAL);
		t.set("HitName","pure destructive energy");
		t.set("BoltImage",85);
		t.set("Radius",4);
		addOffensiveSpell(t);	
		
		
	}

	public static String spellReport() {
		StringBuffer ss=new StringBuffer();
		ArrayList spells=new ArrayList();
		
		for (int i=0; i<spellNames.size(); i++) {
			String name=(String)spellNames.get(i);
			Thing s=Lib.create(name);
			spells.add(s);
		}
			
		// sort spells
		Collections.sort(spells,new Comparator() {
			public int compare(Object aa, Object bb) {
				Thing a=(Thing)aa;
				Thing b=(Thing)bb;
				int ord=a.getString("Order").compareTo(b.getString("Order"));
				if (ord!=0) return ord;
				
				int lev=-(a.getLevel()-b.getLevel());
				if (lev!=0) return lev;
				
				return 0;
			}
		});
		
		for (int i=0; i<spells.size(); i++) {
			Thing s=(Thing)spells.get(i);
			String name=s.name();
			ss.append(Text.rightPad(name+": ",25));
			ss.append(Text.rightPad(s.getString("Order"),20));
		    ss.append(Text.rightPad("Lv. "+s.getLevel(),10));
		    ss.append(Text.rightPad(s.getString("Ingredients"),40));
				
			ss.append("\n");
		}

		
		return ss.toString();
	}


	public static void init() {
		spellNames=new ArrayList();
		
		Thing t;
		
		t=Lib.extend("base spell","base thing");
		t.set("IsWishable",1);
		t.set("IsArt",1);
		t.set("IsSpell",1);
		t.set("Frequency",100);
		t.set("LevelMin",1);
		t.set("SpellCost",8);
		t.set("PowerMultiplier",100);
		t.set("PowerBonus",0);
		t.set("SpellTarget",Spell.TARGET_LOCATION);
		t.set("SpellUsage",Spell.SPELL_OFFENCE);
		t.set("Order",Skill.TRUEMAGIC);
		t.set("BoltImage",2);
		t.set("Radius",0);
		t.set("ChargeRate",100);
		t.set("Charges",1);
		Lib.add(t);
		
		initOffensiveSpells();
		initDefensiveSpells();
		initSummonSpells();
		initCurseSpells();
		initItemSpells();
		initHealingSpells();
		initCureSpells();
		initUtilitySpells();

	}
}