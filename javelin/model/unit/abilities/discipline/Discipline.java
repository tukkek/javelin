package javelin.model.unit.abilities.discipline;

import java.io.Serializable;

import javelin.controller.action.maneuver.ExecuteManeuver;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.world.location.town.labor.military.Academy;

/**
 * Represent a martial-arts discipline from the Path of War books, which is
 * basically a collection of {@link Maneuver}s.
 * 
 * Note that this class isn't {@link Serializable} or {@link Cloneable}. That's
 * because we don't want several instances being persisted/created whenever a
 * new game session is srated. All instances should be accessed in-memory only.
 * 
 * @author alex
 * @see Disciplines#ALL
 */
public abstract class Discipline {
	public String name;
	/**
	 * Whether {@link FeatureGenerator} should generate a DisciplineAcademy or
	 * not. Useful for emulating abilities that are not {@link Maneuvers} as
	 * disciplines for the sake of using the existing {@link ExecuteManeuver}
	 * integrated user interface.
	 * 
	 * Note that some disciplines (such as {@link CombatExpertise}) have an
	 * {@link Academy} - but they are not actual instances of DisciplineAcademy
	 * and are generateed in some other way. In these cases, {@link #hasacademy}
	 * would always be <code>false</code>.
	 */
	public boolean hasacademy = true;

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
