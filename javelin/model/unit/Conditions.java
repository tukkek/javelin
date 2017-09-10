package javelin.model.unit;

import javelin.model.unit.condition.Condition;

public class Conditions extends CloneableList<Condition> {
	@Override
	public String toString() {
		if (isEmpty()) {
			return "Conditions: none.";
		}
		sort();
		String s = "Conditions: ";
		for (Condition c : this) {
			s += c + ", ";
		}
		return s.substring(0, s.length() - 2);
	}

	public void sort() {
		sort(null);
	}

	@Override
	public Conditions clone() {
		return (Conditions) super.clone();
	}
}
