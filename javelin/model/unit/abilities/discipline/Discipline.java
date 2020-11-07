package javelin.model.unit.abilities.discipline;

import java.util.HashMap;

import javelin.controller.action.maneuver.ExecuteManeuver;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.feat.MartialTraining;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.unit.skill.Knowledge;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.Academy;
import javelin.model.world.location.town.labor.Labor;
import javelin.model.world.location.town.labor.military.DisciplineAcademy;
import javelin.model.world.location.town.labor.military.DisciplineAcademy.BuildDisciplineAcademy;

/**
 * Represent a martial-arts discipline from the Path of War books, which is
 * basically a collection of {@link Maneuver}s.
 *
 * Each discipline should have at least 2 {@link Maneuver}s per level.
 * Technically, everything is fine as long as there at least 2 disciplines per
 * leve of {@link MartialTraining} the unit can upgrade to - so having 8 level 1
 * Maneuvers is enough to get a discipline to level 4 (even if not ideal).
 *
 * @author alex
 */
public abstract class Discipline extends Kit{
	/**
	 * AAl instances by {@link Discipline#name}.
	 *
	 * @see MartialTraining
	 */
	public static final HashMap<String,Discipline> ALL=new HashMap<>();

	/**
	 * Whether {@link LocationGenerator} should generate a DisciplineAcademy or
	 * not. Useful for emulating abilities that are not {@link Maneuvers} as
	 * disciplines for the sake of using the existing {@link ExecuteManeuver}
	 * integrated user interface.
	 *
	 * Note that some disciplines (such as {@link CombatExpertise}) have an
	 * {@link Academy} - but they are not actual instances of DisciplineAcademy
	 * and are generateed in some other way. In these cases, {@link #hasacademy}
	 * would always be <code>false</code>.
	 *
	 * @see LocationGenerator
	 * @see #generateacademy()
	 */
	public boolean hasacademy=true;

	/**
	 * Human-readable discipline name.
	 *
	 * TODO fix collison with {@link Kit#name}
	 */
	public String name;
	/**
	 * Particular knowledge skill to apply. Currently there is only
	 * {@link Knowledge}.
	 */
	final public Skill knowledgeupgrade=Skill.KNOWLEDGE;
	/** Upgrade that embodies the training in this discipline. */
	public FeatUpgrade trainingupgrade;
	/** Skill related to this discipline, often used in its {@link Maneuver}s. */
	public Skill skillupgrade;

	/** Constructor. */
	public Discipline(String name,String kitname,RaiseAbility ability,
			Skill skill){
		super(kitname,Warrior.SINGLETON,ability,null);
		this.name=name;
		skillupgrade=skill;
		trainingupgrade=new FeatUpgrade(new MartialTraining(this));
		basic.add(skillupgrade.getupgrade());
		basic.add(knowledgeupgrade.getupgrade());
		extension.add(trainingupgrade);
		prestige=true;
		ALL.put(name,this);
	}

	/**
	 * Used for emulated Disciplines.
	 *
	 * @deprecated
	 */
	@Deprecated
	public Discipline(String name){
		this.name=name;
		hasacademy=false;
	}

	@Override
	public boolean equals(Object obj){
		return obj instanceof Discipline&&name.equals(((Discipline)obj).name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	/** See {@link #hasacademy}. */
	public DisciplineAcademy generateacademy(){
		return new DisciplineAcademy(this);
	}

	/**
	 * @return {@link Academy} {@link Labor}.
	 */
	public BuildDisciplineAcademy buildacademy(){
		return new BuildDisciplineAcademy(this);
	}

	/**
	 * @return A static, non-serializable list of {@link Maneuver}s that are part
	 *         of this martial path. This is to reduce the number of bugs, prevent
	 *         duplication of these lists when each new instance of the game is
	 *         opened and to make debug easier without losing game state.
	 */
	protected abstract Maneuver[] getmaneuvers();

	/**
	 * @param maxlevel Any {@link Maneuver} higher than this level will not be
	 *          returned. If <code>null</code>, return all {@link Maneuver}s.
	 * @return A new list with the filtered maneuvers from this discipline.
	 */
	public Maneuvers getmaneuvers(Integer maxlevel){
		var maneuvers=new Maneuvers(maxlevel*2);
		for(var m:getmaneuvers())
			if(m.level<=maxlevel) maneuvers.add(m);
		maneuvers.sort();
		return maneuvers;
	}

	@Override
	public String toString(){
		return name;
	}

	@Override
	protected void define(){
		//see constructor
	}

	@Override
	protected void extend(){
		//see constructor
	}
}
