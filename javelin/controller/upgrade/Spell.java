package javelin.controller.upgrade;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.exception.NotPeaceful;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public abstract class Spell extends Upgrade implements Cloneable {
	public int perday = 1;
	public int used = 0;
	/**
	 * TODO Only ChallengeRatingCalculator and {@link CrFactor} system should
	 * know about this stuff.
	 */
	final public float cr;
	public final boolean ispeaceful;
	public final int casterlevel;
	public final boolean friendly;

	public Spell(String name, float incrementcost, boolean ispeacefulp,
			int casterlevelp, boolean friendlyp) {
		super(name);
		cr = incrementcost;
		ispeaceful = ispeacefulp;
		casterlevel = casterlevelp;
		friendly = friendlyp;
	}

	@Override
	public String info(Combatant m) {
		return "Currently can cast this " + count(m) + " times before resting";
	}

	@Override
	public boolean apply(Combatant m) {
		Spell me = m.spells.has(this);
		if (me == null) {
			Spell bought = clone();
			bought.name = bought.name.replaceAll("Spell: ", "");
			m.spells.add(bought);
			m.source.spellcr += cr;
			return true;
		}
		if (me.perday >= 5) {
			return false;
		}
		me.perday += 1;
		m.source.spellcr += cr;
		return true;
	}

	private int count(Combatant source) {
		Spell me = source.spells.has(this);
		return me == null ? 0 : me.perday;
	}

	@Override
	public boolean isstackable() {
		return true;
	}

	abstract public int calculatetouchdc(Combatant combatant,
			Combatant targetCombatant, BattleState s);

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
				if (c.isAlly(combatant, s) || c.source.sr == Integer.MAX_VALUE) {
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
	 * @return {@link Float#MAX_VALUE} if it's an impossible roll; negative
	 *         {@link Float#MAX_VALUE} in case of an automatic success; or the
	 *         minimum number needed to roll on a d20 for a successful saving
	 *         throw. With the exceptions above numbers will be approximated
	 *         into the range: ]2,19] to allow the ensuing roll of 1 to always
	 *         be an automatic miss and 20 an automatic hit.
	 */
	public abstract int calculatesavetarget(Combatant caster, Combatant target);

	public abstract String castpeacefully(Combatant caster, Combatant combatant)
			throws NotPeaceful;

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Spell && ((Spell) obj).name.equals(name);
	}

	static public void self(final Combatant combatant,
			final List<Combatant> targets) {
		targets.clear();
		targets.add(combatant);
	}

	static public int save(final int spelllevel, int savingthrow) {
		return 10 + spelllevel + Monster.getbonus(10 + spelllevel)
				- savingthrow;
	}
}
