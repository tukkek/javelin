/*
 * Created on 12-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import javelin.controller.Movement;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Script;
import tyrant.mikera.engine.Thing;

/**
 * This class contains initialisation routines for the monster library
 * 
 */
public class Monster {
	public static final String TAKE_THE_MONEY_AND_RUN = "TakeTheMoneyAndRun";
	public static final String TAKE_THE_MAGIC_AND_RUN = "TakeTheMagicAndRun";

	public static void init() {
		initBase();
		initGoblinoids();
		initCritters();
		initDogs();
		initFrogs();
		initSpiders();
		initRalkans();
		initSlimes();
		initCats();
		initBears();
		initPlants();
		initNamedFoes();
		initEyes();
		initSnakes();
		initDragons();
		initWorms();
		initBirds();
		initInsects();
		initCentipedes();
		initBandits();
		initChaosForces();
		initUndead();
		initDemons();
		initImps();
		initBigHumanoids();
		initBigNasties();
		initElementals();
		initKobolds();
		initUrchins();
		initVoidlings();
		initVortices();
		initjavelin();
	}

	private static void initjavelin() {
		Thing t = Lib.extend("base objects", "base monster");
		t.set("IsBeast", 1);
		t.set("IsDragon", 1);
		t.set("IsReptile", 1);
		t.set("IsViewBlocking", 1);
		t.set("Frequency", 20);
		stats(t, 120, 140, 100, 200, 250, 190, 200, 240);
		t.set("ARM", 300);
		t.set("AttackSpeed", 200);
		t.set("RES:poison", 20);
		t.set("RES:fire", 10);
		t.set("MoveSpeed", 200);
		t.set("Luck", 70);
		t.set("IsFlying", 1);
		t.set(Skill.UNARMED, 3);
		t.set(Skill.ATTACK, 3);
		t.set(Skill.DEFENCE, 4);
		t.set(Skill.BRAVERY, 2);
		t.set(Skill.CASTING, 2);
		t.set(Skill.FOCUS, 3);
		t.set("UnarmedWeapon", Lib.create("razor claw attack"));
		t.set("LevelMin", 30);
		t.set("Image", 645);
		Lib.add(t);

		t = Lib.extend("small object", "base dragon");
		Monster.strengthen(t, 0.8);
		t.set("RES:poison", 1000);
		t.set("LevelMin", 29);
		t.set("Image", 480);
		t.addHandler("OnAction",
				breathAttack("poison", "noxious fumes", 2, 40, 41));
		Lib.add(t);

		t = Lib.extend("medium object", "base dragon");
		Monster.strengthen(t, 0.8);
		t.set("RES:poison", 1000);
		t.set("LevelMin", 29);
		t.set("Image", 481);
		t.addHandler("OnAction",
				breathAttack("poison", "noxious fumes", 2, 40, 41));
		Lib.add(t);

		t = Lib.extend("big object", "base dragon");
		Monster.strengthen(t, 0.8);
		t.set("RES:poison", 1000);
		t.set("LevelMin", 29);
		t.set("Image", 482);
		t.addHandler("OnAction",
				breathAttack("poison", "noxious fumes", 2, 40, 41));
		Lib.add(t);

		t = Lib.extend("merfolk", "base dragon");
		Monster.strengthen(t, 0.8);
		t.set("RES:poison", 1000);
		t.set("LevelMin", 29);
		t.set("Image", 313);
		t.addHandler("OnAction",
				breathAttack("poison", "noxious fumes", 2, 40, 41));
		Lib.add(t);
	}

	public static class BreathAttackScript extends Script {
		private static final long serialVersionUID = 3257006536114256182L;

		@Override
		public boolean handle(final Thing t, final Event e) {
			final int time = e.getStat("Time");
			final int rate = getStat("Rate");

			if (RPG.test(rate, time)) {
				final Thing f = AI.findFoe(t);
				if (f == null) {
					return false;
				}

				final String type = getString("BreathType");
				final String desc = getString("BreathDescription");

				Game.instance().doBreath(t.x, t.y, f.x, f.y,
						getStat("BoltImage"), 10.0);

				t.visibleMessage(t.getTheName() + " " + t.verb("breathe") + " "
						+ desc + " at " + f.getTheName());
				Damage.inflict(f, getStat("Power"), type);
			}

			return false;
		}
	}

	public static Script breathAttack(final String type, final String desc,
			final int rate, final int power, final int c) {
		final Script s = new BreathAttackScript();
		s.set("BreathType", type);
		s.set("BreathDescription", desc);
		s.set("Rate", rate);
		s.set("Power", power);
		s.set("BoltImage", c);
		return s;
	}

	public static class ItemDamageHit extends Script {
		private static final long serialVersionUID = 3761124920951715636L;

		public ItemDamageHit(final String target, final String dt,
				final int chance, final int power) {
			set("SpecialHitTarget", target);
			set("SpecialHitChance", chance);
			set("SpecialHitDamageType", dt);
			set("SpecialHitPower", power);
		}

		@Override
		public boolean handle(final Thing t, final Event e) {
			final int chance = getStat("SpecialHitChance");
			if (RPG.d(100) > chance) {
				return false;
			}
			final String type = getString("SpecialHitTarget");
			// Game.warn(type+" special hit");

			final Thing tt = e.getThing("Target");
			Thing target = null;
			if (type.equals("item")) {
				final Thing[] ts = tt.getItems();
				if (ts.length > 0) {
					target = ts[RPG.r(ts.length)];
				}
			} else if (type.equals("wielded")) {
				final Thing[] ts = tt.getWieldedItems();
				if (ts.length > 0) {
					target = ts[RPG.r(ts.length)];
				}
			} else if (type.startsWith("[")) {
				final Thing[] ts = tt.getFlaggedContents(type.substring(1,
						type.length() - 2));
				if (ts.length > 0) {
					target = ts[RPG.r(ts.length)];
				}
			}

			if (target != null) {
				Damage.inflict(target, getStat("SpecialHitPower"),
						getString("SpecialHitDamageType"));
				return true;
			}

			return false;
		}
	}

	public static class SpecialHit extends Script {
		private static final long serialVersionUID = 3762814904666633524L;

		public SpecialHit(final String type, final int chance) {
			set("SpecialHitChance", chance);
			set("SpecialHitType", type);
		}

		public boolean stealSomething(final Thing target, final Thing actor,
				final String whatType) {
			Thing[] things = null;
			if (whatType == null) {
				things = target.getItems();
			} else {
				things = target.getFlaggedContents(whatType);
			}
			if (things.length == 0) {
				return false; // nothing to steal
			}
			final Thing whatToSteal = things[RPG.r(things.length)];
			if (whatToSteal.y > 0) {
				return false; // don't steal wielded stuff
			} else if (whatToSteal.getFlag("IsTheftProof")) {
				return false; // don't steal theft proof stuff
			} else { // steal the item!
				target.message(actor.getTheName() + " steals your "
						+ whatToSteal.getName(Game.hero()) + "!");
				actor.addThingWithStacking(whatToSteal);
				Being.utiliseItems(actor);
				return true;
			}
		}

		public boolean stealSomething(final Thing target, final Thing actor,
				final String whatType, final int tries) {
			// steal up to tries things
			boolean value = false;
			for (int i = 0; i < tries; i++) {
				if (stealSomething(target, actor, whatType)) {
					value = true;
				}
			}
			return value;
		}

		@Override
		public boolean handle(final Thing thing, final Event e) {
			final int chance = getStat("SpecialHitChance");
			if (RPG.d(100) > chance) {
				return false;
			}
			final String type = getString("SpecialHitType");
			// Game.warn(type+" special hit");

			final Thing target = e.getThing("Target");
			if (!RPG.test(thing.getStat("SK"), target.getStat("SK"), thing,
					target)) {
				return false;
			}
			if (type.equals("steal")) {
				return stealSomething(target, thing, null);
			} else if (type.equals("StealMoney")) {
				return stealSomething(target, thing, "IsMoney");
			} else if (type.equals(TAKE_THE_MONEY_AND_RUN)) {
				if (stealSomething(target, thing, "IsMoney")) {
					final String message = thing.getTheName()
							+ " vanishes in a puff of smoke!";
					if (teleportAway(thing)) {
						target.message(message);
						return true;
					}
				}
			} else if (type.equals(TAKE_THE_MAGIC_AND_RUN)) {
				if (stealSomething(target, thing, "IsMagicItem", 3)) {
					final String message = thing.getTheName()
							+ " vanishes in a cloud of perfume!";
					if (teleportAway(thing)) {
						Game.messageTyrant(message);
						return true;
					}
				}
			}
			return false;
		}

		/*
		 * Do this locally because we want a different message than the one in
		 * the spell effect script for teleportation
		 */
		public boolean teleportAway(final Thing target) {
			final BattleMap map = target.getMap();
			if (map == null) {
				return false;
			}
			final Point point = map.findFreeSquare();
			if (point == null) {
				return false;
			}
			Movement.teleport(target, map, point.x, point.y);
			return true;
		}
	}

	// create stats with a little bit of natural variation
	// to keep game-to-game differences interesting
	static void stats(final Thing t, final int SK, final int ST, final int AG,
			final int TG, final int IN, final int WP, final int CH, final int CR) {
		t.set(RPG.ST_SK, RPG.ln(SK, 0.05));
		t.set(RPG.ST_ST, RPG.ln(ST, 0.05));
		t.set(RPG.ST_AG, RPG.ln(AG, 0.05));
		t.set(RPG.ST_TG, RPG.ln(TG, 0.05));
		t.set(RPG.ST_IN, RPG.ln(IN, 0.05));
		t.set(RPG.ST_WP, RPG.ln(WP, 0.05));
		t.set(RPG.ST_CH, RPG.ln(CH, 0.05));
		t.set(RPG.ST_CR, RPG.ln(CR, 0.05));
	}

	/**
	 * This will multiply *every* stat of the given Thing by the given factor.
	 * 
	 * @param t
	 *            Thing strengthened
	 * @param factor
	 *            The value with which to multiply each stat by
	 */
	static void strengthen(final Thing t, final double factor) {
		for (final String stat : RPG.stats) {
			final int a = t.getBaseStat(stat);
			t.set(stat, (int) (a * factor));
		}
		t.multiplyStat("ARM", factor);
	}

	static void initBase() {
		Thing t;

		// base monster template
		makemonster();

		t = Lib.extend("base humanoid", "base monster");
		t.set("IsHumanoid", 1);
		t.set("IsIntelligent", 1);
		t.set("LevelMin", 5);
		t.set("LevelMax", 100);
		t.set("Alignment", "E");
		t.set("Image", 260); // random critter
		Lib.add(t);
	}

	// static boolean mademonster = false;

	public static void makemonster() {
		// if (mademonster) {
		// return;
		// }
		// mademonster = true;
		final Thing t = Lib.extendCopy("base monster", "base being");
		t.set("Frequency", 50);
		t.set("IsMonster", 1);
		t.set("IsHostile", 1);
		t.set("IsLiving", 1);
		t.set("LevelMin", 1);
		t.set("LevelMax", 100);
		t.set("ViewRange", 6);
		t.set("XPValue", 4);
		t.set("Alignment", "E+");
		t.set("ASCII", "m");
		Lib.add(t);
	}

