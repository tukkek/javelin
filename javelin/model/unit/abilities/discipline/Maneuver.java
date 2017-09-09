package javelin.model.unit.abilities.discipline;

import java.io.Serializable;
import java.util.List;

import javelin.controller.action.Action;
import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

public abstract class Maneuver implements Serializable, Cloneable {
	public boolean spent = false;
	public String name;
	/**
	 * {@link Combatant#ap} cost for this action.
	 */
	public float ap;

	public Maneuver(String name) {
		this.name = name;
	}

	public void spend() {
		spent = true;
	}

	@Override
	public Maneuver clone() {
		try {
			return (Maneuver) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/** See {@link Action#perform(Combatant)}. */
	abstract public boolean perform(Combatant c);

	/** See {@link AiAction#getoutcomes(BattleState, Combatant)}. */
	abstract public List<List<ChanceNode>> getoutcomes(BattleState s,
			Combatant c);

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
		final int modifier = Math.max(m.intelligence,
				Math.max(m.wisdom, m.charisma));
		return m.hd.count() / 2 + Monster.getbonus(modifier);
	}

	public boolean validate(Combatant c) {
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}
