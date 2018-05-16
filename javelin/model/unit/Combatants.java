package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Combatants extends ArrayList<Combatant>
		implements Cloneable, Serializable {
	public Combatants() {
		super();
	}

	public Combatants(int size) {
		super(size);
	}

	public Combatants(Combatants list) {
		super(list);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return Combatant.group(this);
	}

	@Override
	public boolean equals(Object o) {
		return o.getClass() == Combatants.class && hashCode() == o.hashCode();
	}

	@Override
	public Combatants clone() {
		Combatants clone = (Combatants) super.clone();
		for (int i = 0; i < size(); i++) {
			clone.set(i, get(i).clone());
		}
		return clone;
	}

	public List<Monster> getmonsters() {
		ArrayList<Monster> monsters = new ArrayList<Monster>(size());
		for (Combatant c : this) {
			monsters.add(c.source);
		}
		return monsters;
	}
}