	static void initUrchins() {
		Thing t;

		t = Lib.extend("wood urchin", "base humanoid");
		stats(t, 6, 4, 9, 3, 5, 4, 4, 3);
		t.set("LevelMin", 2);
		t.set("Image", 341);
		t.set("DefaultThings", "[IsMushroom]");
		t.set(Skill.UNARMED, 1);
		t.set("OnHit", new SpecialHit("steal", 30));
		t.set("ASCII", "u");
		Lib.add(t);

		t = Lib.extend("leprechaun", "base humanoid");
		stats(t, 26, 4, 29, 5, 5, 14, 4, 3);
		t.set("LevelMin", 7);
		t.set("Image", 349);
		t.set("DefaultThings", "15* gold coin,50%[IsFood]");
		t.set(Skill.UNARMED, 1);
		t.incStat(Skill.MAGICRESISTANCE, 3);
		t.set("OnHit", new SpecialHit(TAKE_THE_MONEY_AND_RUN, 100));
		t.set("ASCII", "L");
		Lib.add(t);

		t = Lib.extend("nymph", "base humanoid");
		stats(t, 16, 14, 19, 13, 9, 14, 4, 13);
		t.set("LevelMin", 11);
		t.set("Image", 352);
		t.set("ARM", 20);
		t.set("AttackSpeed", 150);
		t.set("MoveSpeed", 150);
		t.set("DefaultThings",
				"25%[IsRunestone],10%[IsRing],20%[IsSpellBook],5%[IsWand],25%[IsScroll]");
		t.set(Skill.UNARMED, 2);
		t.incStat(Skill.MAGICRESISTANCE, 4);
		t.set("OnHit", new SpecialHit(TAKE_THE_MAGIC_AND_RUN, 100));
		t.set("ASCII", "n");
		Lib.add(t);

		t = Lib.extend("pink urchin", "base humanoid");
		stats(t, 26, 4, 29, 5, 5, 14, 4, 3);
		t.set("LevelMin", 9);
		t.set("Image", 340);
		t.set("DefaultThings", "[IsFood]");
		t.set(Skill.UNARMED, 1);
		t.set("OnHit", new SpecialHit("steal", 50));
		t.set("ASCII", "u");
		Lib.add(t);

		t = Lib.extend("rock urchin", "base humanoid");
		stats(t, 16, 14, 19, 13, 9, 14, 4, 13);
		t.set("LevelMin", 14);
		t.set("Image", 345);
		t.set("ARM", 20);
		t.set("AttackSpeed", 150);
		t.set("MoveSpeed", 150);
		t.set("DefaultThings", "[IsRunestone]");
		t.set(Skill.UNARMED, 1);
		t.set("OnHit", new SpecialHit("steal", 100));
		t.set("ASCII", "u");
		Lib.add(t);

		t = Lib.extend("delver", "base humanoid");
		stats(t, 10, 8, 10, 13, 5, 14, 6, 13);
		t.set("LevelMin", 10);
		t.set("Image", 343);
		t.set("ARM", 20);
		t.set("AttackSpeed", 150);
		t.set("MoveSpeed", 150);
		t.set("Digging", 1);
		t.set("DefaultThings", "20% [IsCoin],30% [IsDiggingTool]");
		t.set(Skill.UNARMED, 1);
		t.set(Skill.MINING, 4);
		t.set("OnHit", new SpecialHit("steal", 100));
		t.set("ASCII", "u");
		Lib.add(t);
	}

	static void initKobolds() {
		Thing t;

		t = Lib.extend("base kobold", "base humanoid");
		t.set("IsKobold", 1);
		stats(t, 6, 4, 9, 3, 5, 4, 4, 3);
		t.set("LevelMin", 2);
		t.set("Image", 342);
		t.set("DefaultThings", "25%[IsWeapon],20%[IsFood]");
		t.set("ASCII", "k");
		Lib.add(t);

		t = Lib.extend("kobold", "base kobold");
		stats(t, 6, 4, 9, 3, 5, 4, 4, 3);
		t.set("LevelMin", 2);
		t.set("LevelMax", 10);
		t.set("Image", 342);
		t.set("DefaultThings", "15%[IsWeapon],10%[IsFood]");
		Lib.add(t);

		t = Lib.extend("kobold warrior", "kobold");
		stats(t, 6, 4, 9, 3, 5, 4, 4, 3);
		t.set("LevelMin", 4);
		t.set("LevelMax", 12);
		t.set("Image", 348);
		t.set(Skill.UNARMED, 1);
		t.set("ARM", 2);
		t.set("DefaultThings", "40%[IsWeapon],10%[IsArmour]");
		Lib.add(t);

		t = Lib.extend("kobold chieftain", "kobold warrior");
		Monster.strengthen(t, 2.0);
		t.set("LevelMin", 8);
		t.set("Image", 348);
		t.set(Skill.ATTACK, 1);
		t.set(Skill.DEFENCE, 1);
		t.set(Skill.UNARMED, 2);
		t.set("DefaultThings", "[IsWeapon],40%[IsArmour]");
		Lib.add(t);

		t = Lib.extend("kobold spellcaster", "kobold");
		stats(t, 10, 4, 19, 13, 15, 14, 8, 13);
		t.set("LevelMin", 10);
		t.set("Image", 348);
		t.set(Skill.ATTACK, 1);
		t.set(Skill.DEFENCE, 2);
		t.set(Skill.UNARMED, 1);
		t.set(Skill.CASTING, 2);
		t.set("DefaultThings", "[IsStaff],[IsSpell],[IsSpell]");
		Lib.add(t);
	}

	static void initBigHumanoids() {
		Thing t;

		t = Lib.extend("base big humanoid", "base humanoid");
		Lib.add(t);

		t = Lib.extend("hill giant", "base big humanoid");
		stats(t, 16, 46, 19, 88, 2, 68, 6, 7);
		t.set(Skill.UNARMED, 1);
		t.set(Skill.DEFENCE, 2);
		t.set("Image", 260);
		t.set("LevelMin", 19);
		t.set("DefaultThings", "30% [IsClub], [IsFood]");
		Lib.add(t);

		t = Lib.extend("ogre", "base big humanoid");
		stats(t, 17, 36, 19, 38, 5, 48, 6, 7);
		t.set(Skill.UNARMED, 1);
		t.set(Skill.DEFENCE, 1);
		t.set("Image", 264);
		t.set("LevelMin", 16);
		t.set("DefaultThings", "50% [IsWeapon], 40% [IsFood],30% [IsCoin]");
		Lib.add(t);

	}

	static void initBandits() {
		Thing t;
		t = Lib.extend("base bandit", "base humanoid");
		stats(t, 10, 8, 9, 9, 7, 8, 6, 7);
		t.set("IsBandit", 1);
		t.set("LevelMin", 5);
		t.set("Image", 84);
		t.set(Skill.UNARMED, 1);
		t.set("DefaultThings",
				"5% [IsFood],5%[IsWeapon],5% 1 gold coin, 50% [IsCoin], 40% 20 copper coin");
		t.set("ASCII", "h");
		Lib.add(t);

		t = Lib.extend("bandit", "base bandit");
		Lib.add(t);

		t = Lib.extend("bandit archer", "base bandit");
		t.set("DefaultThings",
				"[IsBow],10 [IsArrow],5% 1 gold coin, 50% 3 silver coin, 40% 20 copper coin");
		Lib.add(t);

		t = Lib.extend("cutpurse", "base bandit");
		t.set("OnHit", new SpecialHit("steal", 30));
		t.set("Image", 102);
		Lib.add(t);

		t = Lib.extend("thug", "base bandit");
		t.set("Image", 82);
		t.set(Skill.DEFENCE, 1);
		Lib.add(t);

		t = Lib.extend("mutant", "base bandit");
		stats(t, 6, 6, 9, 8, 7, 8, 6, 7);
		t.set("Image", 261);
		t.set("LevelMin", 3);
		Lib.add(t);

		t = Lib.extend("mercenary", "base bandit");
		stats(t, 15, 18, 12, 18, 7, 13, 5, 9);
		t.set("Image", 85);
		t.set("LevelMin", 11);
		t.set(Skill.DEFENCE, 2);
		t.set(Skill.ATTACK, 1);
		t.set("DefaultThings",
				"50%[IsWeapon],10%[IsArmour],5%[IsItem],10 gold coin");
		Lib.add(t);

		t = Lib.extend("swordsman", "base bandit");
		stats(t, 22, 14, 17, 12, 7, 13, 5, 9);
		t.set("Image", 80);
		t.set("LevelMin", 13);
		t.set(Skill.ATHLETICS, 2);
		t.set(Skill.FEROCITY, 3);
		t.set(Skill.DEFENCE, 4);
		t.set(Skill.ATTACK, 4);
		t.set("Luck", 20);
		t.set("DefaultThings", "100%[IsSword],20%[IsArmour],16 gold coin");
		Lib.add(t);

		t = Lib.extend("pirate", "swordsman");
		t.incStat("ARM", 4);
		t.set("Image", 21);
		t.set("AttackSpeed", 130);
		t.set("LevelMin", 15);
		Lib.add(t);

		t = Lib.extend("pirate leader", "pirate");
		Monster.strengthen(t, 2);
		t.set("Image", 21);
		t.set("AttackSpeed", 160);
		t.set("LevelMin", 18);
		Lib.add(t);

		t = Lib.extend("pirate captain", "pirate");
		Monster.strengthen(t, 3);
		t.set("Image", 21);
		t.set("AttackSpeed", 200);
		t.set("LevelMin", 18);
		Lib.add(t);

		t = Lib.extend("mercenary captain", "mercenary");
		stats(t, 25, 23, 22, 28, 17, 23, 15, 19);
		t.set("Image", 86);
		t.set("LevelMin", 14);
		t.set(Skill.DEFENCE, 3);
		t.set(Skill.ATTACK, 2);
		t.set("DefaultThings",
				"100%[IsWeapon],100%[IsArmour],5%[IsItem],10 sovereign");
		Lib.add(t);

		t = Lib.extend("mercenary commander", "mercenary captain");
		stats(t, 45, 43, 42, 48, 37, 33, 35, 39);
		t.set("Image", 87);
		t.set("LevelMin", 20);
		t.set(Skill.DEFENCE, 4);
		t.set(Skill.ATTACK, 4);
		t.set("MoveSpeed", 150);
		t.set("AttackSpeed", 200);
		t.set("DefaultThings",
				"100%[IsWeapon],100%[IsArmour],100%[IsShield],[IsItem]");
		Lib.add(t);

		t = Lib.extend("warlock", "base bandit");
		stats(t, 15, 18, 12, 18, 7, 13, 5, 9);
		t.set("Image", 121);
		t.set("LevelMin", 13);
		t.set("Frequency", 20);
		t.set(Skill.CASTING, 2);
		t.set("DefaultThings",
				"50%[IsStaff],10%[IsSpellBook],10%[IsWand],100%[IsSpell],100%[IsSpell]");
		Lib.add(t);
	}

	static void initChaosForces() {
		Thing t;
		t = Lib.extend("base chaotic human", "base humanoid");
		stats(t, 10, 8, 9, 9, 7, 8, 6, 7);
		t.set("IsChaotic", 1);
		t.set("LevelMin", 10);
		t.set("Image", 51);
		t.set("ASCII", "h");
		Lib.add(t);

		t = Lib.extend("chaos cultist", "base chaotic human");
		stats(t, 10, 12, 9, 14, 7, 18, 7, 9);
		t.set(Skill.UNARMED, 1);
		t.set(Skill.ATTACK, 1);
		t.set(Skill.DEFENCE, 1);
		t.set("Image", 51);
		t.set("LevelMin", 15);
		t.set("DefaultThings", "[IsWeapon],30% [IsArmour]");
		Lib.add(t);

		t = Lib.extend("chaos warrior", "base chaotic human");
		stats(t, 20, 22, 19, 34, 17, 48, 17, 19);
		t.set("ARM", 25);
		t.set(Skill.UNARMED, 3);
		t.set(Skill.ATTACK, 3);
		t.set(Skill.DEFENCE, 2);
		t.set("Image", 52);
		t.set("LevelMin", 18);
		t.set("DefaultThings", "[IsWeapon],[IsArmour]");
		Lib.add(t);

		t = Lib.extend("chaos knight", "chaos warrior");
		t.set("Image", 52);
		t.set("LevelMin", 24);
		Monster.strengthen(t, 3.0);
		t.set("DefaultThings", "[IsWeapon],[IsArmour],[IsItem]");
		Lib.add(t);

		t = Lib.extend("chaos champion", "chaos warrior");
		t.set("Image", 53);
		t.set("LevelMin", 29);
		Monster.strengthen(t, 10.0);
		Lib.add(t);

		t = Lib.extend("chaos hero", "chaos warrior");
		t.set("Image", 53);
		t.set("LevelMin", 34);
		Monster.strengthen(t, 20.0);
		Lib.add(t);
	}

