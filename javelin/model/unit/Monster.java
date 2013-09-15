/*
 * DefaultMonster.java
 *
 * Created on 21 February 2003, 06:05
 */
package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.controller.upgrade.feat.MeleeFocus;
import javelin.model.feat.Feat;
import javelin.model.feat.WeaponFinesse;
import tyrant.mikera.engine.RPG;

/**
 * A Monster represents a stat-block (monster information) from the monster
 * manual. Different in-game battle units can be linked to the same stat block.
 * A player character creates it's own stat-block which is updated as he levels
 * up.
 * 
 * For performance, Monsters are not to be cloned during BattleNode replication.
 * A monster should only be cloned and modified during AI thinking in the case a
 * spell or ability changes that specific unit's stats (cat's grace or level
 * drain, for example).
 * 
 * @author Brian Voon Yee Yap
 * @author alex
 */
public class Monster implements Cloneable, Serializable {
	public static final int VISION_LOWLIGHT = 1;
	public static final int VISION_DARK = 2;

	public static final int FINE = 0;
	public static final int DIMINUTIVE = 1;
	public static final int TINY = 2;
	public static final int SMALL = 3;
	public static final int MEDIUM = 4;
	public static final int LARGE = 5;
	public static final int HUGE = 6;
	public static final int GARGANTUAN = 7;
	public static final int COLOSSAL = 8;

	/** An array of all sizes valid in this class. */
	public static String SIZES[] = { "fine", "diminutive", "tiny", "small",
			"medium-size", "large", "huge", "gargantuan", "colossal" };

	public int strength = -1;
	public int dexterity = -1;
	public int constitution = -1;
	public int intelligence = -1;
	public int wisdom = -1;
	public int charisma = -1;

	public int fort;
	public int ref;
	/**
	 * @deprecated See #will()
	 */
	@Deprecated
	public int will;

	public int walk = 0;
	public int fly = 0;
	public int swim = 0;

	/**
	 * Use {@link Combatant#ac() instead.}
	 */
	public int ac;
	public int armor;

	public ArrayList<AttackSequence> melee = new ArrayList<AttackSequence>();
	public ArrayList<AttackSequence> ranged = new ArrayList<AttackSequence>();

	public List<Feat> feats = new ArrayList<Feat>();

	public int initiative = Integer.MIN_VALUE;
	public String name = null;
	public int size = -1;
	/**
	 * TODO use
	 */
	public String group;
	public float challengeRating;
	public HD hd = new HD();
	public String monsterType;
	/**
	 * TODO use only {@link #avatarfile}
	 */
	public String avatar = null;
	public String customName = null;
	public int meleedamageupgrades = 0;
	public int fasthealing = 0;
	public int rangeddamageupgrades = 0;
	public int warrior = 0;
	public int aristocrat = 0;
	public int expert = 0;
	public int commoner = 0;
	public String avatarfile = null;
	public int vision = 0;
	public float originalhd;
	public float spellcr = 0;
	public ArrayList<BreathWeapon> breaths = new ArrayList<BreathWeapon>();
	/**
	 * Damage reduction.
	 */
	public int dr = 0;
	/**
	 * Energy resistance. Currently applied to everything that is not a physical
	 * attacks like melee, ranged and charge.
	 */
	public int resistance = 0;
	/**
	 * Spell resistance.
	 */
	public int sr = 0;

	@Override
	public Monster clone() {
		try {
			final Monster m = (Monster) super.clone();
			m.melee = copyattacks(melee);
			m.ranged = copyattacks(ranged);
			m.feats = new ArrayList<Feat>(m.feats);
			m.hd = hd.clone();
			m.breaths = (ArrayList<BreathWeapon>) breaths.clone();
			return m;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	static private ArrayList<AttackSequence> copyattacks(
			final List<AttackSequence> original) {
		final ArrayList<AttackSequence> copy = new ArrayList<AttackSequence>(
				original.size());
		for (final AttackSequence sequence : original) {
			copy.add(sequence.clone());
		}
		return copy;
	}

	public void addfeat(final Feat feat) {
		feats.add(feat);
	}

	public int hasfeat(final Feat f) {
		int i = 0;
		for (final Feat existing : feats) {
			if (existing.name.equals(f.name)) {
				i += 1;
			}
		}
		return i;
	}

	public long countfeats() {
		return feats.size();
	}

	public boolean save(int bonus, int dc) {
		int roll = RPG.r(1, 20);
		if (roll == 1) {
			return false;
		}
		if (roll == 20) {
			return true;
		}
		return roll + bonus >= dc;
	}

	public int getbaseattackbonus() {
		int classesbab = 0;
		for (ClassAdvancement classdata : ClassAdvancement.CLASSES) {
			classesbab += classdata.gettable()[classdata.getlevel(this)].bab;
		}
		return classesbab
				+ new Long(Math.round(originalhd
						* MeleeFocus.bab.get(monsterType))).intValue();
	}

	// public boolean savewill(int dc) {
	// if (intelligence == 0) {
	// return true;
	// }
	// return save(will, dc);
	// }

	public void raisedexterity(int x) {
		ac += 1 * x;
		ref += 1 * x;
		for (List<Attack> attacks : ranged) {
			for (Attack a : attacks) {
				a.bonus += 1 * x;
			}
		}
		initiative += 1 * x;
	}

	public boolean raisestrength() {
		if (hasfeat(WeaponFinesse.singleton) != 0) {
			return false;
		}
		for (AttackSequence sequence : melee) {
			for (Attack a : sequence) {
				a.bonus += 1;
				a.damage[2] += 1;
			}
		}
		return true;
	}

	public void raiseconstitution(Combatant c) {
		fort += 1;
		int bonushp = hd.countdice();
		hd.extrahp += bonushp;
		c.hp += bonushp;
		c.maxhp += bonushp;
		for (BreathWeapon breath : breaths) {
			breath.savedc += 1;
		}
	}

	public int gettopspeed() {
		return walk == 0 ? fly : walk;
	}

	static public int getbonus(int attribute) {
		return new Long(Math.round(Math.floor(attribute / 2.0 - 5.0)))
				.intValue();
	}

	@Override
	public String toString() {
		return customName == null ? name : customName;
	}

	public boolean swim() {
		return swim > 0 || fly > 0;
	}

	public void raisewisdom() {
		will += 1;
	}

	public int will() {
		return intelligence == 0 ? Integer.MAX_VALUE : will;
	}
}
