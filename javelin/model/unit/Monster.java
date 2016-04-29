/*
 * DefaultMonster.java
 *
 * Created on 21 February 2003, 06:05
 */
package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.SpellbookGenerator;
import javelin.controller.action.Breath;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.controller.upgrade.feat.MeleeFocus;
import javelin.model.Cloneable;
import javelin.model.feat.Feat;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.artifact.Slot;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.TouchAttack;
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
	/** TODO make enumeration */
	public static final int VISION_LOWLIGHT = 1;
	/** TODO make enumeration */
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
	private int will;

	public int walk = 0;
	/** TODO also offer perfect flight */
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
	public HD hd = new HD();
	/**
	 * @see Breath
	 */
	public CloneableList<BreathWeapon> breaths =
			new CloneableList<BreathWeapon>();
	/**
	 * This is the spells that this source has by default. Learned spells go to
	 * {@link Combatant#spells}.
	 * 
	 * TOOD this could be removed and better designed.
	 * 
	 * @see Monster#spellcr
	 * @see Spell
	 */
	public ArrayList<Spell> spells = new ArrayList<Spell>();
	/**
	 * @see TouchAttack
	 */
	public TouchAttack touch = null;

	public int initiative = Integer.MIN_VALUE;
	public String name = null;
	public int size = -1;
	/**
	 * TODO use
	 */
	public String group;
	public Float challengeRating = null;
	public String type;
	/**
	 * TODO use only {@link #avatarfile}
	 */
	public String avatar = null;
	/** TODO should probably be a Combatant#name */
	public String customName = null;
	public int meleedamageupgrades = 0;
	public int fasthealing = 0;
	public int rangeddamageupgrades = 0;
	public int warrior = 0;
	public int aristocrat = 0;
	public int expert = 0;
	public int commoner = 0;
	public String avatarfile = null;
	/**
	 * What type of vision perception the monster has.
	 * 
	 * @see #VISION_LOWLIGHT
	 * @see #VISION_DARK
	 */
	public int vision = 0;
	public float originalhd;
	/**
	 * Used to distribute random spells to a new {@link Combatant}.
	 * 
	 * TODO {@link ChallengeRatingCalculator} is using this for
	 * {@link SpellsFactor} instead ot taking the {@link Combatant} into
	 * consideration. Maintain?
	 * 
	 * @see SpellbookGenerator
	 * 
	 */
	public float spellcr = 0;
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
	 * 
	 * "To affect a creature that has spell resistance, a spellcaster must make
	 * a caster level check (1d20 + caster level) at least equal to the
	 * creature’s spell resistance."
	 */
	public int sr = 0;
	public boolean immunetomindeffects = false;
	public ArrayList<String> terrains = new ArrayList<String>();
	/**
	 * Alignment. <code>true</code> if lawful, <code>null</code> if neutral,
	 * <code>false</code> if chaotic.
	 */
	public Boolean lawful = null;
	/**
	 * Alignment. <code>true</code> if good, <code>null</code> if neutral,
	 * <code>false</code> if evil.
	 */
	public Boolean good = null;
	/**
	 * A monster is humanoid if it can use most of it's {@link Slot}s to wear
	 * {@link Artifact}s. Humanoid and monstrous humanoid monster types are
	 * humanoid by default, other types have to manually marked as
	 * Humanoid="yes" in monster.xml to be marked as such.
	 * 
	 * Ideally this would be defined in a slot-by-slot and monster-by-monster
	 * basis but for now a creature tagged as humanoid is eligible to use any
	 * and all {@link Slot}s.
	 */
	public boolean humanoid = false;
	/** See {@link Skills}. */
	public Skills skills = new Skills();

	@Override
	public Monster clone() {
		try {
			final Monster m = (Monster) super.clone();
			m.melee = copyattacks(melee);
			m.ranged = copyattacks(ranged);
			m.feats = new ArrayList<Feat>(m.feats);
			m.hd = hd.clone();
			m.breaths = breaths.clone();
			if (m.touch != null) {
				m.touch = touch.clone();
			}
			terrains = (ArrayList<String>) terrains.clone();
			skills = skills.clone();
			return m;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	static private ArrayList<AttackSequence>
			copyattacks(final List<AttackSequence> original) {
		final ArrayList<AttackSequence> copy =
				new ArrayList<AttackSequence>(original.size());
		for (final AttackSequence sequence : original) {
			copy.add(sequence.clone());
		}
		return copy;
	}

	public void addfeat(final Feat feat) {
		feats.add(feat);
	}

	public int countfeat(final Feat f) {
		int i = 0;
		for (final Feat existing : feats) {
			if (existing.name.equals(f.name)) {
				i += 1;
			}
		}
		return i;
	}

	public boolean hasfeat(final Feat f) {
		for (final Feat existing : feats) {
			if (existing.name.equals(f.name)) {
				return true;
			}
		}
		return false;
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

	/**
	 * @return 0 or higher value.
	 */
	public int getbaseattackbonus() {
		int classesbab = 0;
		for (ClassAdvancement classdata : ClassAdvancement.CLASSES) {
			classesbab += classdata.gettable()[classdata.getlevel(this)].bab;
		}
		return classesbab
				+ new Long(Math.round(originalhd * MeleeFocus.bab.get(type)))
						.intValue();
	}

	/**
	 * @param bonus
	 *            Raises {@link #dexterity} bonus by +1 this many times (+1
	 *            bonus = +2 ability score points).
	 */
	public void raisedexterity(int bonus) {
		dexterity += 2 * bonus;
		ac += bonus;
		ref += bonus;
		for (List<Attack> attacks : ranged) {
			for (Attack a : attacks) {
				a.bonus += bonus;
			}
		}
		initiative += bonus;
	}

	/**
	 * @param bonus
	 *            Raises {@link #strength} bonus by this many steps (+1 bonus =
	 *            +2 score points)
	 */
	public void raisestrength(int bonus) {
		strength += 2 * bonus;
		for (AttackSequence sequence : melee) {
			for (Attack a : sequence) {
				a.bonus += bonus;
				a.damage[2] += bonus;
			}
		}
	}

	/**
	 * @param c
	 *            Raises {@link #constitution} by +2...
	 * @param bonus
	 *            multiplied by this magnitude. Negative values allowed.
	 */
	public void raiseconstitution(Combatant c, int bonus) {
		c.source.constitution += bonus * 2;
		fort += bonus;
		int bonushp = hd.count() * bonus;
		hd.extrahp += bonushp;
		c.hp += bonushp;
		c.maxhp += bonushp;
		for (BreathWeapon breath : breaths) {
			breath.savedc += bonus;
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

	/**
	 * @param score
	 *            Raises {@link #wisdom} by this many ability score points (+2
	 *            point=+1 bonus modifier).
	 */
	public void raisewisdom(int score) {
		wisdom += score;
		will += score / 2;
	}

	public int will() {
		return intelligence == 0 || immunetomindeffects ? Integer.MAX_VALUE
				: will;
	}

	public static String getsignedbonus(int score) {
		int bonus = Monster.getbonus(score);
		return bonus >= 0 ? "+" + bonus : Integer.toString(bonus);
	}

	public void setWill(int willp) {
		will = willp;
	}

	public void addwill(int delta) {
		will += delta;
	}

	public Integer willraw() {
		return will;
	}

	@Override
	public boolean equals(Object obj) {
		Monster m = (Monster) obj;
		return name.equals(m.name);
	}

	/**
	 * @param bonus
	 *            Raises {@link #charisma} by this many points (+2 points = +1
	 *            bonus modifier)
	 */
	public void raisecharisma(int bonus) {
		charisma += bonus * 2;
	}

	/**
	 * @param points
	 *            Raises {@link #intelligence} by this many ability score points
	 *            (+2 point = +1 bonus modifier).
	 */
	public void raiseintelligence(int points) {
		intelligence += points;
	}

	/**
	 * @return Each unit hers is equivalent to 5 silver pieces daily upkeep
	 *         ($0.5, or half a gold piece).
	 */
	public float eat() {
		switch (size) {
		case Monster.FINE:
			return 1 / 16f;
		case Monster.DIMINUTIVE:
			return 1 / 8f;
		case Monster.TINY:
			return 1 / 4f;
		case Monster.SMALL:
			return 1 / 2f;
		case Monster.MEDIUM:
			return 1;
		case Monster.LARGE:
			return 2;
		case Monster.HUGE:
			return 4;
		case Monster.GARGANTUAN:
			return 8;
		case Monster.COLOSSAL:
			return 16;
		default:
			throw new RuntimeException("Unknown size " + size);
		}
	}
}
