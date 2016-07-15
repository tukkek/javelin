package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import javelin.model.BattleMap;
import tyrant.mikera.engine.Description;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * 
 * @author Mike
 * 
 *         Code for management of beings in the Tyrant universe
 */
public class Being {

	protected static void setStats(Thing t, int[] bs) {
		for (int i = 0; i < bs.length; i++) {
			t.set(RPG.stats[i], bs[i]);
		}
	}

	public static boolean tryDrop(Thing h, Thing t) {
		if (t.place != h) {
			throw new Error("Thing in wrong place!");
		}

		// break if worn and cursed
		if (t.y > 0 && t.getFlag("IsCursed")) {
			h.message("You are unable to remove " + t.getYourName() + "!");
			return false;
		}

		int total = t.getStat("Number");
		if (h == Game.hero() && total > 1) {
			int n = Game.getNumber("Drop how many (Enter=All)? ", total);
			if (n > 0) {
				t = t.remove(n);
			} else {
				Game.messageTyrant("");
				return false;
			}
		}
		String dropName = t.getName(h);
		Item.drop(h, t);
		h.message("Dropped " + dropName);
		h.incStat("APS", -Being.actionCost(h));
		return true;
	}

	public static boolean tryPickup(Thing h, Thing t) {
		boolean pickup = true;

		if (Item.isOwned(t)) {

			// find value
			int val = RPG.max(1, Item.value(t));

			// present options
			char c;
			if (t.getFlag("IsShopOwned") && !t.getFlag("IsMoney")) {
				Thing sk = Item.findShopkeeper(t);

				// get shopkeeper price
				if (sk != null) {
					val = Item.shopPrice(t, h, sk);
				}

				Game.messageTyrant(t.getTheName() + " " + t.verb("cost") + " "
						+ Coin.valueString(val));
				Game.messageTyrant("Buy, Shoplift or Leave? (b/s/l)");
				c = Game.getOption("sbl");
			} else {
				Game.messageTyrant(t.getTheName() + " " + t.is() + " not yours");
				Game.messageTyrant("Steal? (y/n)");
				c = Game.getOption("synl");
				if (c == 'y') {
					c = 's';
				}
			}
			Game.messageTyrant("");

			if (c == 'l') {
				pickup = false;
			}
			if (c == 'b') {
				int funds = Coin.getMoney(h);
				if (val > funds) {
					Game.messageTyrant("You can't afford that!");
					Game.messageTyrant("You only have "
							+ Coin.valueString(funds));
					pickup = false;
				} else {
					Game.messageTyrant("You pay " + Coin.valueString(val));
					Coin.removeMoney(h, val);
					Item.clearOwnership(t);
				}
			} else if (c == 's') {
				pickup = true;
			} else {
				pickup = false;
			}
		}

		if (pickup) {
			h.incStat("APS", -Being.actionCost(h));
			Item.pickup(h, t);
		}

		return pickup;
	}

	/**
	 * Get APS cost for a generic action
	 * 
	 * 100 is the base, sleight of hand reduces
	 * 
	 * @param b
	 * @return
	 */
	public static int actionCost(Thing b) {
		return 300 / (3 + b.getStat(Skill.SLEIGHT));

	}

	/**
	 * Calculates the maximum carrying weight for a Being
	 * 
	 * @param b
	 *            A Being
	 * @return Max weight in 100ths of stones
	 */
	public static int maxCarryingWeight(Thing b) {
		int st = (int) (b.getStat(RPG.ST_ST) * (1.0 + 0.25 * b
				.getStat(Skill.TRADING)));

		int max = st * 10000 + 20000;
		max = max * b.getStat("CarryFactor") / 100;
		return max;
	}

	public static boolean feelsFear(Thing b) {
		// TODO: account for insanity etc.
		return b.getFlag("IsLiving") && !b.getFlag(Skill.BRAVERY);
	}

