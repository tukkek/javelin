package javelin.model.unit.abilities.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.kit.Barbarian;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Like {@link Rage} but only targets self. This is not as much a {@link Spell}
 * as it is an emulation of the {@link Barbarian} ability.
 *
 * @author alex
 */
public class BarbarianRage extends Rage{
	/** Constructor. */
	public BarbarianRage(){
		super("Barbarian rage",1,ChallengeCalculator.ratespell(1));
		provokeaoo=false;
		isscroll=false;
	}

	@Override
	float getduration(Combatant target){
		return super.getduration(target)/2f;
	}

	@Override
	public void filter(Combatant combatant,List<Combatant> targets,
			BattleState s){
		targetself(combatant,targets);
	}
}
