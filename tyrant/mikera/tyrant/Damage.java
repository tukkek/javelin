/*
 * Created on 27-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyrant.mikera.tyrant;

import javelin.controller.old.Game;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * @author Mike
 * 
 *         Contains routines for the management of damage to items and monsters.
 * 
 */
public class Damage {
	// damage is halved for each 10 levels of resistance
	private static final double resistMultiplier = Math.pow(0.5, 0.1);

	/**
	 * Calculates the actual amount of modified damage
	 * 
	 * Slight random factor due to rounding
	 * 
	 * Includes all modifiers for armour and resistance
	 * 
	 * @param t
	 *            The thing to be damages
	 * @param dam
	 *            The base (unmodified) amount of damage
	 * @param dt
	 *            The type of damage
	 * @return
	 */
	private static int calculate(final Thing t, double damage, final String dt) {

		if (damage <= 0) {
			return 0;
		}
		final double arm = Armour.calcArmour(t, dt);

		damage = damage * damage / (arm + damage);

		// calculate resistance
		final int res = t.getResistance(dt);
		if (res != 0) {
			// Game.warn("Damage.calculate(): Resistance = "+res);
			damage = damage * Math.pow(resistMultiplier, res);
		}

		if (dt.equals("poison")) {
			if (!t.getFlag("IsLiving")) {
				damage = 0.0;
			}
		}

		if (dt.equals("chill")) {
			if (!t.getFlag("IsLiving")) {
				damage = 0.0;
			}
		}

		int intDamage = RPG.round(damage);

		// item damage is different
		if (t.getFlag("IsItem")) {
			final int max = t.getStat("HPSMAX");
			final int dl = t.getStat("DamageLevels");
			if (dl > 0) {
				if (intDamage < max / (1 + dl)) {
					intDamage = RPG.r(max / (1 + dl)) < intDamage ? max
							/ (1 + dl) : 0;
				}
				// no change
			} else {
				intDamage = RPG.r(max) < intDamage ? max : 0;
			}
		}
		return intDamage;
	}

	public static int inflict(final Thing t, final Integer damageAmount,
			final String damageType) {
		t.getClass();
		damageAmount.getClass();
		damageType.getClass();

		return 0;
	}

	/**
	 * Applay the protection of all wields bags for a damage.
	 * 
	 * <b>Always use a copy of the inventory array because protected equipment
	 * as set to null in the array.</b>
	 **/
	private static void bagProtection(final Thing hero, final Thing[] equip,
			final int dam, final String damtype) {
		for (final int element : RPG.WT_BAGS) {
			final Thing bag = hero.getWielded(element);
			if (bag != null) {
				Damage.inflict(bag, dam, damtype);
				if (!bag.isDead()) {
					// now I clear the array of all the items that was in the
					// bags
					for (int j = 0; j < equip.length; j++) {
						if (equip[j] != null) {
							// set to null the protected items and the bag
							// because damage have already been apply
							if (equip[j] == bag
									|| !hero.isWielded(equip[j])
									&& equip[j].getFlag((String) bag
											.get("Holds"))) {
								equip[j] = null;
							}
						}
					}
				}
			}
		}
	}

	// inflict damage on thing and all sub-items/inventory
	public static void damageInventory(final Thing t, final int dam,
			final String damtype, final int chance) {

		Thing[] inv = t.inv();
		if (t.isHero()) {
			inv = new Thing[inv.length];
			System.arraycopy(t.inv(), 0, inv, 0, inv.length);
			bagProtection(t, inv, dam, damtype);
		}
		for (int i = 0; i < t.invCount(); i++) {

			final Thing it = inv[i];
			if (it != null && RPG.r(100) < chance && it.getFlag("IsItem")) {
				final int itemsInInventory = t.invCount();
				final int res = Damage.inflict(it, dam, damtype);
				if (itemsInInventory > t.invCount()) {
					i--;
				}
				if (t.isHero() && res > 0 && it.place != null) {
					Game.messageTyrant(it.getYourName() + " " + it.is() + " damaged");
				}
			}
		}
	}

	private static void itemDamage(final Thing t, int dam, final String dt) {
		if (!t.hasInventory()) {
			return;
		}

		int idc = 0;
		int arm = 0;
		if (dt.equals("fire") || dt.equals("ice")) {
			idc += 2;
		}

		if (dt.equals("water")) {
			idc += 3;
		}

		if (dt.equals("acid")) {
			idc += 5;
		}

		if (dt.equals(RPG.DT_DISINTEGRATE)) {
			idc += 20;
		}

		idc = RPG.middle(0, idc, 100);

		if (idc > 0 && dam > 0) {
			arm = Armour.calcArmour(t, dt);
			if (arm > 0) {
				final int denom = arm + dam;
				dam = dam * dam / denom;
			}
			// Game.warn("Inventory damage chance="+idc);
			if (dam > 0) {
				damageInventory(t, dam, dt, idc);
			}
		}
	}

	public static String describeState(final Thing t) {
		final double d = t.getStat("HPS") / (double) t.getStat("HPSMAX");
		if (d == 1) {
			return "no";
		}
		if (d > 0.99) {
			return "minimal";
		}
		if (d > 0.9) {
			return "minor";
		}
		if (d > 0.7) {
			return "light";
		}
		if (d > 0.5) {
			return "moderate";
		}
		if (d > 0.25) {
			return "heavy";
		}
		if (d > 0.0) {
			return "critical";
		}
		return "massive";
	}

}
