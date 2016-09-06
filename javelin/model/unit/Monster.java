/*
 * DefaultMonster.java
 *
 * Created on 21 February 2003, 06:05
 */
package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.SpellbookGenerator;
import javelin.controller.Weather;
import javelin.controller.action.Breath;
import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseIntelligence;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.controller.upgrade.feat.MeleeFocus;
import javelin.model.Cloneable;
import javelin.model.feat.Feat;
import javelin.model.item.Scroll;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.artifact.Slot;
import javelin.model.spell.enchantment.compulsion.HoldMonster;
import javelin.model.spell.necromancy.Poison;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.Spells;
import javelin.model.unit.abilities.TouchAttack;
import javelin.model.world.location.town.Town;
import javelin.view.screen.upgrading.SkillSelectionScreen;
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
	public static String[] SIZES = { "fine", "diminutive", "tiny", "small",
			"medium-size", "large", "huge", "gargantuan", "colossal" };

	/**
	 * Map of {@link Terrain} types, mapped by {@link Monster#name}.
	 * 
	 * @deprecated
	 * @see #getterrains()
	 */
	@Deprecated
	public static final HashMap<String, List<String>> TERRAINDATA =
			new HashMap<String, List<String>>();

	public int strength = -1;
	public int dexterity = -1;
	/** May be 0. */
	public int constitution = -1;
	/** Maybe be 0. */
	public int intelligence = -1;
	public int wisdom = -1;
	public int charisma = -1;

	/**
	 * See {@link #fortitude()}
	 */
	@Deprecated
	public int fort;
	public int ref;
	/**
	 * @deprecated See {@link #will()})
	 */
	@Deprecated
	public int will;

	/**
	 * 5 units = 1 square. A unit is able to move this number of squares as a
	 * move-equivalent action (.5 action points).
	 * 
	 * @see #fly
	 */
	public int walk = 0;
	/**
	 * Flying allows an unit to ignore water and obstacles. A flying unit should
	 * have {@link #walk} 0.
	 * 
	 * TODO also offer perfect flight, which could at least charge through
	 * obstacles.
	 * 
	 * @see #walk
	 */
	public int fly = 0;
	/**
	 * Burrow allows a creature to submerge into the earth. Normally they would
	 * be able to descend to any depth but due to AI and complexity concerns
	 * burrowing right is more restrict. Read "combatmodifiers.txt".
	 * 
	 * Burrowing and surfacing cost the equivalent of a single square movement
	 * unless engaged.
	 */
	public int burrow = 0;

	/**
	 * A swimming creature is able to ignore water penalties and charge through
	 * flooded squares.
	 * 
	 * @see #walk
	 */
	public int swim = 0;

	/**
	 * Use {@link Combatant#ac() instead.}
	 */
	public int ac;
	/** The portion of {@link #ac} due to armor protection. */
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
	public Spells spells = new Spells();
	/** @see TouchAttack */
	public TouchAttack touch = null;

	public int initiative = Integer.MIN_VALUE;
	/** Monster name as found in the d20 SRD. */
	public String name = null;
	public int size = -1;
	/** Subgroup of {@link #type}, merely descriptive. */
	public String group;
	/** Cache for {@link ChallengeRatingCalculator}. */
	public Float challengeRating = null;
	public String type;
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
	 * {@link SpellsFactor} instead of taking the {@link Combatant} into
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
	 * attacks like melee, ranged and charge. A value of
	 * {@link Integer#MAX_VALUE} means energy immunity.
	 */
	public int energyresistance = 0;
	/**
	 * Spell resistance.
	 * 
	 * "To affect a creature that has spell resistance, a spellcaster must make
	 * a caster level check (1d20 + caster level) at least equal to the
	 * creature’s spell resistance."
	 */
	public int sr = 0;
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
	public Skills skills = new Skills(this);
	/** Creatures that should only be spawned at night or underground. */
	public boolean nightonly = false;
	/** Immunity to critical hits. */
	public boolean immunitytocritical = false;
	/** Immune to the effects of {@link Poison}. */
	public boolean immunitytopoison = false;
	/** If <code>true</code> cannot be affected by {@link HoldMonster}. */
	public boolean immunitytoparalysis = false;
	/** Immune to mind-affecting effects. */
	public boolean immunitytomind = false;

	/**
	 * Temporary {@link Monster#constitution} damage. 1 poison = -2 temporary
	 * constitution score.
	 */
	public int poison = 0;
	/** Unspent skill points. */
	public int skillpool = 0;

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
			skills = skills.clone(m);
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

	/**
	 * Rolls a d20 to determine a saving throw result. Note that for being
	 * random this can only be used outside of {@link BattleAi} thinking.
	 * 
	 * @param bonus
	 *            {@link #fortitude()}, {@link #will()} or {@link #ref} bonus. A
	 *            value of {@link Integer#MAX_VALUE} represents automatic
	 *            success.
	 * @param dc
	 *            Difficulty class target for the saving roll.
	 * @return <code>true</code> on save success.
	 */
	public boolean save(int bonus, int dc) {
		if (bonus == Integer.MAX_VALUE) {
			return true;
		}
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
			classesbab += classdata.table[classdata.getlevel(this)].bab;
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
	 * @return
	 * @throws Death
	 *             In case of zero or negative constitution.
	 * @return <code>false</code> if this combatant has been killed during this
	 *         operation.
	 */
	public boolean raiseconstitution(Combatant c, int bonus) {
		if (constitution == 0 || bonus == 0) {
			return true;
		}
		constitution += bonus * 2;
		while (constitution <= 0) {
			constitution += 2;
			bonus += 2;
		}
		fort += bonus;
		final int hds = hd.count();
		int bonushp = hds * bonus;
		if (c.maxhp + bonushp < hds) {
			bonushp = -(c.maxhp - hds);
		}
		hd.extrahp += bonushp;
		c.hp += bonushp;
		if (c.hp < 1) {
			c.hp = 1;
		}
		c.maxhp += bonushp;
		if (c.hp > c.maxhp) {
			c.hp = c.maxhp;
		}
		for (BreathWeapon breath : breaths) {
			breath.savedc += bonus;
		}
		return true;
	}

	public int gettopspeed() {
		return walk == 0 ? fly : walk;
	}

	/**
	 * TODO turn attributes into {@link Enum}.
	 */
	static public int getbonus(int attribute) {
		return new Long(Math.round(Math.floor(attribute / 2.0 - 5.0)))
				.intValue();
	}

	@Override
	public String toString() {
		return customName == null ? name : customName;
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

	/**
	 * @return {@link Integer#MAX_VALUE} if immune, will saving throw bonus
	 *         otherwise.
	 */
	public int will() {
		return immunitytomind ? Integer.MAX_VALUE : will;
	}

	public static String getsignedbonus(int score) {
		int bonus = Monster.getbonus(score);
		return bonus >= 0 ? "+" + bonus : Integer.toString(bonus);
	}

	public void setwill(int willp) {
		will = willp;
	}

	public void addwill(int delta) {
		will += delta;
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
		if (intelligence > 0) {
			intelligence += points;
		}
	}

	/**
	 * @return A scale in which 1 unit equals to half a gold piece (1
	 *         medium-size monster).
	 * @see Squad#eat()
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

	/**
	 * @return {@link Integer#MAX_VALUE} if immune or the fortitude saving throw
	 *         bonus.
	 */
	public int fortitude() {
		return constitution == 0 ? Integer.MAX_VALUE : fort;
	}

	/**
	 * @return <code>true</code> if can read a {@link Spell} from a
	 *         {@link Scroll}.
	 */
	public boolean read(Scroll s) {
		if (skills.usemagicdevice() >= 10 + s.spell.casterlevel) {
			return true;
		}
		return skills.decipher(s.spell) && 10 + hd.count()
				+ skills.spellcraft / 2 >= s.spell.casterlevel + 1;
	}

	/**
	 * @param minimum
	 *            Minimum intelligence bonus required. If <code>null</code> will
	 *            assume -1 or higher is fine (primitive intelligence).
	 * @return <code>true</code> if able to communicate and think reasonably
	 *         well. Being able to understand and follow simple, general tasks
	 *         should do (which is not the case for animals that can only follow
	 *         a few specific, well-trained tasks).
	 */
	public boolean think(Integer minimum) {
		return Monster
				.getbonus(intelligence) >= (minimum == null ? -1 : minimum);
	}

	/**
	 * @return a -0, -2 or -4 skill check penalty depending on time of day and
	 *         {@link Monster} vision.
	 * 
	 * @see Javelin#getDayPeriod()
	 * @see #vision
	 * @see Combatant#perceive(String)
	 * @see Combatant#view(String)
	 * @see Skills#perceive(Monster, boolean)
	 */
	public int view() {
		if (vision == VISION_DARK) {
			return 0;
		}
		String period = Javelin.getDayPeriod();
		boolean verydark = period == Javelin.PERIODNIGHT
				|| Weather.current == Weather.STORM;
		if (vision == VISION_LOWLIGHT) {
			if (verydark) {
				return -2;
			}
		} else { // normal vision
			if (period == Javelin.PERIODEVENING
					|| Weather.current == Weather.RAIN) {
				return -2;
			}
			if (verydark) {
				return -4;
			}
		}
		return 0;
	}

	/**
	 * Call this to spend {@link #skillpool}.
	 * 
	 * @param u
	 *            If <code>null</code> will use all available classes to
	 *            determine class skills.
	 * @return You can either call {@link SkillSelectionScreen#show()} for user
	 *         input or {@link SkillSelectionScreen#upgradeautomatically()} for
	 *         no user input.
	 * 
	 * @see ClassAdvancement
	 * @see RaiseIntelligence
	 * @see #skills
	 */
	public SkillSelectionScreen purchaseskills(Upgrade u) {
		return new SkillSelectionScreen(this, u);
	}

	/**
	 * @return All terrain types this monster can be found in. May be empty if
	 *         this is an internal Javelin monster (like {@link Town} Workers).
	 * @see Terrain#toString()
	 */
	public List<String> getterrains() {
		return Monster.TERRAINDATA.get(name);
	}

	/**
	 * @return <code>true</code> if the monster can swim on (or fly above)
	 *         water.
	 */
	public int swim() {
		return Math.max(swim, fly);
	}
}
