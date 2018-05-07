package javelin.model.unit;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javelin.model.Cloneable;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * The current {@link AttackSequence} being used as a full-attack by a
 * {@link Combatant}.
 * 
 * @author alex
 */
public class CurrentAttack implements Serializable, Cloneable {
	public int sequenceindex = -1;
	public LinkedList<Attack> next = new LinkedList<Attack>();

	@Override
	public CurrentAttack clone() {
		try {
			final CurrentAttack c = (CurrentAttack) super.clone();
			c.next = (LinkedList<Attack>) c.next.clone();
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public CurrentAttack() {
		// new instance
	}

	public boolean continueattack() {
		return sequenceindex != -1 && !next.isEmpty();
	}

	public void setcurrent(final int current,
			final List<AttackSequence> attacks) {
		if (current == sequenceindex) {
			if (next.isEmpty()) {
				repopulate(attacks);
			}
			return;
		}
		if (continueattack()) {
			throw new RuntimeException("Ignoring unfinished attack sequence!");
		}
		sequenceindex = current;
		repopulate(attacks);
	}

	public Attack getnext() {
		return next.pop();
	}

	public void repopulate(final List<AttackSequence> attacks) {
		next.addAll(attacks.get(sequenceindex));
	}

	public Attack peek() {
		return next.isEmpty() ? null : next.get(0);
	}

	@Override
	public String toString() {
		return sequenceindex == -1 ? "EMPTY" : Integer.toString(sequenceindex);
	}
}
