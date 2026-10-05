package javelin.model.unit.feat.attack.expertise;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.expertise.CombatExpertiseManeuver;
import javelin.model.unit.abilities.discipline.expertise.DefensiveAttackManeuver;
import javelin.model.unit.feat.Feat;

/**
 * This is offering the action "Attack defensively", with the appropriate
 * enhancements for the feat. Not otherwise available to prevent overloading the
 * AI with options.
 *
 * @see ImprovedGrapple
 * @author alex
 */
public class CombatExpertise extends ExpertiseFeat{
	/** Unique instance of this {@link Feat}. */
	public static final CombatExpertise SINGLETON=new CombatExpertise();

	/** Constructor. */
	private CombatExpertise(){
		super("Combat expertise");
	}

	@Override
	public String inform(Combatant c){
		return "Has "+c.source.melee.size()+" mêlée attack(s) and intelligence "
				+c.source.intelligence;
	}

	@Override
	public boolean upgrade(Combatant c){
		return c.source.intelligence>=13&&!c.source.melee.isEmpty()
				&&super.upgrade(c);
	}

	@Override
	protected CombatExpertiseManeuver getmaneuver(){
		return new DefensiveAttackManeuver();
	}
}