	static void initVoidlings() {
		Thing t;

		t = Lib.extend("base voidling", "base monster");
		t.set("IsLiving", 0);
		t.set("IsFlying", 1);
		t.set("IsVoidling", 1);
		t.set("ARM", 100);
		t.set("RES:fire", 100);
		t.set("RES:ice", 100);
		t.set("RES:acid", 100);
		t.set("RES:disintegrate", 1000);
		t.set("RES:shock", 100);
		t.set("Image", 406);
		t.set("IsViewBlocking", 1);
		t.set("ASCII", "V");
		Lib.add(t);

		t = Lib.extend("voidling", "base voidling");
		t.set("LevelMin", 38);
		stats(t, 300, 300, 500, 200, 300, 500, 0, 300);
		t.set("MoveSpeed", 300);
		t.set("AttackSpeed", 300);
		t.set("UnarmedWeapon", Lib.create("disintegrate attack"));
		t.set("DeathDecoration", "Void");
		Lib.add(t);

		t = Lib.extend("greater voidling", "voidling");
		t.set("LevelMin", 40);
		strengthen(t, 2);
		Lib.add(t);

		t = Lib.extend("master voidling", "voidling");
		t.set("LevelMin", 42);
		stats(t, 300, 300, 500, 200, 300, 500, 0, 300);
		strengthen(t, 3);
		t.addHandler("OnAction", Scripts.generator("voidling", 100));
		Lib.add(t);
	}

	static void initVortices() {
		/*
		 * Vortices are magical contructs of writhing energy aminated by a
		 * malevolent spirit, they will fly swiftly towards a foe and explode on
		 * contact
		 */
		Thing t;

		t = Lib.extend("base vortex", "base monster");
		t.set("Image", 600);
		t.set("IsVortex", 1);
		t.set("IsLiving", 0);
		t.set("IsHumanoid", 0);
		t.set("IsFlying", 1);
		t.set("LevelMin", 15);
		t.set("RES:impact", 1000);
		t.set("RES:piercing", 1000);
		t.set("RES:fire", 1000);
		t.set("RES:ice", 1000);
		t.set("RES:chill", 1000);
		t.set("RES:shock", 1000);
		t.set("RES:drain", -20);
		stats(t, 30, 30, 40, 20, 0, 80, 0, 0);
		t.set("OnAttack", Scripts.returnTrue(Scripts.die()));
		Lib.add(t);

		t = Lib.extend("fire vortex", "base vortex");
		t.set("Image", 600);
		t.set("DeathDecoration", "Blaze");
		t.set("LevelMin", 15);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -40);
		Lib.add(t);

		t = Lib.extend("ice vortex", "base vortex");
		t.set("Image", 602);
		t.set("DeathDecoration", "Ice Blast");
		t.set("LevelMin", 17);
		t.set("RES:ice", 1000);
		t.set("RES:fire", -40);
		Lib.add(t);

		t = Lib.extend("poison vortex", "base vortex");
		t.set("Image", 607);
		t.set("DeathDecoration", "Poison Cloud");
		t.set("LevelMin", 12);
		t.set("RES:fire", -40);
		Lib.add(t);

