package javelin.model.unit.abilities.spell.conjuration;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.labor.basic.Lodge;

/**
 * See the d20 SRD for more info.
 */
public class SecureShelter extends Spell{
	public SecureShelter(){
		super("Secure shelter",4,ChallengeCalculator.ratespelllikeability(4));
		castoutofbattle=true;
		isscroll=true;
	}

	@Override
	public boolean validate(Combatant caster,Combatant target){
		return Dungeon.active==null;
	}

	@Override
	public String castpeacefully(Combatant caster,Combatant target,
			List<Combatant> squad){
		Lodge.rest(1,8,true,Lodge.LODGE);
		return null;
	}
}
