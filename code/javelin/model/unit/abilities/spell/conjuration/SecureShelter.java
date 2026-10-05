package javelin.model.unit.abilities.spell.conjuration;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.basic.Lodge;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelter extends Spell{
	/** Constructor. */
	public SecureShelter(){
		super("Secure shelter",4,ChallengeCalculator.ratespell(4));
		castoutofbattle=true;
	}

	@Override
	public boolean validate(Combatant caster,Combatant target){
		return Dungeon.active==null;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target){
		Lodge.rest(1,Lodge.RESTPERIOD,true,Lodge.LODGE);
		return null;
	}
}
