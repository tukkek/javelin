package javelin.model.unit.abilities.discipline;

import java.io.Serializable;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.maneuver.ExecuteManeuver;
import javelin.controller.ai.ChanceNode;
import javelin.model.Cloneable;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public abstract class Maneuver
		implements Serializable, Cloneable, Comparable<Maneuver> {
	public boolean spent = false;
	public String name;
	/**
	 * {@link Combatant#ap} cost for this action.
	 */
	public float ap;
	public int level;
	/**
	 * Instant maneuver are those like Boosts where there is no further user
	 * interaction necessary.
	 */
	public boolean instant = true;

	public Maneuver(String name, int level) {
		this.name = name;
		this.level = level;
	}

	public void spend() {
		if (!Javelin.DEBUG) {
			spent = true;
		}
	}

	@Override
	public Maneuver clone() {
		try {
			return (Maneuver) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delegate to {@link Action#perform(Combatant)}. The implementations need
	 * not worry about handling the {@link Combatant#ready(Maneuver)} or
	 * {@link Maneuver#spend()}.
	 * 
	 * @see ExecuteManeuver
	 */
	abstract public boolean perform(Combatant c);

	/**
	 * A delegate for {@link AiAction#getoutcomes(Combatant, BattleState)}. By
	 * the time this is called, all parameters have already been properly
	 * cloned. The implementations need not worry about handling the
	 * {@link Combatant#ready(Maneuver)} nor {@link Maneuver#spend()}.
	 * 
	 * @see ExecuteManeuver
	 */
	abstract public List<List<ChanceNode>> getoutcomes(Combatant c,
			BattleState s);

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Maneuver) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	static public int getinitiationmodifier(Combatant c) {
		final Monster m = c.source;
		int modifier = m.hd.count() / 2;
		modifier += Monster.getbonus(
				Math.max(m.intelligence, Math.max(m.wisdom, m.charisma)));
		return Math.min(modifier, m.hd.count());
	}

	protected static boolean save(int save, int savedc, Combatant active) {
		return save == Integer.MAX_VALUE
				|| 10 + save >= savedc + getinitiationmodifier(active);
	}

	public boolean validate(Combatant c) {
		return !c.disciplines.getmaneuvers().contains(this);
	}

	@Override
	public String toString() {
		return name + (spent ? "*" : "");
	}

	@Override
	public int compareTo(Maneuver o) {
		if (level != o.level) {
			return Integer.compare(o.level, level);
		}
		return name.compareTo(o.name);
	}
}
