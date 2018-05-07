package javelin.model.unit.abilities;

import java.io.Serializable;

public class Constrict implements javelin.model.Cloneable, Serializable {
	public int damage;
	public boolean energy = false;

	@Override
	public Constrict clone() {
		try {
			return (Constrict) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "constrict " + damage + " damage" + (energy ? " (energy)" : "");
	}
}