	public static int calcInventoryWeight(Thing b) {
		Thing[] ts = b.getItems();

		int inventoryWeight = 0;
		for (Thing t : ts) {
			int wt = t.getWeight();
			inventoryWeight += wt;
		}
		return inventoryWeight;
	}

	public static void calcEncumberance(Thing b) {
		Thing[] ts = b.getItems();

		int enc = 0;
		int st = b.getStat(RPG.ST_ST);
		int inventoryWeight = 0;
		for (Thing t : ts) {
			int wt = t.getWeight();
			if (t.y > 0 && t.y != RPG.WT_MISSILE) {
				// wielded items encumberance
				enc += wt / (st * 40);
			}
			inventoryWeight += wt;
		}

		// encumberance for % of max weight
		enc += inventoryWeight * 100 / maxCarryingWeight(b);

		// encumberance if over-eaten
		// up to max of 50%
		int hungerMax = b.getStat(RPG.ST_HUNGERTHRESHOLD);
		int hl = 0;
		if (hungerMax > 0) {
			hl = 100 * b.getStat(RPG.ST_HUNGER) / hungerMax;
		}
		if (hl < 0) {
			enc += -hl / 2;
		}

		// no encumberance for first 15%
		// also trader bonus
		enc = RPG.middle(0, enc - (20 + 3 * b.getStat(Skill.TRADING)), 100);

		b.set(RPG.ST_ENCUMBERANCE, enc);
	}

	public static double getHealth(Thing b) {
		double health = b.getStat("HPS") / (double) b.getStat("HPSMAX");
		if (health <= 0.0) {
			return 0.0;
		}
		if (health >= 1.0) {
			return 1.0;
		}
		return health;
	}

	public static void heal(Thing b, int h) {
		if (!b.getFlag("IsBeing")) {
			return;
		}

		// healing damages undead!
		if (b.getFlag("IsUndead")) {
			h = -h;
		}

		int hps = b.getStat("HPS");
		int hpsmax = b.getStat("HPSMAX");
		int gain = RPG.min(h, hpsmax - hps);
		if (gain > 0) {
			if (gain > hps) {
				b.message("You feel much better!");
			} else if (gain + hps == hpsmax) {
				b.message("You feel superb");
			} else {
				b.message("You feel better");
			}
		} else if (hps == hpsmax) {
			b.message("You feel great!");
		} else if (gain < 0) {
			b.message("You feel strangely worse");
		}
		b.incStat("HPS", gain);
	}

	// gain multiple levels to a given target
	public static void gainLevel(Thing t, int targetlevel) {
		while (t.getStat(RPG.ST_LEVEL) < targetlevel) {
			gainLevel(t);
		}
	}

	public static void gainStat(Thing t, String stat) {
		gainStat(t, stat, 1);
	}

	public static void gainStat(Thing t, String stat, int a) {
		t.incStat(stat, a);
		if (a == 0) {
			return;
		}

		if (stat.equals("SK")) {
			t.message("You feel your reflexes sharpening");
		}
		if (stat.equals("ST")) {
			t.message("You feel stronger now. What bulging muscles!");
		}
		if (stat.equals("AG")) {
			t.message("You feel nimble and quick");
		}
		if (stat.equals("TG")) {
			t.message("You feel healthier");
		}
		if (stat.equals("IN")) {
			t.message("You feel thoughtful");
		}
		if (stat.equals("WP")) {
			t.message("You feel more determined");
		}
		if (stat.equals("CH")) {
			t.message("You feel good about yourself");
		}
		if (stat.equals("CR")) {
			t.message("You feel inspiration coming on");
		}
	}

	public static int statIndex(String s) {
		return RPG.index(s, stats);
	}

	private static final String[] stats = { "SK", "ST", "AG", "TG", "IN", "WP",
			"CH", "CR" };

	public static int averageStat(Thing b) {
		int result = 0;

		for (String stat : stats) {
			result += b.getBaseStat(stat);
		}
		return result / 8;
	}

	// gain a level
	// enhance stats / skills as needed
	public static final double STAT_LEVEL_GAIN = 0.1;