		t = Lib.extend("electric vortex", "base vortex");
		t.set("Image", 603);
		t.set("DeathDecoration", "Sheet Lightning");
		t.set("LevelMin", 22);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -40);
		Lib.add(t);

	}

	static void initBigNasties() {
		Thing t;

		t = Lib.extend("base big monster", "base monster");
		t.set("ASCII", "M");
		t.set("IsBeast", 1);
		Lib.add(t);

		// horrendous gobbler by Catherine Boyce
		t = Lib.extend("horrendous gobbler", "base big monster");
		stats(t, 130, 160, 70, 200, 5, 240, 3, 10);
		t.set("UnarmedWeapon", Lib.create("strong poison attack"));
		t.set("MoveSpeed", 200);
		t.set("AttackSpeed", 300);
		t.set("DeathDecoration", "blood pool,bone");
		t.addHandler("OnAction", Scripts.generator("fly swarm", 100));
		t.addHandler("OnAction",
				breathAttack("poison", "poisonous fumes", 2, 60, 41));
		t.set("RES:poison", 1000);
		t.set("ARM", 160);
		t.set("FearFactor", 6);
		t.set("LevelMin", 28);
		t.set("Image", 424);
		Lib.add(t);

		t = Lib.extend("purple horror", "base big monster");
		stats(t, 40, 60, 50, 50, 15, 50, 3, 10);
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		t.set("MoveSpeed", 100);
		t.set("AttackSpeed", 100);
		t.set("DeathDecoration", "blood pool,bone");
		t.addHandler("OnAction", Scripts.generator("purple horror", 30));
		t.set("RES:poison", 100);
		t.set("ARM", 30);
		t.set("FearFactor", 4);
		t.set("LevelMin", 22);
		t.set("Image", 423);
		Lib.add(t);

		t = Lib.extend("gryphon", "base big monster");
		stats(t, 120, 60, 150, 70, 15, 90, 10, 5);
		t.set("UnarmedWeapon", Lib.create("claw attack"));
		t.set("MoveSpeed", 300);
		t.set("AttackSpeed", 300);
		t.set("DeathDecoration", "blood pool,bone");
		t.set("ARM", 20);
		t.set("FearFactor", 2);
		t.set("LevelMin", 23);
		t.set("Image", 361);
		Lib.add(t);

		t = Lib.extend("crocodile", "base big monster");
		stats(t, 40, 90, 20, 90, 2, 30, 1, 3);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		t.set("MoveSpeed", 80);
		t.set("AttackSpeed", 100);
		t.set("DeathDecoration", "blood pool,bone");
		t.set("ARM", 10);
		t.set("LevelMin", 18);
		t.set("Image", 367);
		Lib.add(t);

		t = Lib.extend("hydra", "base big monster");
		stats(t, 70, 90, 40, 130, 12, 70, 1, 33);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		t.set("MoveSpeed", 90);
		t.set("AttackSpeed", 400);
		t.set("DeathDecoration", "blood pool,bone");
		t.set("ARM", 60);
		t.set("LevelMin", 24);
		t.set("Image", 362);
		Lib.add(t);
	}

	static void initEyes() {
		Thing t;

		t = Lib.extend("base eye", "base monster");
		t.set("IsEye", 1);
		t.set("IsLiving", 1);
		t.set("IsFlying", 1);
		t.set("MoveSpeed", 70);
		t.set("Frequency", 30);
		t.set("RES:piercing", -13);
		t.incStat(Skill.MAGICRESISTANCE, 3);
		t.set("ASCII", "e");
		t.set("UnarmedWeapon", Lib.create("drain attack"));
		Lib.add(t);

		t = Lib.extend("floating eye", "base eye");
		Monster.stats(t, 10, 20, 15, 20, 40, 40, 2, 10);
		t.set("Image", 373);
		t.set("LevelMin", 10);
		Lib.add(t);

		t = Lib.extend("magic eye", "base eye");
		Monster.stats(t, 12, 20, 15, 20, 40, 40, 2, 10);
		t.set("Image", 373);
		t.set("LevelMin", 15);
		t.set(Skill.CASTING, 3);
		t.set(Skill.FOCUS, 1);
		t.set("DefaultThings", "[IsSpell],[IsSpell],[IsSpell],[IsSpell]");
		Lib.add(t);

		t = Lib.extend("malevolent eye", "base eye");
		Monster.stats(t, 22, 33, 25, 30, 50, 60, 6, 20);
		t.set("ARM", 20);
		t.set("Image", 372);
		t.set("LevelMin", 20);
		t.set("AttackSpeed", 150);
		t.set(Skill.DEFENCE, 1);
		t.set(Skill.CASTING, 3);
		t.set(Skill.FOCUS, 2);
		t.set("DefaultThings", "[IsSpell],[IsSpell],[IsSpell],[IsSpell]");
		Lib.add(t);

		t = Lib.extend("beholder", "base eye");
		Monster.stats(t, 32, 53, 35, 70, 80, 160, 16, 30);
		t.set("ARM", 50);
		t.set("Image", 368);
		t.set("LevelMin", 25);
		t.set("AttackSpeed", 150);
		t.set(Skill.DEFENCE, 1);
		t.set(Skill.CASTING, 4);
		t.set(Skill.FOCUS, 4);
		t.set("DefaultThings",
				"[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell]");
		Lib.add(t);
	}

	static void initElementals() {
		Thing t;

		t = Lib.extend("base elemental", "base monster");
		t.set("IsElemental", 1);
		t.set("IsLiving", 0);
		t.set("Frequency", 20);
		t.set(Skill.CASTING, 1);
		t.set("ASCII", "E");
		Lib.add(t);

		t = Lib.extend("ice elemental", "base elemental");
		stats(t, 40, 60, 50, 60, 15, 50, 4, 15);
		t.set("UnarmedWeapon", Lib.create("ice attack"));
		t.set("MoveSpeed", 150);
		t.set("AttackSpeed", 150);
		t.set("DeathDecoration", "water pool");
		t.set("RES:ice", 100);
		t.set("RES:fire", -15);
		t.set("ARM", 20);
		t.set("LevelMin", 24);
		t.set("Image", 425);
		t.set("DefaultThings", "Ice Blast");
		Lib.add(t);

		t = Lib.extend("fire elemental", "base elemental");
		stats(t, 60, 60, 60, 60, 15, 60, 3, 10);
		t.set("UnarmedWeapon", Lib.create("fire attack"));
		t.set("MoveSpeed", 150);
		t.set("AttackSpeed", 150);
		t.set("DeathDecoration", "medium fire");
		t.set("RES:impact", 100);
		t.set("RES:piercing", 100);
		t.set("RES:ice", -15);
		t.set("RES:fire", 100);
		t.set("ARM", 20);
		t.set("LevelMin", 22);
		t.set("Image", 407);
		t.set("DefaultThings", "Blaze");
		Lib.add(t);

		t = Lib.extend("earth elemental", "base elemental");
		stats(t, 50, 70, 20, 100, 15, 60, 3, 10);
		t.set("UnarmedWeapon", Lib.create("bash attack"));
		t.set("MoveSpeed", 100);
		t.set("AttackSpeed", 100);
		t.set("DeathDecoration", "rock");
		t.set("ARM", 60);
		t.set("LevelMin", 21);
		t.set("Image", 405);
		t.set("DefaultThings", "Blast");
		Lib.add(t);

		t = Lib.extend("air elemental", "base elemental");
		stats(t, 60, 60, 130, 40, 25, 70, 3, 10);
		t.set("MoveSpeed", 300);
		t.set("AttackSpeed", 200);
		t.set("ARM", 10);
		t.set("RES:impact", 100);
		t.set("RES:piercing", 100);
		t.set("RES:shock", 100);
		t.set("LevelMin", 23);
		t.set("Image", 385);
		t.set("IsFlying", 1);
		t.set("DefaultThings", "Thunderbolt");
		Lib.add(t);
	}

	static void initUndead() {
		final Thing t = Lib.extend("base undead", "base monster");
		t.set("IsUndead", 1);
		t.set("IsLiving", 0);
		t.set("Image", 302);
		t.set("DeathDecoration", null);
		t.set(Skill.UNARMED, 1);
		t.set("ASCII", "z");
		Lib.add(t);

		initGhosts();
		initZombies();
		initSkeletons();
	}

	static void initSkeletons() {
		Thing t;
		t = Lib.extend("base skeleton", "base undead");
		t.set("Image", 305);
		t.set("RES:piercing", 10);
		t.set("RES:fire", -5);
		t.set("ARM", 2);
		t.set("FearFactor", 1);
		t.set("IsRegenerating", 0);
		t.set("DeathDecoration", "50% bone");
		t.set("MoveSpeed", 100);
		t.set("IsSkeleton", 1);
		t.set("ASCII", "S");
		Lib.add(t);

		t = Lib.extend("lesser skeleton", "base skeleton");
		Monster.stats(t, 6, 8, 3, 8, 0, 9, 0, 2);
		t.set("LevelMin", 8);
		Lib.add(t);

		t = Lib.extend("skeleton", "base skeleton");
		Monster.stats(t, 10, 12, 8, 12, 0, 13, 0, 5);
		t.set("LevelMin", 11);
		t.set("DefaultThings", "30% [IsWeapon]");
		Lib.add(t);

		t = Lib.extend("haunted skeleton", "base skeleton");
		Monster.stats(t, 12, 12, 6, 10, 0, 13, 0, 5);
		t.set("LevelMin", 12);
		t.set("DeathDecoration", "haunted skull");
		Lib.add(t);

		t = Lib.extend("large skeleton", "skeleton");
		Monster.stats(t, 14, 18, 7, 18, 0, 19, 0, 7);
		t.set("ARM", 3);
		t.set("LevelMin", 14);
		Lib.add(t);

		t = Lib.extend("fearsome skeleton", "skeleton");
		Monster.stats(t, 16, 18, 10, 18, 0, 23, 0, 7);
		t.set("FearFactor", 2);
		t.set("ARM", 3);
		t.set("LevelMin", 15);
		Lib.add(t);

		t = Lib.extend("skeleton warrior", "skeleton");
		Monster.stats(t, 18, 18, 16, 18, 0, 19, 0, 17);
		t.set("Image", 306);
		t.set("FearFactor", 2);
		t.set("ARM", 6);
		t.set("LevelMin", 16);
		t.set("DefaultThings", "[IsWeapon],50% [IsArmour],30% [IsRangedWeapon]");
		Lib.add(t);

		t = Lib.extend("skeleton hero", "skeleton");
		Monster.stats(t, 78, 58, 56, 78, 0, 99, 0, 27);
		t.set("Image", 306);
		t.set("FearFactor", 3);
		t.set("ARM", 13);
		t.set("LevelMin", 24);
		t.set("MoveSpeed", 150);
		t.set("AttackSpeed", 200);
		t.set("DefaultThings",
				"[IsWeapon],[IsArmour],[IsThrowingWeapon],[IsItem]");
		Lib.add(t);

		t = Lib.extend("skeleton lord", "skeleton");
		Monster.stats(t, 138, 88, 76, 178, 0, 199, 0, 50);
		t.set("Image", 306);
		t.set("FearFactor", 4);
		t.set("ARM", 20);
		t.set("LevelMin", 29);
		t.set("MoveSpeed", 200);
		t.set("AttackSpeed", 250);
		t.set("DefaultThings",
				"[IsWeapon],[IsArmour],[IsThrowingWeapon],[IsItem]");
		Lib.add(t);

		t = Lib.extend("skeletal dragon", "skeleton");
		Monster.stats(t, 78, 98, 76, 178, 0, 199, 0, 57);
		t.set("Image", 643);
		t.set("FearFactor", 4);
		t.set("ARM", 100);
		t.set("LevelMin", 28);
		t.set("MoveSpeed", 250);
		t.set("AttackSpeed", 300);
		t.set("IsFlying", 1);
		t.set("DeathDecoration", "70% haunted dragon skull");

		Lib.add(t);
	}

	static void initImps() {
		Thing t;

		t = Lib.extend("base imp", "base demon");
		t.set("IsImp", 1);
		t.set("ARM", 2);
		Lib.add(t);

		t = Lib.extend("blue imp", "base imp");
		Monster.stats(t, 6, 10, 5, 3, 6, 4, 7, 2);
		t.set("Image", 347);
		t.set("LevelMin", 7);
		t.set("DeathDecoration", "Frost Bolt");
		Lib.add(t);

		t = Lib.extend("frost imp", "base imp");
		Monster.stats(t, 4, 4, 5, 3, 5, 5, 7, 2);
		t.set("Image", 347);
		t.set("LevelMin", 8);
		t.set(Skill.CASTING, 1);
		t.set("DefaultThings", "Frost Bolt");
		t.set("ARM", 3);
		t.set("DeathDecoration", "Frost Bolt");
		Lib.add(t);

		t = Lib.extend("fire imp", "base imp");
		Monster.stats(t, 7, 7, 3, 3, 7, 8, 7, 2);
		t.set("Image", 220);
		t.set("IsFlying", 1);
		t.set("LevelMin", 9);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -30);
		t.set(Skill.CASTING, 1);
		t.set("DefaultThings", "Flame");
		t.set("ARM", 4);
		t.set("DeathDecoration", "Flame");
		Lib.add(t);

		t = Lib.extend("greater blue imp", "blue imp");
		Monster.stats(t, 16, 16, 15, 16, 6, 14, 7, 2);
		t.set("Image", 347);
		t.set("LevelMin", 11);
		t.set("DeathDecoration", "Frost Bolt,[IsRunestone]");
		Lib.add(t);

		t = Lib.extend("greater fire imp", "fire imp");
		Monster.stats(t, 17, 17, 13, 13, 17, 18, 17, 12);
		t.set("Image", 220);
		t.set("IsFlying", 1);
		t.set("LevelMin", 12);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -30);
		t.set(Skill.CASTING, 2);
		t.set(Skill.FOCUS, 2);
		t.set("DefaultThings", "Flame, Fireball");
		t.set("ARM", 8);
		t.set("DeathDecoration", "Fireball,[IsRunestone]");
		Lib.add(t);
	}

	static void initDemons() {
		Thing t;

		t = Lib.extend("base demon", "base monster");
		t.set("IsLiving", 0);
		t.set("IsDemonic", 1);
		t.set("RES:fire", 10);
		t.set("RES:acid", 10);
		t.set("ASCII", "D");
		t.multiplyStat(RPG.ST_REGENERATE, 3);
		t.set("UnarmedWeapon", Lib.create("claw attack"));
		Lib.add(t);

		t = Lib.extend("lesser demon", "base demon");
		Monster.stats(t, 20, 20, 20, 20, 20, 20, 20, 20);
		t.set(Skill.UNARMED, 2);
		t.set(Skill.DEFENCE, 2);
		t.set(Skill.ATTACK, 2);
		t.set("Image", 420);
		t.set("LevelMin", 14);
		Lib.add(t);

		t = Lib.extend("dark angel", "lesser demon");
		Monster.stats(t, 30, 20, 40, 20, 40, 30, 60, 20);
		t.set("Image", 400);
		t.set("IsFlying", 1);
		t.set("LevelMin", 16);
		t.set("ARM", 10);
		Lib.add(t);

		t = Lib.extend("pit beast", "lesser demon");
		Monster.stats(t, 40, 40, 50, 40, 10, 60, 10, 20);
		t.set("Image", 420);
		t.set("LevelMin", 21);
		t.set("ARM", 18);
		t.set("AttackSpeed", 200);
		Lib.add(t);

		t = Lib.extend("bile demon", "lesser demon");
		Monster.stats(t, 40, 40, 30, 70, 50, 60, 1, 40);
		t.set("Image", 545);
		t.set("LevelMin", 24);
		t.set("ARM", 60);
		t.set("AttackSpeed", 150);
		t.set(Skill.CASTING, 2);
		t.set(Skill.FOCUS, 1);
		t.set("UnarmedWeapon", Lib.create("strong poison attack"));
		t.set("DefaultThings", "Aruk's Poison Cloud,[IsSpell]");
		Lib.add(t);

		t = Lib.extend("wasp demon", "lesser demon");
		Monster.stats(t, 60, 40, 70, 40, 10, 70, 5, 30);
		t.set("Image", 548);
		t.set("LevelMin", 25);
		t.set("ARM", 20);
		t.set("MoveSpeed", 200);
		t.set("AttackSpeed", 200);
		t.set("UnarmedWeapon", Lib.create("strong poison attack"));
		Lib.add(t);

		t = Lib.extend("soul eater", "lesser demon");
		Monster.stats(t, 70, 80, 80, 140, 160, 190, 100, 140);
		t.set("Image", 543);
		t.set("LevelMin", 27);
		t.set("ARM", 60);
		t.set("AttackSpeed", 150);
		t.set("MoveSpeed", 70);
		t.set(Skill.CASTING, 2);
		t.set(Skill.FOCUS, 3);
		t.set("Luck", 60);
		t.set("UnarmedWeapon", Lib.create("drain attack"));
		t.set("DefaultThings", "Thunderbolt,Blaze,[IsSpell],[IsSpell]");
		Lib.add(t);

		t = Lib.extend("greater demon", "lesser demon");
		Monster.stats(t, 150, 100, 100, 150, 100, 200, 50, 100);
		t.set("ARM", 70);
		t.set("AttackSpeed", 200);
		t.set("MoveSpeed", 200);
		t.set("IsFlying", 1);
		t.set(Skill.CASTING, 2);
		t.set("Image", 541);
		t.set("Luck", 130);
		t.set("LevelMin", 30);
		Lib.add(t);

		t = Lib.extend("bilious demon", "greater demon");
		Monster.stats(t, 150, 100, 100, 150, 100, 200, 50, 100);
		t.set("ARM", 40);
		t.set("AttackSpeed", 150);
		t.set("MoveSpeed", 150);
		t.set("IsFlying", 0);
		t.set(Skill.CASTING, 4);
		t.set("Image", 542);
		t.set("Luck", 140);
		t.set("DefaultThings", "Thunderbolt,Blaze,[IsSpell],[IsSpell]");
		t.set("LevelMin", 36);
		Lib.add(t);

		t = Lib.extend("greater fire demon", "greater demon");
		Monster.stats(t, 150, 150, 150, 200, 100, 250, 50, 50);
		t.set("ARM", 100);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -20);
		t.set("RES:water", -20);
		t.set("Image", 544);
		t.set("LevelMin", 33);
		t.addHandler("OnAction",
				breathAttack("fire", "blazing flames", 3, 40, 1));
		Lib.add(t);

		t = Lib.extend("greater frost demon", "greater demon");
		Monster.stats(t, 160, 120, 150, 150, 150, 250, 50, 50);
		t.set("ARM", 60);
		t.set("RES:fire", -10);
		t.set("RES:ice", 1000);
		t.set("RES:water", 20);
		t.set("Image", 546);
		t.set("LevelMin", 34);
		t.set("UnarmedWeapon", Lib.create("ice attack"));
		t.addHandler("OnAction",
				breathAttack("ice", "a blast of ice", 3, 40, 101));
		Lib.add(t);

		t = Lib.extend("baal-rukh", "greater demon");
		Monster.stats(t, 400, 500, 500, 400, 200, 600, 100, 200);
		t.set("Image", 540);
		t.set("ARM", 200);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -20);
		t.set("RES:water", -20);
		t.set("AttackSpeed", 300);
		t.set("MoveSpeed", 400);
		t.set("IsFlying", 1);
		t.set(Skill.CASTING, 5);
		t.set("UnarmedWeapon", Lib.create("claw attack"));
		t.set("DefaultThings", "[IsSword],Firepath,Blaze,[IsSpell]");
		t.set("Luck", 130);
		t.set("LevelMin", 39);
		Lib.add(t);
	}

	static void initZombies() {
		Thing t;
		t = Lib.extend("base zombie", "base undead");
		t.set("Image", 300);
		t.set("RES:piercing", 7);
		t.set("RES:fire", -10);
		t.set("ARM", 1);
		t.set("FearFactor", 1);
		t.set("IsRegenerating", 0);
		t.set("DeathDecoration", "bone");
		t.set("MoveSpeed", 65);
		t.set("ASCII", "z");
		Lib.add(t);

		t = Lib.extend("lesser zombie", "base zombie");
		Monster.stats(t, 3, 7, 4, 3, 0, 5, 0, 0);
		t.set("LevelMin", 5);
		Lib.add(t);

		t = Lib.extend("zombie", "base zombie");
		Monster.stats(t, 9, 17, 6, 7, 0, 14, 0, 0);
		t.set("LevelMin", 8);
		Lib.add(t);

		t = Lib.extend("fearsome zombie", "base zombie");
		Monster.stats(t, 9, 20, 6, 16, 0, 14, 0, 0);
		t.set("LevelMin", 9);
		t.set("FearFactor", 2);
		t.set("AttackSpeed", 150);
		Lib.add(t);

		t = Lib.extend("large zombie", "base zombie");
		Monster.stats(t, 12, 26, 8, 36, 0, 20, 0, 0);
		t.set("LevelMin", 10);
		t.set("Image", 301);
		Lib.add(t);

		t = Lib.extend("horrendous zombie", "base zombie");
		Monster.stats(t, 12, 20, 6, 20, 0, 20, 0, 0);
		t.set("LevelMin", 11);
		t.set("FearFactor", 3);
		t.set("Image", 301);
		Lib.add(t);
	}

	static void initGhosts() {
		Thing t;
		t = Lib.extend("base ghost", "base undead");
		t.set("IsUndead", 1);
		t.set("IsEthereal", 1);
		t.set("IsFlying", 1);
		t.set("IsInhabitant", 1); // probably an old relative!
		t.set("IsBlocking", 1);
		t.set("IsGhost", 1);
		t.set("FearFactor", 2);
		t.set("Image", 310);
		t.set("RES:ice", 20);
		t.set("RES:acid", 20);
		t.set("MoveSpeed", 80); // bit slow
		t.set("UnarmedWeapon", Lib.create("chill attack"));
		t.set("ASCII", "G");
		Lib.add(t);

		t = Lib.extend("ghost", "base ghost");
		stats(t, 30, 30, 40, 10, 20, 20, 0, 0);
		t.set("Image", 310);
		t.set("LevelMin", 17);
		t.set("OnTouch", Scripts.damage("chill", 3,
				"chilled by the touch of the ghost", 50));
		Lib.add(t);

		t = Lib.extend("holy ghost", "ghost");
		stats(t, 40, 30, 40, 30, 20, 20, 0, 0);
		t.set("Image", 310);
		t.set("LevelMin", 22);
		t.addHandler("OnAction", Scripts.generator("ghost", 20));
		Lib.add(t);

		t = Lib.extend("spectre", "base ghost");
		stats(t, 80, 80, 60, 50, 60, 120, 0, 0);
		t.set("Image", 315);
		t.set("LevelMin", 24);
		t.set("AttackSpeed", 150);
		t.set("FearFactor", 5);
		t.set("IsViewBlocking", 1);
		t.addHandler("OnAction", Scripts.generator("spectre", 40));
		t.set("OnTouch", Scripts.damage("chill", 10,
				"chilled by the touch of the spectre", 50));
		Lib.add(t);

		t = Lib.extend("demon spectre", "spectre");
		stats(t, 100, 100, 80, 90, 60, 150, 0, 0);
		t.set("Image", 315);
		t.set("LevelMin", 30);
		t.set("AttackSpeed", 200);
		t.set("FearFactor", 6);
		t.addHandler("OnAction", Scripts.generator("spectre", 100));
		Lib.add(t);

		t = Lib.extend("spectre lord", "demon spectre");
		stats(t, 130, 160, 80, 170, 90, 200, 0, 0);
		t.set("Image", 315);
		t.set("LevelMin", 34);
		t.set("AttackSpeed", 300);
		t.set("FearFactor", 7);
		Lib.add(t);

	}

	static void initGoblinoids() {
		Thing t;

		t = Lib.extend("base goblinoid", "base humanoid");
		t.set("IsGoblinoid", 1);
		t.set("Image", 240);
		t.set("ARM", 1);
		t.set(Skill.UNARMED, 1);
		t.set("OnChat", new Personality(Personality.CHATTER,
				Personality.CHATTER_GOBLIN));
		Lib.add(t);

		t = Lib.extend("goblin", "base goblinoid");
		stats(t, 10, 8, 12, 7, 6, 5, 5, 5);
		t.set("DeathDecoration", "slime pool, 20% gold coin");
		t.set("LevelMin", 4);
		t.set("LevelMax", 15);
		t.set("Image", 244);
		t.set("IsGoblin", 1);
		t.set("DefaultThings", "30%[IsWeapon],15%[IsFood]");
		Lib.add(t);

		t = Lib.extend("weedy goblin", "base goblinoid");
		stats(t, 5, 3, 12, 3, 5, 2, 5, 5);
		t.set("DeathDecoration", "slime pool");
		t.set("LevelMin", 2);
		t.set("LevelMax", 8);
		t.set("Image", 244);
		t.set("DefaultThings", "10%[IsWeapon]");
		Lib.add(t);

		t = Lib.extend("small goblin", "base goblinoid");
		stats(t, 6, 6, 10, 4, 5, 2, 5, 5);
		t.set("DeathDecoration", "slime pool");
		t.set("LevelMin", 3);
		t.set("LevelMax", 12);
		t.set("Image", 244);
		t.set("DefaultThings", "20%[IsWeapon]");
		Lib.add(t);

		t = Lib.extend("goblin rockthrower", "base goblinoid");
		stats(t, 6, 6, 10, 4, 5, 2, 5, 5);
		t.set("DeathDecoration", "slime pool");
		t.set("LevelMin", 4);
		t.set("Image", 244);
		t.set("RetreatChance", 50);
		t.set(Skill.THROWING, RPG.d(2));
		t.set("DefaultThings", "10 [IsRock]");
		Lib.add(t);

		t = Lib.extend("goblin slinger", "goblin");
		stats(t, 10, 10, 6, 10, 4, 5, 4, 5);
		t.set("Image", 244);
		t.set("LevelMin", 5);
		t.set("RetreatChance", 40);
		t.set(Skill.ARCHERY, RPG.d(2));
		t.set("DefaultThings", "[IsSling], 20 stone");
		Lib.add(t);

		t = Lib.extend("goblin archer", "goblin");
		stats(t, 10, 10, 6, 13, 4, 9, 4, 7);
		t.set("Image", 248);
		t.set("LevelMin", 7);
		t.set("RetreatChance", 60);
		t.set(Skill.ARCHERY, RPG.d(2));
		t.set("DefaultThings",
				"100%[IsBow], 100% 6 [IsArrow],60% 6 goblin arrow");
		Lib.add(t);

		t = Lib.extend("big goblin", "goblin");
		stats(t, 10, 10, 6, 13, 4, 9, 4, 7);
		t.set("LevelMin", 6);
		t.set("Image", 241);
		t.set(Skill.UNARMED, 2);
		t.set("ARM", 3);
		Lib.add(t);

		t = Lib.extend("goblin warrior", "big goblin");
		stats(t, 13, 10, 11, 9, 6, 8, 5, 6);
		t.set("Image", 240);
		t.set("DefaultThings", "30% [IsArmour], 30%[IsWeapon]");
		t.set("LevelMin", 8);
		t.set("ARM", 6);
		Lib.add(t);

		t = Lib.extend("goblin leader", "goblin warrior");
		stats(t, 23, 12, 19, 17, 6, 8, 5, 6);
		t.set("Image", 243);
		t.set("DefaultThings", "50% [IsArmour], [IsWeapon]");
		t.set("LevelMin", 10);
		t.set(Skill.DEFENCE, 2);
		t.incStat(RPG.ST_SKILLPOINTS, 2);
		t.set("ARM", 10);
		Lib.add(t);

		t = Lib.extend("goblin champion", "goblin leader");
		stats(t, 33, 22, 39, 27, 16, 18, 11, 16);
		t.set(Skill.ATTACK, 1);
		t.set("ARM", 15);
		t.set("LevelMin", 14);
		t.incStat("Luck", 20);
		Lib.add(t);

		t = Lib.extend("goblin chieftain", "goblin champion");
		stats(t, 35, 32, 39, 47, 16, 28, 11, 16);
		t.set(Skill.ATTACK, 1);
		t.set(Skill.DEFENCE, 1);
		t.set("ARM", 18);
		t.set("LevelMin", 17);
		t.incStat("Luck", 30);
		Lib.add(t);

		t = Lib.extend("goblin hero", "goblin chieftain");
		stats(t, 53, 32, 69, 47, 36, 48, 21, 36);
		t.set("MoveSpeed", 160);
		t.set("AttackSpeed", 160);
		t.set(Skill.ATTACK, 2);
		t.incStat(RPG.ST_SKILLPOINTS, 4);
		t.set("DefaultThings",
				"[IsArmour], [IsWeapon], 20% [IsThrowingWeapon], 20% [IsRangedWeapon]");
		t.set("ARM", 20);
		t.set("LevelMin", 20);
		t.incStat("Luck", 40);
		Lib.add(t);

		t = Lib.extend("goblin war-boss", "goblin hero");
		Monster.strengthen(t, 2);
		t.set("LevelMin", 26);
		t.set("IsDisplaceable", 0);
		Lib.add(t);

		t = Lib.extend("flying goblin", "goblin");
		stats(t, 18, 10, 16, 19, 26, 28, 15, 16);
		t.set("Image", 247);
		t.set("MoveSpeed", 350);
		t.set("AttackSpeed", 200);
		t.set("IsFlying", 1);
		t.set("DefaultThings", "50% [IsArmour], 100%[IsWeapon]");
		t.set("Frequency", 10);
		t.set("LevelMin", 13);
		Lib.add(t);

		t = Lib.extend("goblin shaman", "goblin");
		stats(t, 16, 10, 16, 13, 26, 28, 15, 16);
		t.set("UnarmedWeapon", Lib.create("curse attack"));
		t.set(Skill.CASTING, 3);
		t.set(Skill.DEFENCE, 2);
		t.set(Skill.FOCUS, 2);
		t.set("DefaultThings",
				"[IsStaff],50% Fireball,50% Poison Cloud,50% Flame,[IsSpell],50% [IsArmour],[IsScroll]");
		t.set("Image", 245);
		t.set("LevelMin", 17);
		t.set("ARM", 15);
		t.incStat("Luck", 50);
		Lib.add(t);

		t = Lib.extend("orc", "big goblin");
		stats(t, 8, 10, 7, 11, 4, 9, 3, 3);
		t.set("ARM", 4);
		t.set("LevelMin", 8);
		t.set(Skill.ATTACK, 1);
		t.set("Image", 242);
		t.set("IsOrc", 1);
		t.set("OnChat", new Personality(Personality.CHATTER,
				Personality.CHATTER_ORC));
		Lib.add(t);

		t = Lib.extend("big orc", "orc");
		stats(t, 7, 14, 7, 16, 4, 9, 3, 3);
		t.set("ARM", 6);
		t.set("LevelMin", 10);
		t.set(Skill.UNARMED, 2);
		t.set(Skill.ATTACK, 1);
		t.set(Skill.DEFENCE, 2);
		t.set("Image", 242);
		Lib.add(t);

		t = Lib.extend("orc warrior", "orc");
		stats(t, 13, 18, 9, 18, 5, 18, 5, 6);
		t.set("ARM", 10);
		t.set(Skill.ATTACK, 2);
		t.set(Skill.DEFENCE, 1);
		t.set("Image", 246);
		t.set("DefaultThings", "50% [IsArmour], 100%[IsWeapon]");
		t.set("LevelMin", 13);
		Lib.add(t);

		t = Lib.extend("orc champion", "orc");
		Monster.strengthen(t, 2.5);
		t.set(Skill.UNARMED, 3);
		t.set(Skill.ATTACK, 2);
		t.set(Skill.DEFENCE, 3);
		t.set("AttackSpeed", 160);
		t.set("Image", 246);
		t.set("DefaultThings", "[IsArmour],[IsWeapon]");
		t.set("LevelMin", 16);
		t.incStat("Luck", 20);
		Lib.add(t);

		t = Lib.extend("orc hero", "orc");
		Monster.strengthen(t, 6.0);
		t.set(Skill.UNARMED, 4);
		t.set(Skill.ATTACK, 4);
		t.set(Skill.DEFENCE, 5);
		t.set("AttackSpeed", 200);
		t.set("MoveSpeed", 130);
		t.set("Image", 246);
		t.incStat(RPG.ST_SKILLPOINTS, 4);
		t.set("DefaultThings", "[IsArmour],[IsWeapon], 50% [IsThrowingWeapon]");
		t.set("LevelMin", 22);
		t.incStat("Luck", 40);
		Lib.add(t);

		t = Lib.extend("orc warlord", "orc hero");
		Monster.strengthen(t, 2.0);
		t.set(Skill.UNARMED, 6);
		t.set(Skill.ATTACK, 6);
		t.set(Skill.DEFENCE, 8);
		t.set("AttackSpeed", 300);
		t.set("MoveSpeed", 150);
		t.set("Image", 246);
		t.set("DefaultThings",
				"[IsArmour],[IsArmour],[IsWeapon],[IsThrowingWeapon],[IsItem]");
		t.set("LevelMin", 28);
		t.incStat("Luck", 60);
		Lib.add(t);
	}

	static void initInsects() {
		// insects are small and DON'T block

		Thing t;
		t = Lib.extend("base insect", "base monster");
		t.set("DeathDecoration", "slime pool");
		t.set("IsInsect", 1);
		t.set("IsBeast", 1);
		t.set("IsBlocking", 1);
		t.set("Frequency", 40);
		t.set("RES:poison", 10);
		t.set("LevelMin", 3);
		t.incStat(Skill.DODGE, 1); // dodgy little pests!
		Lib.add(t);

		// simple insect
		t = Lib.extend("insect", "base insect");
		stats(t, 2, 2, 3, 1, 1, 1, 1, 1);
		t.set("Image", 161);
		t.set("LevelMin", 1);
		Lib.add(t);

		// bugs
		t = Lib.extend("small yellow bug", "insect");
		stats(t, 3, 3, 1, 2, 1, 1, 1, 2);
		t.set("Image", 162);
		t.set("LevelMin", 1);
		t.set("DeathDecoration", "20% squished bug");
		Lib.add(t);

		t = Lib.extend("cockroach", "insect");
		stats(t, 2, 2, 5, 2, 1, 2, 0, 1);
		t.set("Image", 161);
		t.set("LevelMin", 2);
		t.set("DeathDecoration", "30% squished roach");
		Lib.add(t);

		t = Lib.extend("beetle", "insect");
		stats(t, 5, 3, 5, 4, 2, 3, 1, 2);
		t.set("Image", 169);
		t.set("LevelMin", 3);
		t.set("DeathDecoration", "30% dead beetle");
		Lib.add(t);

		t = Lib.extend("bug", "insect");
		stats(t, 8, 8, 8, 8, 2, 9, 1, 2);
		t.set("Image", 161);
		t.set("ARM", 3);
		t.set("LevelMin", 4);
		t.set("DeathDecoration", "30% dead bug");
		Lib.add(t);

		t = Lib.extend("yellow bug", "insect");
		stats(t, 9, 8, 8, 8, 2, 9, 1, 2);
		t.set("Image", 162);
		t.set("ARM", 4);
		t.set("LevelMin", 5);
		t.set("DeathDecoration", "40% dead bug");
		Lib.add(t);

		t = Lib.extend("big bug", "bug");
		stats(t, 14, 12, 6, 13, 3, 19, 1, 3);
		t.set("Image", 181);
		t.set("ARM", 3);
		t.set("LevelMin", 6);
		t.set("DeathDecoration", "40% dead bug");
		Lib.add(t);

		t = Lib.extend("giant cockroach", "insect");
		stats(t, 13, 10, 18, 13, 2, 5, 0, 2);
		t.set("Image", 161);
		t.set(Skill.DEFENCE, 1);
		t.set("ARM", 5);
		t.set("LevelMin", 7);
		t.set("DeathDecoration", "40% dead roach");
		Lib.add(t);

		t = Lib.extend("ice bug", "bug");
		stats(t, 14, 13, 6, 12, 3, 19, 1, 3);
		t.set("Image", 181);
		t.set("LevelMin", 8);
		t.set("ARM", 2);
		t.set("UnarmedWeapon", Lib.create("ice attack"));
		// no corpse -- they melt
		Lib.add(t);

		t = Lib.extend("chaos beetle", "insect");
		stats(t, 13, 16, 18, 18, 2, 15, 0, 2);
		t.set("Image", 191);
		t.set(Skill.DEFENCE, 2);
		t.set(Skill.ATTACK, 2);
		t.set("ARM", 18);
		t.set("LevelMin", 12);
		t.set("DeathDecoration", "40% dead beetle");
		Lib.add(t);

		// scorpions
		// vulnerable but v. nasty attacks!
		t = Lib.extend("scorpion", "insect");
		stats(t, 40, 18, 20, 10, 2, 29, 1, 6);
		t.set("Image", 168);
		t.set("LevelMin", 10);
		t.set("ARM", 2);
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		t.set("DeathDecoration", "slime pool, scorpion tail, 20% dead scorpion");
		Lib.add(t);

		// insect swarms
		t = Lib.extend("base swarm", "base insect");
		stats(t, 1, 1, 1, 1, 1, 1, 1, 1);
		t.set("Image", 281);
		t.set("IsFlying", 1);
		t.set("LevelMin", 1);
		t.set("ARM", 0);
		t.set("IsConfused", 1);
		t.incStat(Skill.DODGE, 1);
		Lib.add(t);

		t = Lib.extend("fly swarm", "base swarm");
		stats(t, 6, 4, 7, 3, 1, 3, 1, 1);
		t.set("Image", 280);
		t.set("LevelMin", 3);
		t.set("DeathDecoration", "20* dead fly");
		Lib.add(t);

		t = Lib.extend("bee swarm", "base swarm");
		stats(t, 12, 6, 13, 5, 1, 3, 1, 1);
		t.set("Image", 281);
		t.set("LevelMin", 5);
		t.set("DeathDecoration", "30* dead bee");
		Lib.add(t);

		t = Lib.extend("wasp swarm", "base swarm");
		stats(t, 20, 8, 16, 10, 1, 3, 1, 1);
		t.set("Image", 282);
		t.set("ARM", 1);
		t.set("LevelMin", 7);
		t.set("DeathDecoration", "10* dead wasp");
		Lib.add(t);

		t = Lib.extend("hornet swarm", "base swarm");
		stats(t, 28, 18, 26, 20, 1, 3, 1, 1);
		t.set("Image", 282);
		t.set("ARM", 3);
		t.set("LevelMin", 11);
		t.set("DeathDecoration", "15* dead hornet");
		Lib.add(t);

		t = Lib.extend("fire wasp swarm", "base swarm");
		stats(t, 25, 8, 16, 15, 1, 3, 1, 1);
		t.set("Image", 289);
		t.set("LevelMin", 9);
		t.set("ARM", 2);
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		t.set("DeathDecoration", "20* dead fire wasp");
		Lib.add(t);
	}

	static void initBears() {
		Thing t;

		t = Lib.extend("base bear", "base monster");
		t.set("IsBear", 1);
		t.set("IsBeast", 1);
		t.set("MoveSpeed", 100);
		t.set("AttackSpeed", 150);
		t.set("Frequency", 10);
		t.set("Image", 201);
		t.set("ARM", 8);
		t.set("DeathDecoration", "blood pool, 30% bear paw");
		t.set("UnarmedWeapon", Lib.create("claw attack"));
		Lib.add(t);

		t = Lib.extend("brown bear", "base bear");
		Monster.stats(t, 20, 25, 16, 35, 3, 15, 4, 3);
		t.set("LevelMin", 12);
		t.set("Image", 201);
		Lib.add(t);

		t = Lib.extend("grizzly bear", "base bear");
		Monster.stats(t, 25, 35, 19, 45, 3, 25, 4, 3);
		t.set("LevelMin", 15);
		t.set("Image", 201);
		Lib.add(t);

		t = Lib.extend("black bear", "base bear");
		Monster.stats(t, 30, 50, 25, 65, 3, 35, 4, 3);
		t.set("LevelMin", 16);
		t.set("Image", 203);
		Lib.add(t);

		t = Lib.extend("polar bear", "base bear");
		Monster.stats(t, 35, 55, 29, 75, 3, 45, 4, 3);
		t.set("LevelMin", 18);
		t.set("Image", 202);
		Lib.add(t);

		t = Lib.extend("ice bear", "base bear");
		Monster.stats(t, 35, 55, 29, 75, 3, 55, 4, 3);
		t.set("LevelMin", 20);
		t.set("Image", 202);
		t.set("UnarmedWeapon", Lib.create("ice attack"));

		Lib.add(t);
	}

	static void initCats() {
		Thing t;

		t = Lib.extend("base feline", "base monster");
		t.set("IsFeline", 1);
		t.set("MoveSpeed", 120);
		t.set("Frequency", 10);
		t.set("Image", 213);
		t.set("DeathDecoration", "blood pool, 30% cat whisker");
		t.set("ASCII", "f");
		t.set("IsBeast", 1);
		Lib.add(t);

		t = Lib.extend("black cat", "base feline");
		Monster.stats(t, 10, 3, 16, 5, 3, 5, 4, 2);
		t.set("LevelMin", 3);
		t.set("Image", 213);
		Lib.add(t);

		t = Lib.extend("wildcat", "black cat");
		Monster.stats(t, 20, 13, 26, 20, 3, 13, 3, 3);
		t.set("LevelMin", 8);
		t.set("Image", 211);
		Lib.add(t);

		t = Lib.extend("cheetah", "wildcat");
		Monster.stats(t, 30, 23, 36, 20, 3, 13, 3, 3);
		t.set("LevelMin", 11);
		t.set("MoveSpeed", 200);
		t.set("AttackSpeed", 200);
		t.set("Image", 211);
		Lib.add(t);

		t = Lib.extend("cave lion", "wildcat");
		Monster.stats(t, 40, 40, 36, 40, 4, 23, 9, 2);
		t.set("LevelMin", 14);
		t.set("UnarmedWeapon", Lib.create("razor claw attack"));
		t.set("Image", 212);
		Lib.add(t);

		t = Lib.extend("witch's cat", "black cat");
		Monster.stats(t, 28, 13, 36, 15, 10, 19, 13, 7);
		t.set("LevelMin", 17);
		t.set("UnarmedWeapon", Lib.create("hex attack"));
		t.set("FatePoints", 9);
		t.set("Luck", 100);
		t.set("Image", 213);
		Lib.add(t);

	}

	static void initPlants() {
		Thing t = Lib.extend("base plant monster", "base monster");
		t.set("IsPlantMonster", 1);
		t.set("Image", 320);
		t.set("MoveSpeed", 30);
		t.set("RES:fire", -15);
		t.set("RES:impact", 12);
		t.set("RES:piercing", 12);
		t.set("RES:poison", 1000);
		t.set("ASCII", "t");
		Lib.add(t);

		t = Lib.extend("triffid", "base plant monster");
		t.set("Image", 320);
		Monster.stats(t, 10, 10, 10, 15, 0, 10, 0, 3);
		t.set("UnarmedWeapon", Lib.create("blind attack"));
		t.set("ARM", 3);
		t.set("LevelMin", 10);
		t.set("DeathDecoration", "slime pool");
		Lib.add(t);

		t = Lib.extend("seed triffid", "triffid");
		t.set("Image", 322);
		Monster.stats(t, 20, 10, 20, 30, 0, 30, 0, 6);
		t.set("UnarmedWeapon", Lib.create("blind attack"));
		t.addHandler("OnAction", Scripts.generator("triffid", 100));
		t.set("LevelMin", 14);
		Lib.add(t);

		t = Lib.extend("tree monster", "base plant monster");
		Monster.stats(t, 20, 40, 10, 80, 4, 40, 0, 2);
		t.set("ARM", 20);
		t.set("IsIntelligent", 1);
		t.set("Image", 321);
		t.set("LevelMin", 10);
		t.set("DeathDecoration", "wooden log");
		t.set("ASCII", "T");
		Lib.add(t);

		t = Lib.extend("tree hurler", "tree monster");
		Monster.stats(t, 30, 50, 10, 80, 4, 40, 0, 2);
		t.set("ARM", 30);
		t.set("Image", 321);
		t.set("LevelMin", 13);
		t.set("DefaultThings", "10 rock");
		t.set(Skill.THROWING, 3);
		t.set("DeathDecoration", "wooden log");
		Lib.add(t);

		t = Lib.extend("greater tree monster", "tree monster");
		Monster.stats(t, 40, 60, 12, 180, 19, 70, 6, 7);
		t.set("ARM", 50);
		t.set("Image", 321);
		t.set("LevelMin", 18);
		t.set("DeathDecoration", "wooden log");
		Lib.add(t);

		t = Lib.extend("shrooma", "base plant monster");
		Monster.stats(t, 40, 60, 20, 100, 39, 70, 6, 27);
		t.set("ARM", 80);
		t.set("Image", 325);
		t.set("MoveSpeed", 60);
		t.incStat(Skill.CASTING, 1);
		t.set("DefaultThings", "Aruk's Poison Cloud");
		t.set("LevelMin", 22);
		t.set("DeathDecoration", "Poison Cloud,[IsMushroom]");
		Lib.add(t);

		t = Lib.extend("vileweed", "base plant monster");
		Monster.stats(t, 80, 100, 0, 120, 1, 50, 0, 3);
		t.set("ARM", 120);
		t.set("Image", 323);
		t.set("MoveSpeed", 10);
		t.set("AttackSpeed", 100);
		t.set("LevelMin", 24);
		t.set("DeathDecoration", "Poison Cloud,slime pool");
		t.set("UnarmedWeapon", Lib.create("poison whip attack"));
		Lib.add(t);
	}

	static void initBirds() {
		// Little critters
		// low level, quite common
		Thing t;

		t = Lib.extend("base bird", "base monster");
		t.set("IsBird", 1);
		t.set("IsBeast", 1);
		t.set("Frequency", 45);
		t.set("IsFlying", 1);
		t.set("MoveSpeed", 200);
		t.set("Image", 291);
		t.set("RES:shock", 20);
		t.set("RES:fire", -5);
		Lib.add(t);

		t = Lib.extend("kestrel", "base bird");
		t.set("LevelMin", 3);
		t.set("DeathDecoration", "blood pool,40% kestrel feather");
		stats(t, 7, 4, 18, 3, 1, 3, 5, 2);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		Lib.add(t);

		t = Lib.extend("hawk", "base bird");
		t.set("LevelMin", 7);
		t.set("DeathDecoration", "blood pool,40% hawk feather");
		stats(t, 16, 10, 28, 13, 2, 6, 7, 3);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		Lib.add(t);

		t = Lib.extend("eagle", "hawk");
		t.set("LevelMin", 11);
		t.set("ARM", 3);
		t.set("DeathDecoration", "blood pool,40% hawk feather");
		stats(t, 26, 15, 38, 18, 6, 16, 27, 3);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		Lib.add(t);
	}

	/*
	 * Dragons are fast, tough and deadly
	 */
	static void initDragons() {
		Thing t;

		t = Lib.extend("base dragon", "base monster");
		t.set("IsBeast", 1);
		t.set("IsDragon", 1);
		t.set("IsReptile", 1);
		t.set("IsViewBlocking", 1);
		t.set("Frequency", 20);
		stats(t, 120, 140, 100, 200, 250, 190, 200, 240);
		t.set("ARM", 300);
		t.set("AttackSpeed", 200);
		t.set("RES:poison", 20);
		t.set("RES:fire", 10);
		t.set("MoveSpeed", 200);
		t.set("Luck", 70);
		t.set("IsFlying", 1);
		t.set(Skill.UNARMED, 3);
		t.set(Skill.ATTACK, 3);
		t.set(Skill.DEFENCE, 4);
		t.set(Skill.BRAVERY, 2);
		t.set(Skill.CASTING, 2);
		t.set(Skill.FOCUS, 3);
		t.set("UnarmedWeapon", Lib.create("razor claw attack"));
		t.set("LevelMin", 30);
		t.set("Image", 645);
		Lib.add(t);

		t = Lib.extend("swamp dragon", "base dragon");
		Monster.strengthen(t, 0.8);
		t.set("RES:poison", 1000);
		t.set("LevelMin", 29);
		t.set("Image", 645);
		t.addHandler("OnAction",
				breathAttack("poison", "noxious fumes", 2, 40, 41));
		Lib.add(t);

		t = Lib.extend("green dragon", "base dragon");
		Monster.strengthen(t, 1.0);
		t.set("LevelMin", 30);
		t.set("Image", 647);
		t.addHandler("OnAction",
				breathAttack("poison", "poisonous fumes", 2, 60, 41));
		t.addHandler("OnAction",
				breathAttack("fire", "blazing flames", 2, 60, 1));
		Lib.add(t);

		t = Lib.extend("blue dragon", "base dragon");
		t.set("RES:shock", 1000);
		t.set("LevelMin", 31);
		t.set("Image", 644);
		t.addHandler("OnAction",
				breathAttack("ice", "crackling lightning", 3, 60, 61));
		Lib.add(t);

		t = Lib.extend("frost dragon", "base dragon");
		t.set("RES:ice", 1000);
		t.set("RES:fire", -15);
		t.set("LevelMin", 32);
		t.addHandler("OnAction",
				breathAttack("ice", "a blast of ice", 5, 60, 101));

		t.set("Image", 640);
		Lib.add(t);

		t = Lib.extend("red dragon", "base dragon");
		Monster.strengthen(t, 1.5);
		t.set("RES:fire", 1000);
		t.set("RES:ice", -15);
		t.set("LevelMin", 34);
		t.set("Image", 641);
		t.addHandler("OnAction",
				breathAttack("fire", "hellish flames", 6, 100, 1));
		Lib.add(t);

		t = Lib.extend("black dragon", "base dragon");
		Monster.strengthen(t, 1.5);
		t.set("RES:ice", 1000);
		t.set("RES:fire", -15);
		t.set("LevelMin", 36);
		t.set("Image", 642);
		t.set("Luck", 100);
		t.set(Skill.CASTING, 6);
		t.set(Skill.FOCUS, 5);
		t.addHandler("OnAction",
				breathAttack("fire", "hellish flames", 4, 60, 1));
		t.set("DefaultThings",
				"[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell],[IsSpell]");
		Lib.add(t);

	}

	static void initCentipedes() {
		Thing t;

		t = Lib.extend("base centipede", "base monster");
		t.set("IsCentipede", 1);
		t.set("IsInsect", 1);
		t.set("IsBeast", 1);
		t.set("Frequency", 40);
		stats(t, 7, 5, 5, 7, 2, 6, 1, 1);
		t.set("MoveSpeed", 100);
		t.set("Image", 560);
		t.set("ARM", 2);
		t.set("RES:poison", 20);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		Lib.add(t);

		t = Lib.extend("centipede", "base centipede");
		t.set("LevelMin", 2);
		Lib.add(t);

		t = Lib.extend("giant centipede", "base centipede");
		Monster.strengthen(t, 1.5);
		t.set("LevelMin", 4);
		Lib.add(t);

		t = Lib.extend("monster centipede", "base centipede");
		Monster.strengthen(t, 2);
		t.set("Image", 565);
		t.set("LevelMin", 7);
		Lib.add(t);

		t = Lib.extend("demon centipede", "base centipede");
		Monster.strengthen(t, 3);
		t.set("Image", 563);
		t.set("LevelMin", 10);
		Lib.add(t);

		t = Lib.extend("magipede", "base centipede");
		Monster.strengthen(t, 3);
		stats(t, 10, 8, 15, 17, 25, 28, 4, 12);
		t.set("Image", 561);
		t.set(Skill.CASTING, 2);
		t.set("DefaultThings", "[IsSpell],[IsSpell],[IsSpell]");
		t.set("LevelMin", 13);
		Lib.add(t);
	}

	static void initWorms() {
		Thing t;

		t = Lib.extend("base worm", "base monster");
		t.set("IsWorm", 1);
		t.set("IsBeast", 1);
		t.set("Frequency", 25);
		stats(t, 6, 4, 8, 4, 2, 4, 2, 2);
		t.set("MoveSpeed", 90);
		t.set("Image", 624);
		t.set("RES:fire", -20);
		t.set("RES:poison", 20);
		Lib.add(t);

		t = Lib.extend("giant worm", "base worm");
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		stats(t, 8, 7, 3, 15, 1, 12, 1, 1);
		t.set("LevelMin", 4);
		t.set("Image", 624);
		Lib.add(t);

		t = Lib.extend("vileworm", "base worm");
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		stats(t, 18, 17, 13, 35, 1, 22, 1, 1);
		t.set("LevelMin", 11);
		t.set("Image", 621);
		Lib.add(t);

		t = Lib.extend("fire worm", "base worm");
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		stats(t, 18, 27, 13, 35, 1, 22, 1, 1);
		t.set("LevelMin", 13);
		t.set("RES:fire", 1000);
		t.set("RES:ice", 20);
		t.set("MoveSpeed", 100);
		t.set("Image", 620);
		Lib.add(t);

		t = Lib.extend("chaos worm", "base worm");
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		stats(t, 28, 27, 23, 45, 1, 32, 1, 1);
		t.set("IsChaotic", 1);
		t.set("LevelMin", 15);
		t.set("MoveSpeed", 100);
		t.set("Image", 625);
		Lib.add(t);

		t = Lib.extend("corpse worm", "base worm");
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		stats(t, 38, 37, 33, 55, 1, 42, 1, 1);
		t.set("LevelMin", 17);
		t.set("MoveSpeed", 100);
		t.set("AttackSpeed", 150);
		t.set("Image", 623);
		Lib.add(t);

	}

	static void initSnakes() {
		// Little critters
		// low level, quite common
		Thing t;

		t = Lib.extend("base snake", "base monster");
		t.set("IsSnake", 1);
		t.set("IsBeast", 1);
		t.set("IsReptile", 1);
		t.set("Frequency", 25);
		stats(t, 6, 4, 8, 4, 2, 4, 2, 2);
		t.set("MoveSpeed", 70);
		t.set("Image", 567);
		t.set("RES:ice", -5);
		t.set("RES:poison", 20);
		t.set(Skill.SWIMMING, 1);
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		t.set("ASCII", "s");
		Lib.add(t);

		t = Lib.extend("grass snake", "base snake");
		t.set("UnarmedWeapon", Lib.create("unarmed attack"));
		t.set("LevelMin", 1);
		Lib.add(t);

		t = Lib.extend("small snake", "grass snake");
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		t.set("LevelMin", 2);
		Lib.add(t);

		t = Lib.extend("snake", "base snake");
		t.set("LevelMin", 3);
		t.set("DeathDecoration", "blood pool,40% snake skin");
		Lib.add(t);

		t = Lib.extend("python", "base snake");
		stats(t, 26, 17, 22, 16, 3, 17, 2, 5);
		t.set("Image", 566);
		t.set("LevelMin", 9);
		t.set("DeathDecoration", "blood pool,40% snake skin");
		Lib.add(t);

		t = Lib.extend("red snake", "base snake");
		t.set("Image", 570);
		stats(t, 16, 7, 12, 6, 3, 7, 2, 3);
		t.set("LevelMin", 6);
		t.set("RES:ice", -10);
		t.set("ARM", 2);
		t.set("DeathDecoration", "blood pool,40% red snake skin");
		Lib.add(t);

		t = Lib.extend("fire snake", "red snake");
		stats(t, 22, 14, 18, 9, 8, 12, 7, 5);
		t.set("RES:ice", 10);
		t.set("LevelMin", 8);
		Lib.add(t);

		t = Lib.extend("demon snake", "fire snake");
		stats(t, 26, 19, 28, 29, 18, 23, 16, 12);
		t.addHandler("OnAction", Scripts.generator("fire snake", 200));
		t.set("MoveSpeed", 60);
		t.set("AttackSpeed", 60);
		t.set("LevelMin", 13);
		t.set("LevelMax", 20);
		t.set("FearFactor", 2);
		t.set("ARM", 6);
		t.set("Image", 568);
		Lib.add(t);
	}

	static void initSpiders() {
		Thing t;

		t = Lib.extend("base spider", "base monster");
		t.set("IsSpider", 1);
		t.set("IsBeast", 1);
		t.set("Frequency", 50);
		Monster.stats(t, 5, 5, 6, 8, 3, 9, 1, 3);
		t.set("MoveSpeed", 100);
		t.set("Image", 164);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		t.set("LevelMin", 1);
		t.set("RES:fire", -10);
		t.set("ARM", 1);
		t.set("ASCII", "s");
		Lib.add(t);

		t = Lib.extend("small spider", "base spider");
		t.set("Image", 164);
		Monster.stats(t, 5, 3, 5, 2, 3, 9, 1, 3);
		t.set("LevelMin", 1);
		t.set("ARM", 1);
		t.set("DeathDecoration", "20% squished spider, blood pool");
		Lib.add(t);

		t = Lib.extend("spider", "base spider");
		t.set("Image", 164);
		Monster.stats(t, 7, 5, 8, 6, 3, 12, 1, 3);
		t.set("LevelMin", 4);
		t.set("DeathDecoration", "20% dead spider, blood pool");
		Lib.add(t);

		t = Lib.extend("wolf spider", "base spider");
		t.set("Image", 184);
		Monster.stats(t, 9, 6, 9, 9, 3, 15, 1, 4);
		t.set("MoveSpeed", 120);
		t.set("AttackSpeed", 120);
		t.set("ARM", 3);
		t.set("LevelMin", 6);
		t.set("DeathDecoration", "30% dead wolf spider, blood pool");
		Lib.add(t);

		t = Lib.extend("large spider", "base spider");
		t.set("Image", 184);
		Monster.stats(t, 12, 10, 10, 18, 3, 20, 2, 5);
		t.set("LevelMin", 7);
		t.set("ARM", 5);
		t.addHandler("OnAction", Scripts.generator("spider web", 20));
		t.set("DeathDecoration", "40% dead spider, blood pool");
		Lib.add(t);

		t = Lib.extend("red spider", "base spider");
		Monster.stats(t, 13, 7, 12, 15, 4, 18, 2, 6);
		t.set("Image", 165);
		t.set("ARM", 6);
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		t.set("LevelMin", 8);
		t.set("DeathDecoration", "30% dead red spider, blood pool");
		Lib.add(t);

		t = Lib.extend("black widow", "base spider");
		Monster.stats(t, 33, 12, 22, 15, 4, 28, 4, 8);
		t.set("Image", 583);
		t.set("ARM", 7);
		t.set("UnarmedWeapon", Lib.create("strong poison attack"));
		t.set("LevelMin", 13);
		t.set("DeathDecoration", "40% dead black widow, blood pool");
		Lib.add(t);

		t = Lib.extend("baby giant spider", "base spider");
		Monster.stats(t, 12, 10, 10, 10, 3, 20, 2, 5);
		t.set("Image", 167);
		t.set("ARM", 8);
		t.set("DecayRate", 10);
		t.set("DecayType", "giant spider");
		t.addHandler("OnAction", Scripts.decay());
		t.set("LevelMin", 13);
		t.set("DeathDecoration", "30% dead spider, blood pool");
		Lib.add(t);

		t = Lib.extend("giant spider", "base spider");
		Monster.stats(t, 23, 19, 18, 25, 6, 28, 4, 10);
		t.set("Image", 187);
		t.set("ARM", 18);
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		t.addHandler("OnAction", Scripts.generator("giant spider web", 20));
		t.set("LevelMin", 14);
		t.set("DeathDecoration", "40% dead spider, blood pool");
		Lib.add(t);

		t = Lib.extend("tarantula", "base spider");
		Monster.stats(t, 33, 29, 28, 35, 6, 48, 6, 12);
		t.set("Image", 585);
		t.set("ARM", 18);
		t.set("UnarmedWeapon", Lib.create("strong poison attack"));
		t.set("LevelMin", 21);
		t.set("DeathDecoration", "20% dead tarantula, blood pool");
		Lib.add(t);

		t = Lib.extend("giant tarantula", "tarantula");
		Monster.stats(t, 33, 39, 28, 55, 6, 68, 6, 15);
		t.set("Image", 585);
		t.set("ARM", 30);
		t.set("UnarmedWeapon", Lib.create("strong poison attack"));
		t.set("LevelMin", 24);
		t.set("DeathDecoration", "50% dead tarantula, blood pool");
		Lib.add(t);

	}

	public static void initRalkans() {
		Thing t;

		t = Lib.extend("base ralkan", "base monster");
		t.set("IsSpider", 1);
		t.set("Frequency", 50);
		Monster.stats(t, 25, 25, 12, 48, 5, 29, 3, 3);
		t.set("MoveSpeed", 80);
		t.set("Image", 186);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		t.set("LevelMin", 15);
		t.set("ARM", 40);
		t.set("ASCII", "R");
		Lib.add(t);

		t = Lib.extend("baby ralkan", "base ralkan");
		t.set("Image", 166);
		t.set("ARM", 30);
		t.set("DecayRate", 3);
		t.set("DecayType", "ralkan");
		t.addHandler("OnAction", Scripts.decay());
		t.set("LevelMin", 15);
		Lib.add(t);

		t = Lib.extend("ralkan", "base ralkan");
		Monster.strengthen(t, 2);
		t.set("Image", 186);
		t.set("ARM", 90);
		t.addHandler("OnAction", Scripts.generator("baby ralkan", 5));
		t.set("LevelMin", 18);
		Lib.add(t);

		t = Lib.extend("forest ralkan", "base ralkan");
		Monster.strengthen(t, 3);
		t.set("Image", 192);
		t.set("ARM", 60);
		t.set("LevelMin", 19);
		t.set("UnarmedWeapon", Lib.create("poison bite attack"));
		Lib.add(t);
	}

	static void initCritters() {
		// Little critters
		// low level, quite common
		Thing t;

		t = Lib.extend("base critter", "base monster");
		t.set("IsCritter", 1);
		t.set("Frequency", 50);
		t.set("LevelMin", 1);
		t.set("IsBeast", 1);
		t.set("UnarmedWeapon", Lib.create("bite attack"));
		Lib.add(t);

		t = Lib.extend("rat", "base critter");
		t.set("Image", 283);
		t.set("IsRat", 1);
		t.set("LevelMin", 2);
		stats(t, 3, 2, 3, 2, 1, 2, 1, 1);
		t.set("DeathDecoration", "blood pool,20% rat tail");
		t.set("ASCII", "r");
		Lib.add(t);

		t = Lib.extend("field mouse", "rat");
		t.set("Image", 283);
		t.set("LevelMin", 1);
		t.set("LevelMax", 3);
		stats(t, 3, 1, 4, 1, 1, 1, 1, 1);
		t.set("DeathDecoration", "blood pool,20% mouse tail");
		Lib.add(t);

		t = Lib.extend("small rat", "rat");
		t.set("Image", 283);
		t.set("LevelMin", 1);
		t.set("LevelMax", 3);
		stats(t, 4, 1, 3, 1, 1, 1, 1, 1);
		Lib.add(t);

		t = Lib.extend("big rat", "rat");
		t.set("LevelMin", 3);
		stats(t, 4, 4, 5, 4, 1, 3, 1, 1);
		Lib.add(t);

		// nasty sewer rat breeder
		t = Lib.extend("sewer rat", "rat");
		t.set("LevelMin", 4);
		t.set("LevelMax", 12);
		t.set("Image", 296);
		t.set("IsGenerator", 1);
		t.addHandler("OnAction", Scripts.generator("sewer rat", 10));
		stats(t, 5, 3, 6, 3, 3, 6, 1, 5);
		Lib.add(t);

		t = Lib.extend("giant rat", "rat");
		stats(t, 6, 7, 6, 8, 2, 8, 2, 2);
		t.set("LevelMin", 5);
		t.set("LevelMax", 10);
		t.set("Image", 284);
		Lib.add(t);

		// nasty chaos rat breeder
		t = Lib.extend("chaos rat", "rat");
		t.set("LevelMin", 8);
		t.set("Image", 295);
		t.set("IsGenerator", 1);
		t.set("IsChaotic", 1);
		t.addHandler("OnAction", Scripts.generator("chaos rat", 30));
		t.set("UnarmedWeapon", Lib.create("poison attack"));
		stats(t, 10, 8, 16, 8, 5, 8, 2, 7);
		Lib.add(t);

	}

	static void initDogs() {
		Thing t;

		t = Lib.extend("dog", "base critter");
		t.set("Image", 287);
		t.set("LevelMin", 3);
		t.set("MoveSpeed", 130);
		t.set(Skill.SWIMMING, 1);
		t.set(Skill.UNARMED, 1);
		stats(t, 7, 6, 5, 5, 1, 3, 1, 1);
		t.set("ASCII", "d");
		Lib.add(t);

		t = Lib.extend("big dog", "base critter");
		t.set("Image", 287);
		t.set("LevelMin", 6);
		t.set("MoveSpeed", 120);
		stats(t, 7, 9, 4, 9, 2, 5, 2, 2);
		Lib.add(t);

		t = Lib.extend("hound", "base critter");
		t.set("Image", 287);
		t.set("LevelMin", 9);
		t.set("MoveSpeed", 150);
		t.set("AttackSpeed", 150);
		stats(t, 17, 12, 14, 19, 2, 5, 2, 2);
		Lib.add(t);
	}

	static void initFrogs() {
		Thing t;

		t = Lib.extend("base frog", "base critter");
		t.set("IsFrog", 1);
		t.set("IsBeast", 1);
		t.set("Image", 521);
		t.set(Skill.SWIMMING, 3);
		t.multiplyStat("Frequency", 0.3);
		t.set("MoveSpeed", 100);
		stats(t, 3, 4, 7, 4, 4, 7, 1, 1);
		t.set("LevelMin", 2);
		t.set("RES:fire", -15);
		t.set("RES:poison", 20);
		t.set("RES:acid", 100);
		t.set("UnarmedWeapon", Lib.create("acid attack"));
		t.set("ASCII", "f");
		t.set("DeathDecoration", "blood pool,30% frog leg");
		Lib.add(t);

		t = Lib.extend("giant frog", "base frog");
		t.set("Image", 521);
		stats(t, 10, 6, 16, 9, 4, 25, 2, 2);
		t.set("LevelMin", 13);
		Lib.add(t);

		t = Lib.extend("giant toad", "base frog");
		t.set("Image", 520);
		stats(t, 26, 18, 24, 29, 4, 45, 2, 2);
		t.set("ARM", 10);
		t.set(Skill.DEFENCE, 1);
		t.set("LevelMin", 16);
		t.set("DeathDecoration", "blood pool,30% toad leg");
		Lib.add(t);

		t = Lib.extend("horntoad", "giant toad");
		t.set("Image", 522);
		stats(t, 46, 35, 50, 69, 34, 85, 6, 27);
		t.set(Skill.ATTACK, 1);
		t.set(Skill.DEFENCE, 2);
		t.set("ARM", 25);
		t.set("LevelMin", 19);
		Lib.add(t);
	}

	static void initSlimes() {
		Thing t;

		t = Lib.extend("base slime", "base monster");
		t.set("IsIntelligent", 0);
		t.set("IsLiving", 0);
		t.set("IsSlime", 1);
		t.set("Image", 380);
		Lib.add(t);

		t = Lib.extend("yellow slime", "base slime");
		Monster.stats(t, 10, 10, 10, 10, 1, 5, 0, 0);
		t.set("LevelMin", 4);
		t.set("Image", 383);
		Lib.add(t);

		t = Lib.extend("grey slime", "base slime");
		Monster.stats(t, 15, 15, 10, 30, 1, 15, 0, 0);
		t.set("LevelMin", 7);
		t.set("Image", 381);
		{
			final ItemDamageHit idh = new ItemDamageHit("wielded", "acid", 50,
					10);
			t.set("OnHit", idh);
		}
		Lib.add(t);

		t = Lib.extend("purple slime", "base slime");
		Monster.stats(t, 10, 10, 10, 20, 1, 10, 0, 0);
		t.set("LevelMin", 8);
		t.set("Image", 380);
		t.set("OnTouch", Scripts.damage("acid", 2,
				"hit by the noxious acid of the purple slime", 25));
		Lib.add(t);

		t = Lib.extend("red slime", "base slime");
		Monster.stats(t, 20, 20, 20, 40, 1, 25, 0, 0);
		t.set("LevelMin", 9);
		t.set("Image", 382);
		{
			final ItemDamageHit idh = new ItemDamageHit("wielded", "fire", 40,
					5);
			t.set("OnHit", idh);
		}
		t.set("UnarmedWeapon", Lib.create("curse attack"));
		t.set("DeathDecoration", "Fireball");
		Lib.add(t);

	}

	private static void initNamedFoes() {
		Thing t;

		t = Lib.extend("Borrok", "goblin shaman");
		Monster.strengthen(t, 1.3);
		Lib.add(t);
	}
}
