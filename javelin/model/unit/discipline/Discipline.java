package javelin.model.unit.discipline;

import java.io.Serializable;

import javelin.model.unit.discipline.expertise.CombatExpertiseDiscipline;

/**
 * Represent a martial-arts discipline from the Path of War books, which is
 * basically a collection of {@link Maneuver}s.
 * 
 * Note that this class isn't {@link Serializable} or {@link Cloneable}. That's
 * because we don't want several instances being persisted/created whenever a
 * new game session is srated. All instances should be accessed in-memory only.
 * 
 * @author alex
 */
public abstract class Discipline {
	public static final Discipline[] DISCIPLINES = new Discipline[] {
			CombatExpertiseDiscipline.INSTANCE };

	String name;

	public Discipline(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Maneuver) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