	public static String[] statNames() {
		return stats;
	}

	public static void registerKill(Thing h, Thing t) {
		if (h != null && t.getFlag("IsBeing")) {
			int killlevel = t.getLevel();
			if (h.isHero()) {
				Hero.gainKillExperience(h, t);

			}
			if (h.getStat(RPG.ST_SCORE_BESTKILL) < killlevel) {
				h.set(RPG.ST_SCORE_BESTKILL, killlevel);
			}
			h.incStat(RPG.ST_SCORE_KILLS, 1);
		}

		Event e = new Event("Kill");
		e.set("Killer", h);
		e.set("Target", t);
		Quest.notifyKill(e);
	}

	public static void gainLevel(Thing t) {
		int hpgain = RPG.round(t.getBaseStat("TG") / 3.0);
		int mpgain = RPG.round(t.getBaseStat("WP") / 3.0);
		t.multiplyStat("HPS", 1.0 + hpgain / (double) t.getStat("HPSMAX"));
		t.multiplyStat("MPS", 1.0 + mpgain / (double) t.getStat("MPSMAX"));
		t.incStat("HPSMAX", hpgain);
		t.incStat("MPSMAX", mpgain);

		int level = t.getStat(RPG.ST_LEVEL) + 1;
		t.set(RPG.ST_LEVEL, level);
		t.incStat(RPG.ST_SKILLPOINTS, 1);

		if (t.isHero()) {
			Game.messageTyrant("You have achieved level " + level);
		} else {
			// add skills automatically for creatures/NPCs
			autoLearnSkills(t);
		}

		for (int i = 0; i < 8; i++) {
			int stat = t.getBaseStat(RPG.stats[i]);
			double gain = STAT_LEVEL_GAIN;

			if (RPG.d(2) == 1) {
				gainStat(t, RPG.stats[i], RPG.round(stat * gain));
			}
		}

	}

	// use initially gifted items
	// not very discerning
	public static void utiliseItems(Thing being) {
		if (!being.getFlag("IsIntelligent")) {
			return;
		}
		Thing[] stuff = being.getWieldableContents();
		if (stuff != null) {
			for (Thing it : stuff) {
				// test before wielding
				// in case a previous wield has killed the creaure
				// this is unlikely, but possible
				if (it.place == being) {
					being.wield(it, it.getStat("WieldType"));
				}
			}
		}
	}

	// throw an item
	public static void throwThing(Thing b, Thing t, int tx, int ty) {
		BattleMap m = b.getMap();
		if (m == null) {
			return;
		}

		// find where thing hits
		Point p = m.tracePath(b.x, b.y, tx, ty);

		// check for current location
		if (p.x == b.x && p.y == b.y) {
			b.dropThing(t);
			return;
		}
		Missile.throwAt(t, b, m, p.x, p.y);
	}

	public static int calcViewRange(Thing h) {
		int r = h.getStat("VisionRange");
		BattleMap map = h.getMap();
		if (map != null) {
			int mr = map.getStat("VisionRange");
			if (mr > 0 && mr < r) {
				r = mr;
			}
		}
		return r;
	}

	// automatic skill gains for creatures/NPCs
	// TODO: make more focused based on NPC type
	private static final String[] commonSkills = new String[] { Skill.ATTACK,
			Skill.ATTACK, Skill.DEFENCE, Skill.DEFENCE, Skill.FEROCITY,
			Skill.ATHLETICS, Skill.BRAVERY, Skill.CASTING, Skill.FOCUS };

