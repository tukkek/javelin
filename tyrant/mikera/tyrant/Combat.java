/*
 * Created on 12-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 * 
 * Contains code for the execution of combat actions
 * 
 */
public class Combat {
	
	public static final boolean DEBUG = false;
	
	public static int calcAttackSpeed(Thing t) {
		int ms=t.getStat(RPG.ST_ATTACKSPEED);
		int fer=t.getStat(Skill.FEROCITY);
		if (fer>0) {
			ms+=RPG.min(100,t.getStat("SK"))*fer/4;
		}
		return ms;
	}
	
	// attack a given target with HtH weapon
	public static int attack(Thing a, Thing target) {
		if (target == null)
			return 0;
		
		if (a.handles("OnAttack")) {
			Event e=new Event("Attack");
			e.set("Attacker",target);
			e.set("Target",target);
			if (a.handle(e)) {
				return e.getStat("Damage");
			}
		}
		
		int result = 0;

		if (a.getStat(RPG.ST_PANICKED) > 0) {
			if (RPG.test(a.getStat(RPG.ST_PANICKED), a.getStat(RPG.ST_WP))) {
				if (a.isVisible(Game.hero()))
					Game.messageTyrant(a.getTheName() + " freeze"
							+ (a.isHero() ? "" : "s") + " in terror");
				return 0;
			}
		}
		
		int ff=target.getStat("FearFactor");
		if ((ff>0)&&(Being.feelsFear(a))) {
			if ((a.getStat(RPG.ST_WP)*(1+a.getStat(Skill.BRAVERY)))>=RPG.r(target.getStat(RPG.ST_WP)*ff)) {
				// bravery check passed
			} else {
				a.visibleMessage(a.getTheName()+" "+a.verb("freeze")+" in terror and "+a.is()+" unable to attack!");
				a.incStat("APS",-60);
				return 0;
			}
		}

		int aspeed = a.getStat("AttackSpeed");
		if (aspeed <= 0) {
			a.set("APS", -1);
			Game.warn(a.getName(Game.hero())+" is too slow to attack!");
			return 0;
		}

		// get main weapon
		Thing w = a.getWielded(RPG.WT_MAINHAND);

		if ((w !=null)&&(w.getFlag("IsWeapon"))) {
			// main attack
			result += attackWith(a, target, w);

			// attack with second weapon if available
			w = a.getWielded(RPG.WT_SECONDHAND);
			if ((w !=null)&&(w.getFlag("IsWeapon")&&(!target.isDead()))) {
				result += attackWith(a, target, w);
			}
		} else {
			w = a.getWielded(RPG.WT_HANDS);
			if (w!=null) {
				// fighting with gloves
				result += attackWith(a, target, w);
			} else {
			
				// use default weapon for unarmed creatures
				w = Combat.unarmedWeapon(a);
				result += attackWith(a, target, w);
			}
		}

		return result;
	}

	private static Thing unarmedWeapon=null;
	private static Thing baseWeapon=null;
	public static Thing unarmedWeapon(Thing t) {
		// see if creature has a special weapon
		Thing uw=(Thing)t.get("UnarmedWeapon");
		if (uw!=null) return uw;
			
		// fall back to default unarmed weapon	
		if (unarmedWeapon==null) {
			unarmedWeapon=Lib.create("unarmed attack");
			baseWeapon=Lib.create("base attack");
		}
		
		if (t.getStat(Skill.UNARMED)<=0) {
			return unarmedWeapon;
		} 
			
		return baseWeapon;
	}
	
	private static Thing kickingWeapon;

	public static Thing kickingWeapon(Thing t) {
		if (kickingWeapon==null) {
			kickingWeapon=Lib.create("kick attack");
		}
		
		Thing weapon=t.getWielded(RPG.WT_BOOTS);
		if (weapon==null) {
			weapon=kickingWeapon;
		}
		
		return weapon;
	}
	
	public static int attackCost(Thing a, Thing w) {
		double acost= w.getStat(RPG.ST_ATTACKCOST);
		acost += a.getStat(RPG.ST_ATTACKCOST);
		
		if (w.getFlag("IsUnarmedWeapon")) {
			acost=acost/(1.0+0.1*a.getStat(Skill.UNARMED));
		}
		
		return (int)((100*acost)/calcAttackSpeed(a));
	}
	
