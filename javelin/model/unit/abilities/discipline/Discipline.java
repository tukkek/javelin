package javelin.model.unit.abilities.discipline;

import java.io.Serializable;

import javelin.controller.action.maneuver.ExecuteManeuver;
import javelin.controller.generator.feature.FeatureGenerator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.abilities.discipline.serpent.SteelSerpent;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.town.labor.military.BuildDisciplineAcademy;
import javelin.model.world.location.town.labor.military.DisciplineAcademy;

/**
 * Represent a martial-arts discipline from the Path of War books, which is
 * basically a collection of {@link Maneuver}s.
 * 
 * Note that this class isn't {@link Serializable} or {@link Cloneable}. That's
 * because we don't want several instances being persisted/created whenever a
 * new game session is srated. All instances should be accessed in-memory only.
 * 
 * Each discipline should have at least 2 {@link Maneuver}s per level.
 * 
 * @author alex
 * @see Disciplines#ALL
 */
public abstract class Discipline {
	public static final Discipline[] DISCIPLINES = new Discipline[] {
			SteelSerpent.INSTANCE };

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
	 * 
	 * @see FeatureGenerator
	 * @see #generateacademy()
	 */
	public boolean hasacademy = true;

	public RaiseAbility abilityupgrade;
	public ClassLevelUpgrade classupgrade;
	public Upgrade skillupgrade;
	public String name;

	public Discipline(String name, ClassLevelUpgrade classupgrade,
			RaiseAbility abilityupgrade, Upgrade skillupgrade) {
		this(name);
		this.abilityupgrade = abilityupgrade;
		this.classupgrade = classupgrade;
		this.skillupgrade = skillupgrade;
	}

	/** Use only for emulated {@link Discipline}s. */
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

	/** See {@link #hasacademy}. */
	public DisciplineAcademy generateacademy() {
		return new DisciplineAcademy(this);
	}

	public BuildDisciplineAcademy buildacademy() {
		return new BuildDisciplineAcademy(this);
	}

	protected abstract Maneuver[] getmaneuvers();

	/**
	 * @param maxlevel
	 *            Any {@link Maneuver} higher than this level will not be
	 *            returned. If <code>null</code>, return all {@link Maneuver}s.
	 * @return A new list with the filtered maneuvers from this discipline.
	 */
	public Maneuvers getmaneuvers(Integer maxlevel) {
		Maneuvers maneuvers = new Maneuvers(maxlevel * 2);
		for (Maneuver m : maneuvers) {
			if (maxlevel == null || m.level <= maxlevel) {
				maneuvers.add(m);
			}
		}
		maneuvers.sort();
		return maneuvers;
	}
}
