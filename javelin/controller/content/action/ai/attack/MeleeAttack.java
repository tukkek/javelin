package javelin.controller.content.action.ai.attack;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.target.MeleeTarget;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.condition.Prone;

/**
 * AI version of mêlée attacks.
 *
 * @see MeleeTarget
 * @author alex
 */
public class MeleeAttack extends AbstractAttack{
	static final public MeleeAttack INSTANCE=new MeleeAttack(null);

	/** Constructor. */
	public MeleeAttack(Strike m){
		super("Melee attack",m,"melee-hit","melee-miss");
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
		var successors=new ArrayList<List<ChanceNode>>();
		for(var target:s.getsurroundings(active.getlocation()))
			if(!target.isally(active,s)) for(var attacks:active.source.melee){
				var resolver=new AttackResolver(this,active,target,attacks,s);
				successors.add(resolver.attack(active,target,s));
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