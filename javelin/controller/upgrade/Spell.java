package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.walker.Walker;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.Potion;
import javelin.model.item.Scroll;
import javelin.model.item.Wand;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;
import javelin.model.unit.Squad;
import javelin.model.world.location.fortification.Shrine;

/**
 * Represents a spell-like ability. See the d20 SRD for more info.
 * 
 * A spell can be manifested in {@link Item} form through several means but
 * should choose only one:
 * 
 * - A {@link Potion} (usually a self spell)
 * 
 * - A {@link Wand} (usually a ranged spell)
 * 
 * - A {@link Scroll} (everything else)
 * 
 * Spells that can be {@link #castinbattle} and {@link #castoutofbattle} cannot
 * have a {@link #components} cost currently.
 * 
 * TODO extract a SpellUpgrade class
 */
public abstract class Spell extends Upgrade implements javelin.model.Cloneable {
	/** Canonical list of all spells by lower-case name. */
	final public static HashMap<String, Spell> SPELLS =
			new HashMap<String, Spell>();

	/** Load spells. */
	static public void init() {
		for (Spell s : UpgradeHandler.singleton.getspells()) {
			SPELLS.put(s.name.toLowerCase(), s);
		}
	}

	/**
	 * Number of times this spell can be cast before needing to rest. A maximum
	 * of 5 is enforced since in the Challenging Challenge Ratings document more
	 * than 5 casts per day represent the same CR.
	 */
	public int perday = 1;
	/** Number of spent uses. */
	public int used = 0;
	/** Set to <code>true</code> if can be cast outside of battle. */
	public boolean castoutofbattle = false;
	/** Set to <code>true</code> if can be used during combat. */
	public boolean castinbattle = false;
	/** Set to <code>true</code> if able to target allies. */
	public boolean castonallies = false;
	/**
	 * Set to <code>true</code> if doesn't need to roll a touch attack.
	 * 
	 * @see #hit(Combatant, Combatant, BattleState)
	 */
	public boolean automatichit = false;
	/** Action point cost to cast this particular spell. */
	/** Rituals are cast by NPCs in Shrines. */
	public boolean isritual = false;
	/**
	 * <code>true</code> if can automatically generate a {@link Scroll} for
	 * this.
	 */
	public boolean isscroll = false;
	/** Same as {@link #isscroll} but for {@link Potion}s. */
	public boolean ispotion = false;
	/** Same as {@link #isscroll} but for {@link Wand}s. */
	public boolean iswand = false;
	/**
	 * If <code>false</code> will not consider this threatening (ignores attacks
	 * of opportunity so it can be cast while engaged without needing a
	 * {@link Skills#concentration} roll). Default: true.
	 */
	public boolean provokeaoo = true;
	/**
	 * How many action points to spend when casting (default: standard action).
	 */
	public float apcost = .5f;
	/** Level this spell is being cast at. */
	public Integer casterlevel;
	/** Spell level. */
	public int level;
	/**
	 * TODO Only ChallengeRatingCalculator and {@link CrFactor} system should
	 * know about this stuff.
	 */
	public float cr;
	/** Material components cost. */
	public int components = 0;
	/** Used to determine where each {@link Scroll} will be sold. */
	final public Realm realm;

	/**
	 * @param name
	 *            Upgrade name.
	 * @param level
	 *            level, from which caster level is calculated.
	 * @param incrementcost
	 *            Challenge rating factor.
	 */
	public Spell(String name, int levelp, float incrementcost, Realm realmp) {
		super(name);
		casterlevel = calculatecasterlevel(levelp);
		cr = incrementcost;
		level = levelp;
		realm = realmp;
	}

	/**
	 * @return the minimum necessary caster level to cast a spell of given spell
	 *         level.
	 */
	public static int calculatecasterlevel(int level) {
		return level == 0 ? 1 : level * 2 - 1;
	}

	@Override
	public String info(Combatant m) {
		return "Currently can cast this " + count(m) + " times before resting";
	}

	@Override
	public boolean apply(Combatant c) {
		int hitdice = c.source.hd.count();
		if (!checkcasterlevel(hitdice, c.source)
				|| c.spells.count() >= hitdice) {
			// design parameters
			return false;
		}
		Spell s = c.spells.has(this);
		if (s == null) {
			s = clone();
			s.name = s.name.replaceAll("Spell: ", "");
			c.spells.add(s);
		} else {
			s.perday += 1;
		}
		c.source.spellcr += s.cr;
		return s.perday <= 5;
	}

	private boolean checkcasterlevel(int hitdice, Monster source) {
		return hitdice >= casterlevel || Skills.take10(source.skills.spellcraft,
				source.intelligence) >= 10 + getspelllevel();
	}

	private int getspelllevel() {
		return (casterlevel + 1) / 2;
	}

	private int count(Combatant source) {
		Spell s = source.spells.has(this);
		return s == null ? 0 : s.perday;
	}