	public static void autoLearnSkills(Thing t) {
		while (t.getStat(RPG.ST_SKILLPOINTS) > 0) {
			t.incStat(RPG.ST_SKILLPOINTS, -1);
			String[] ss;
			if (RPG.d(3) == 1) {
				ss = Skill.fullList();
			} else {
				ss = commonSkills;
			}

			String sk = ss[RPG.r(ss.length)];
			t.incStat(sk, 1);

			// create various acouterements to accompany new skills
			if (t.getFlag("IsIntelligent") && t.getFlag("IsLiving")) {
				int level = t.getLevel();
				if (Spell.isOrder(sk)) {
					t.addThing(Spell.randomSpell(sk, t.getLevel()));
				} else if (sk.equals("Throwing") && RPG.d(3) == 1) {
					t.addThing(Lib.createType("IsThrowingWeapon", level));
				} else if (sk.equals("Archery") && RPG.d(2) == 1) {
					Thing rw = Lib.createType("IsRangedWeapon", level);
					t.addThing(rw);
					t.addThing(RangedWeapon.createAmmo(rw, level));
				} else if (sk.equals("Throwing") && RPG.d(2) == 1) {
					Thing tw = Lib.createType("IsThrowingWeapon", level);
					t.addThing(tw);
				} else if (sk.equals("Alchemy") && RPG.d(3) == 1) {
					t.addThing(Lib.createType("IsPotion", level));
				}

			}

		}
	}

	public static void recover(Thing t, int time) {

		int mps = t.getStat("MPS");
		if (mps < 0) {
			t.incStat("MPS", -mps);
			Damage.inflict(t, mps, "special");
			mps = 0;
		}

		boolean hungry = false;
		if (t.isHero()) {
			int hunger = t.getStat(RPG.ST_HUNGER);
			int hungerThreshold = t.getStat(RPG.ST_HUNGERTHRESHOLD);
			hungry = hunger > hungerThreshold;
		}

		// recharge hit points if not very hungry
		if (!hungry) {
			// regenerate hit points
			int regenerate = t.getStat(RPG.ST_REGENERATE);
			int heal = t.getStat(Skill.HEALING);

			int gain = 0;
			int hpsmax = t.getStat(RPG.ST_HPSMAX);
			int hps = t.getStat("HPS");
			if (regenerate > 0) {
				int tg = t.getStat("TG");
				gain += RPG.round((float) time * regenerate * tg / 1000000);
			}
			if (heal > 0) {
				gain += RPG.round((float) time * heal * 50 * hpsmax / 1000000);
			}
			t.set("HPS", RPG.middle(hps, hps + gain, hpsmax));

			int recharge = t.getStat(RPG.ST_RECHARGE);

			if (recharge > 0) {
				int mpsmax = t.getStat(RPG.ST_MPSMAX);
				t.set("MPS",
						RPG.middle(
								mps,
								mps
										+ RPG.round((float) time * recharge
												* mpsmax / 1000000), mpsmax));
			}

			if (t.getFlag(Skill.FOCUS) || t.getFlag(Skill.PRAYER)) {
				Spell.rechargeSpells(t, time);
			}
		}

	}

	public static void init() {
		Thing t;

		t = Lib.extend("base being", "base thing");
		t.set("HPS", 1);
		t.set("ImageSource", "Creatures");
		t.set("Image", 340);
		t.set("IsBeing", 1);
		t.set("IsMobile", 1);
		t.set("IsLiving", 1);
		t.set("IsActive", 1);
		t.set("IsPhysical", 1);
		t.set("IsJumpable", 1);
		t.set("IsDestructible", 1);
		t.set("IsDisplaceable", 1);
		t.set("IsRegenerating", 1);
		t.set("CarryFactor", 100);
		t.set("VisionRange", 6);
		t.set(RPG.ST_RECHARGE, 200);
		t.set(RPG.ST_REGENERATE, 40);
		t.set("Speed", 100);
		t.set("MoveSpeed", 100);
		t.set("AttackSpeed", 100);
		t.set(RPG.ST_PEITY, 20);
		t.set("Frequency", 50);
		t.set("Alignment", "N");
		t.set("IsBlocking", 1);
		t.set("MoveCost", 100);
		// t.addHandler("OnAction",new AI.AIScript());
		t.set("DeathDecoration", "blood pool");
		t.set("RES:water", 35);
		t.set("NameType", Description.NAMETYPE_NORMAL);
		t.set("Z", Thing.Z_MOBILE);
		Lib.add(t);
	}
}