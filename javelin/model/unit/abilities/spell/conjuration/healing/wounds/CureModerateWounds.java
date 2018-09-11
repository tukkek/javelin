package javelin.model.unit.abilities.spell.conjuration.healing.wounds;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;

/**
 * See the d20 SRD for more info.
 */
public class CureModerateWounds extends Touch{
	/**
	 * @XdY+Z
	 */
	final int[] rolldata;

	public CureModerateWounds(final String name,final float incrementcost,
			int[] rolldatap,int levelp){
		super(name,levelp,incrementcost,Realm.WATER);
		rolldata=rolldatap;
		castonallies=true;
		castoutofbattle=true;
		castinbattle=true;
		isritual=true;
		ispotion=true;
	}

	public CureModerateWounds(){
		this("Cure moderate wounds",ChallengeCalculator.ratespelllikeability(2),
				new int[]{2,8,4},2);
	}

	@Override
	public void filtertargets(Combatant combatant,List<Combatant> targets,
			BattleState s){
		super.filtertargets(combatant,targets,s);
		for(Combatant c:new ArrayList<>(targets))
			if(c.hp==c.maxhp) targets.remove(c);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		final int heal=rolldata[0]*rolldata[1]/2+rolldata[2];
		target.heal(heal,true);
		return target+" is now "+target.getstatus()+".";
	}

	@Override
	public String castpeacefully(final Combatant caster,final Combatant combatant,
			List<Combatant> squad){
		return cast(caster,combatant,false,null,null);
	}

	@Override
	public boolean canheal(Combatant c){
		return c.hp<c.maxhp;
	}
}
