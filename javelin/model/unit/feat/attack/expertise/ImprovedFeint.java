package javelin.model.unit.feat.attack.expertise;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.abilities.discipline.expertise.FeintManeuver;
import javelin.model.unit.feat.Feat;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedFeint extends ExpertiseFeat{
	/** Unique instance of this {@link Feat}. */
	public static final ImprovedFeint SINGLETON=new ImprovedFeint();

	/** Constructor. */
	private ImprovedFeint(){
		super("Improved feint");
		prerequisite=CombatExpertise.SINGLETON;
	}

	@Override
	public String inform(Combatant c){
		return "";
	}

	@Override
	protected CombatExpertiseManeuver getmaneuver(){
		return new FeintManeuver();
	}
}
