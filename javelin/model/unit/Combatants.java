package javelin.model.unit;

import java.io.Serializable;
import java.util.ArrayList;

public class Combatants extends ArrayList<Combatant>
		implements Cloneable, Serializable {
	public Combatants() {
		super();
	}

	public Combatants(int size) {
		super(size);
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
}
