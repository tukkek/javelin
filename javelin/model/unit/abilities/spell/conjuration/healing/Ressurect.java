package javelin.model.unit.abilities.spell.conjuration.healing;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;

/**
 * Also features "restoration", implicitly. See the d20 SRD for more info.
 */
public class Ressurect extends RaiseDead{
	/** Constructor. */
	public Ressurect(){
		super("Ressurection",7,
				ChallengeCalculator.ratespell(7)+RaiseDead.RESTORATIONCR);
		components=10000;
		castinbattle=false;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		target.hp=target.maxhp;
		return null;
	}
}
