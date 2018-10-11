package javelin.model.unit.abilities.spell.conjuration.healing.wounds;

import javelin.controller.challenge.ChallengeCalculator;

/**
 * See the d20 SRD for more info.
 */
public class CureSeriousWounds extends CureModerateWounds{

	public CureSeriousWounds(String name,float incrementcost){
		super(name,incrementcost,new int[]{3,8,6},3);
	}

	public CureSeriousWounds(){
		this("Cure serious wounds",ChallengeCalculator.ratespelllikeability(3));
	}
}
