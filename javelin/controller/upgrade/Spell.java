package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.spell.Blink;
import javelin.model.spell.DayLight;
import javelin.model.spell.DeeperDarkness;
import javelin.model.spell.DominateMonster;
import javelin.model.spell.Doom;
import javelin.model.spell.Heroism;
import javelin.model.spell.HoldMonster;
import javelin.model.spell.SlayLiving;
import javelin.model.spell.Summon;
import javelin.model.spell.totem.BearsEndurance;
import javelin.model.spell.totem.BullsStrength;
import javelin.model.spell.totem.CatsGrace;
import javelin.model.spell.totem.OwlsWisdom;
import javelin.model.spell.wounds.CureCriticalWounds;
import javelin.model.spell.wounds.CureLightWounds;
import javelin.model.spell.wounds.CureModerateWounds;
import javelin.model.spell.wounds.CureSeriousWounds;
import javelin.model.spell.wounds.InflictCriticalWounds;
import javelin.model.spell.wounds.InflictLightWounds;
import javelin.model.spell.wounds.InflictModerateWounds;
import javelin.model.spell.wounds.InflictSeriousWounds;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Represents a spell-like ability.
 * 
 * See the d20 SRD for more info.
 */
public abstract class Spell extends Upgrade implements javelin.model.Cloneable {
	final public static HashMap<String, Spell> SPELLS =
			new HashMap<String, Spell>();

	static {
		for (Spell s : new Spell[] { new CureLightWounds(),
				new CureModerateWounds(), new CureSeriousWounds(),
				new CureCriticalWounds(), new CatsGrace(), new BullsStrength(),
				new BearsEndurance(), new OwlsWisdom(), new Heroism(),
				new HoldMonster(), new DominateMonster(), new DayLight(),
				new DeeperDarkness(), new InflictLightWounds(),
				new InflictModerateWounds(), new InflictSeriousWounds(),
				new InflictCriticalWounds(), new SlayLiving(), new Blink(),
				new Doom(), new Summon("Dretch", 1),
				new Summon("Gray slaad", 1) }) {
			SPELLS.put(s.name.toLowerCase(), s);
		}
	}

	public int perday = 1;
	public int used = 0;
	/**
	 * TODO Only ChallengeRatingCalculator and {@link CrFactor} system should
	 * know about this stuff.
	 */
	public float cr;
	public final boolean ispeaceful;
	public int casterlevel;
	public final boolean friendly;

	/**
	 * @param ispeacefulp
	 *            <code>true</code> if it can be used outside combat.
	 * @param Spell
	 *            level, from which caster level is calculated.
	 */
	public Spell(String name, float incrementcost, boolean ispeacefulp,
			int level, boolean friendlyp) {
		super(name);
		cr = incrementcost;
		ispeaceful = ispeacefulp;
		casterlevel = calculatecasterlevel(level);
		friendly = friendlyp;
	}

	public static int calculatecasterlevel(int level) {
		return level == 0 ? 1 : level * 2 - 1;
	}

	@Override
	public String info(Combatant m) {
		return "Currently can cast this " + count(m) + " times before resting";
	}

	@Override
	public boolean apply(Combatant m) {
		int hitdice = m.source.hd.count();
		if (casterlevel > hitdice || m.spells.size() >= hitdice) {
			// design parameters
			return false;
		}
		Spell s = m.spells.has(this);
		if (s == null) {
			s = clone();
			s.name = s.name.replaceAll("Spell: ", "");
			m.spells.add(s);
		} else {
			s.perday += 1;
		}
		m.source.spellcr += s.cr;
		return s.perday <= 5;
	}

	private int count(Combatant source) {
		Spell s = source.spells.has(this);
		return s == null ? 0 : s.perday;
	}

	@Override
	public boolean isstackable() {
		return true;
	}

	/**
	 * @return Chance of missing targetting the spell from 0 to 1 or
	 *         -Integer.MAX_VALUE for automatic hit.
	 */
	abstract public int calculatetouchdc(Combatant combatant,
			Combatant targetCombatant, BattleState s);

	/**
	 * @param s
	 *            Has already been cloned, along with the Combatant parameters.
	 * @param saved
	 *            <code>true</code> in case the target's saving throw was
	 *            successful.
	 * @return Description of outcome.
	 */
	abstract public String cast(Combatant caster, Combatant target,
			BattleState s, boolean saved);

	public void filtertargets(final Combatant combatant,
			final List<Combatant> targets, BattleState s) {
		final ArrayList<Combatant> iterable = new ArrayList<Combatant>(targets);
		if (friendly) {
			for (Combatant c : iterable) {
				if (!c.isAlly(combatant, s)) {
					targets.remove(c);
				}
			}
		} else {
			for (Combatant c : iterable) {
				if (c.isAlly(combatant, s)
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

	public String getName() {
		return name.replaceAll("Spell: ", "");
	}

	public double apcost() {
		return .5;
	}

	/**
	 * TODO refactor so only this is needed instead of having
	 * {@link #calculatetouchdc(Combatant, Combatant, BattleState)} too
	 * 
	 * @return Descriptive chance of spell being succesful.
	 */
	abstract public int calculatehitdc(Combatant active, Combatant target,
			BattleState state);

	public boolean exhausted() {
		assert used <= perday;
		return used == perday;
	}

	@Override
	public String toString() {
		return name + showleft();
	}

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
	 */
	public abstract int calculatesavetarget(Combatant caster, Combatant target);

	/**
	 * @throws NotPeaceful
	 *             if should not be used out of combat.
	 */
	public abstract String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful;

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Spell && ((Spell) obj).name.equals(name);
	}

	static public void targetself(final Combatant combatant,
			final List<Combatant> targets) {
		targets.clear();
		targets.add(combatant);
	}

	static public int save(final int spelllevel, final int savingthrow,
			final Combatant caster) {
		return savingthrow == Integer.MAX_VALUE ? Integer.MAX_VALUE
				: 10 + spelllevel + Monster.getbonus(caster.source.charisma)
						- savingthrow;
	}

	public boolean canbecast(Combatant caster) {
		return !exhausted();
	}

	public void postloadmonsters() {
		// does nothing by default
	}
}
