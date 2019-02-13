package javelin.controller.action.ai.attack;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.target.MeleeTarget;
import javelin.controller.ai.ChanceNode;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.condition.Prone;

/**
 * AI version of mêlée attacks.
 *
 * @see MeleeTarget
 * @author alex
 */
public class MeleeAttack extends AbstractAttack{
	static final public MeleeAttack SINGLETON=new MeleeAttack();

	MeleeAttack(){
		super("Melee attack","melee");
		feign=true;
		cleave=true;
	}

	@Override
	List<AttackSequence> getattacks(final Combatant active){
		return active.source.melee;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant active,
			final BattleState s){
		final ArrayList<List<ChanceNode>> successors=new ArrayList<>();
		for(final Combatant target:s.getsurroundings(active))
			for(final Integer attack:getcurrentattack(active)){
				if(target.isally(active,s)) continue;
				final BattleState newstate=s.clone();
				final Combatant newactive=newstate.clone(active);
				newactive.currentmelee.setcurrent(attack,newactive.source.melee);
				var outcome=attack(newstate,newactive,target,newactive.currentmelee,0);
				successors.add(outcome);
			}
		return successors;
	}

	@Override
	public int getpenalty(Combatant attacker,Combatant target,BattleState s){
		int penalty=super.getpenalty(attacker,target,s);
		if(attacker.flank(target,s)) penalty-=2;
		if(target.hascondition(Prone.class)!=null) penalty-=2;
		return penalty;
	}
}