	public static int attackWith(Thing a, Thing target, Thing w) {
		if (target == null)
			return 0;
		
		int result = 0;

		BattleMap map = target.getMap();

		if (Weapon.attack(w,a, target) > 0) {

			// get result from hitting with weapon
			// result = hps damage inflicted
			result = Weapon.hit(w, a, target);
		}

		a.incStat("APS", -attackCost(a,w));

		// notify nearby creatures of attack
		if ((map != null)&&a.isHero()) {
			map.areaNotify(a.x, a.y, 8, AI.EVENT_ALARM, target
					.getStat(RPG.ST_SIDE), a);
		}
		
		return result;
	}


	public static void die(Thing t) {
	    
	    if (t.getFlag("IsImmortal")) {
	    	t.set("HPS",1);
	    	return;
	    }
	    
	    BattleMap m = t.getMap();
		int tx=t.getMapX();
		int ty=t.getMapY();
		
		if (t.getFlag("IsItem")||t.getFlag("IsOwned")) {
	    	if (t.place instanceof Thing && ((Thing)t.place).isHero()) {
	    		Game.messageTyrant("Your "+t.getFullName(Game.hero())+" "+t.is()+" destroyed!");
	    	} 
	    
	    	if (m != null && (Game.actor.isHero() && t.getFlag("IsOwned"))) {
	    		// local folks don't approve of vandalism!!
	    		m.areaNotify(tx, ty, 6, AI.EVENT_THEFT, 1, Game.actor);
	    	}
	    }
		
		// remove from map
		t.remove();
		
		if (m!=null) {
			if (t!=Game.hero()) {
				// drop all items
				Thing[] stuff = t.getFlaggedContents("IsItem");
				if (stuff != null) {
					boolean impact="impact".equals(t.getstring("KillingDamageType"));
					for (int i = 0; i < stuff.length; i++) {
						m.addThing(stuff[i], tx, ty);
						if (impact) stuff[i].displace();
					}
				}
			} else {
				// save the killer for the hiscore table
				t.set("Killer",Game.actor);
			}
			
			// add decorative touch
			String s=t.getstring("DeathDecoration");
			if (s!=null) {
				m.addThing(s,tx,ty);
			}
		}

		// handle event first
		if (t.handles("OnDeath")) {
			Event deathEvent=new Event("Death");
			deathEvent.set("DeathMap",m);
			deathEvent.set("DeathX",tx);
			deathEvent.set("DeathY",ty);
		    t.handle(deathEvent);
		}		
		
		Game.registerDeath(t);
		t.remove();
		t.set("APS",-1000);
		t.set("HPS",RPG.min(0,t.getStat("HPS")));
	}

	private static void kickEffect(Thing b, Thing t) {
		// Combat.damage(t,b.getStat("ST")/2,"unarmed");		
		Thing kickWeapon=kickingWeapon(b);
		
		Game.instance().pushMessages();
		attackWith(b,t,kickWeapon);
		
		java.util.ArrayList al=Game.instance().popMessages();
		if (t.isDead()) {
			String verbed=(t.getFlag("IsLiving"))?"killed":"destroyed";
			b.message(t.getTheName()+" is "+verbed+" by your kick");
		}		
		Game.message(al);
	}
	
	// kick in specified direction
	public static void kick(Thing b,int dx, int dy) {
		b.incStat("APS",-100);
		
		BattleMap m = b.getMap();
		Thing t = m.getObjects(b.x + dx, b.y + dy);
		while (t != null) {
			if (t.isBlocking()) {
				// b.message("You kick " + t.getTheName());
				kickEffect(b,t);
				return;
			}
			t = t.next;
		}
		if (m.isTileBlocked(b.x + dx, b.y + dy)) {
			Tile.kick(b,m,b.x + dx, b.y + dy);
			
			return;
		}
		t = m.getObjects(b.x + dx, b.y + dy);

		while (t != null) {
			if ((t.getFlag("IsItem")) || (t.getFlag("IsScenery"))) {
				if (!(t.getFlag("IsDecoration"))) {
					//if (t.getFlag("IsItem")) {
					//	int nx = b.x + dx * 2;
					//	int ny = b.y + dy * 2;
					//	if (!(m.isBlocked(nx, ny) || t.isOwned())) {
					//		Movement.moveTo(t, m, nx, ny);
					//	}
					//}
					kickEffect(b,t);
					return;
				}
			}
			t = t.next;
		}
		
		Tile.kick(b,m,b.x+dx,b.y+dy);
	}	
}