	/**
	 * Once the spell hits, apply effect.
	 * 
	 * @param s
	 *            Should modify this directly (no internal cloning).
	 * @param saved
	 *            <code>true</code> in case the target's saving throw was
	 *            successful.
	 * @return Description of outcome.
	 */
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		throw new RuntimeException("Can't be cast in battle: " + name);
	}

	/**
	 * @param combatant
	 *            Active unit.
	 * @param targets
	 *            Remove from this all unit that should not be considered
	 *            targets.
	 * @param s
	 *            Current game state.
	 */
	public void filtertargets(final Combatant combatant,
			final List<Combatant> targets, BattleState s) {
		final ArrayList<Combatant> iterable = new ArrayList<Combatant>(targets);
		if (castonallies) {
			for (Combatant c : iterable) {
				if (!c.isally(combatant, s)) {
					targets.remove(c);
				}
			}
		} else {
			for (Combatant c : iterable) {
				if (c.isally(combatant, s)
						|| c.source.sr == Integer.MAX_VALUE) {
					targets.remove(c);
				}
			}
		}
	}

	@Override
	public Spell clone() {
		try {
			return (Spell) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * By default is an automatic hit.
	 * 
	 * @return Difficulty class target to be hit on a d20 roll or
	 *         -Integer.MAX_VALUE for automatic hit.
	 */
	public int hit(Combatant active, Combatant target, BattleState state) {
		return -Integer.MAX_VALUE;
	}

	/**
	 * @return <code>true</code> if this spell has been cast to it's limit
	 *         already before it's {@link Combatant} caster rests.
	 */
	public boolean exhausted() {
		if (Javelin.DEBUG) {
			assert used <= perday;
		}
		return used == perday;
	}

	@Override
	public String toString() {
		return name + showleft();
	}

	/**
	 * " (4/5)" - where 4 is the times this still can be cast before resting and
	 * 5 is the total {@link #perday} uses.
	 * 
	 * @return A description in the format above.
	 */
	public String showleft() {
		return " (" + (perday - used) + "/" + perday + ")";
	}

	/**
	 * @return {@link Integer#MAX_VALUE} if it's an impossible roll; negative
	 *         {@link Integer#MAX_VALUE} in case of an automatic success; or the
	 *         minimum number needed to roll on a d20 for a successful saving
	 *         throw. With the exceptions above numbers will be approximated
	 *         into the range: ]2,19] to allow the ensuing roll of 1 to always
	 *         be an automatic miss and 20 an automatic hit.
	 * 
	 * @see #save(int, int, Combatant)
	 */
	public int save(Combatant caster, Combatant target) {
		return -Integer.MAX_VALUE;
	}

	/**
	 * @param caster
	 *            Can be <code>null</code> if being cast from a {@link Shrine}.
	 * @param target
	 *            May be <code>null</code> if no targetting is necessary.
	 * @return A message to be displayed or <code>null</code> if feedback is
	 *         handled internally.
	 * @throws NotPeaceful
	 *             if should not be used out of combat.
	 * 
	 * @see #isritual
	 */
	public String castpeacefully(Combatant caster, Combatant target) {
		throw new RuntimeException("Can't be cast peacefully: " + name);
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(getClass());
	}

	/**
	 * Helper method for {@link #filtertargets(Combatant, List, BattleState)}.
	 * 
	 * @param combatant
	 *            Will clear the given targets and include only this one (self).
	 */
	static public void targetself(final Combatant combatant,
			final List<Combatant> targets) {
		targets.clear();
		targets.add(combatant);
	}

	/**
	 * @param spell
	 *            Used for {@link Monster#sr} verification.
	 * @return Combatant in radius distance using the target as a point of
	 *         reference.
	 */
	public static List<Combatant> getradius(Combatant target, int radius,
			Spell spell, BattleState state) {
		ArrayList<Combatant> targets = new ArrayList<Combatant>();
		for (Combatant c : state.getcombatants()) {
			if (Walker.distance(c, target) <= radius
					&& 10 + spell.casterlevel >= c.source.sr) {
				targets.add(c);
			}
		}
		return targets;
	}

	/**
	 * Takes 10.
	 * 
	 * @param savingthrow
	 *            Value for the defending unit's saving throw bonus (fortitude,
	 *            reflexes or will).
	 * @param caster
	 *            Attacking spellcaster.
	 * @return Target number that needs to be hit on a d20 roll for the defender
	 *         to succesfully save against this spell or negative
	 *         {@link Integer#MAX_VALUE} If the monster is immune to this type
	 *         of save.
	 */
	public int rollsave(final int savingthrow, final Combatant caster) {
		return savingthrow == Integer.MAX_VALUE ? -Integer.MAX_VALUE
				: 10 + level + Monster.getbonus(caster.source.charisma)
						- savingthrow;
	}

	/**
	 * @param caster
	 *            Spellcaster.
	 * @return <code>true</code> if this is allowed to be cast.
	 */
	public boolean canbecast(Combatant caster) {
		return !exhausted();
	}

	/**
	 * Event invoked once per {@link Spell} class after all {@link Monster}s
	 * have been loaded.
	 */
	public void postloadmonsters() {
		// does nothing by default
	}

	/**
	 * TODO offer error message as return instead.
	 * 
	 * @param target
	 *            May be <code>null</code> if this spell doesn't need to target
	 *            someone.
	 * @return <code>false</code> if for some reason this spell shouldn't be
	 *         cast.
	 */
	public boolean validate(Combatant caster, Combatant target) {
		return true;
	}

	/**
	 * Helper method to select a target automatically if needed.
	 * 
	 * @see #castonallies
	 * @see Javelin#choose(String, List, boolean, boolean)
	 * @see #castpeacefully(Combatant, Combatant)
	 */
	public boolean castpeacefully(Combatant c) {
		Combatant target = null;
		if (castonallies) {
			int targeti = Javelin.choose("Cast on which ally?",
					Squad.active.members, true, false);
			if (targeti == -1) {
				return false;
			}
			target = Squad.active.members.get(targeti);
		}
		castpeacefully(c, target);
		return true;
	}

	/**
	 * This is called if this spell is being used as an {@link Attack#effect}.
	 */
	public void setdamageeffect() {
		casterlevel = null;// prevents dispel
	}
}
