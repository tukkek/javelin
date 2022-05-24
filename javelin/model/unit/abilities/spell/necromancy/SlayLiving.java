package javelin.model.unit.abilities.spell.necromancy;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;

/** See the d20 SRD for more info. */
public class SlayLiving extends Touch{
	/** Constructor. */
	public SlayLiving(){
		super("Slay living",5,ChallengeCalculator.ratespell(5));
		castinbattle=true;
		provokeaoo=false;
	}

	@Override
	public void filter(Combatant combatant,List<Combatant> targets,
			BattleState s){
		super.filter(combatant,targets,s);
		for(Combatant c:new ArrayList<>(targets))
			if(c.source.passive||!c.source.isalive()) targets.remove(c);
	}

	@Override
	public String cast(final Combatant caster,final Combatant target,
			final boolean saved,final BattleState s,ChanceNode cn){
		if(saved){
			target.damage(Math.round(3*3.5f+9),target.source.energyresistance,s);
			return target+" resists, is now "+target.getstatus()+".";
		}
		target.damage(target.hp+10,0,s);
		return target+" is killed!";
	}

	@Override
	public int save(final Combatant caster,final Combatant target){
		return getsavetarget(target.source.getfortitude(),caster);
	}

}
