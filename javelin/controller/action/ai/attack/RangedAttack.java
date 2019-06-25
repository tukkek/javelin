package javelin.controller.action.ai.attack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.target.RangedTarget;
import javelin.controller.ai.ChanceNode;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.model.unit.attack.AttackSequence;
import javelin.model.unit.condition.Prone;
import javelin.model.unit.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.unit.feat.attack.shot.PointBlankShot;
import javelin.model.unit.feat.attack.shot.PreciseShot;

/**
 * needs to take into account range increments, each full range increment
 * imposes a cumulative -2 penalty on the attack roll. Actually doesn't sound
 * too important since most weapons used have really long ranges for our combats
 *
 * also -4 if in melée with another enemy (many more details @
 * http://www.d20pfsrd.com/gamemastering/combat
 *
 * @author alex
 * @see RangedTarget
 */
public class RangedAttack extends AbstractAttack{
	static final public RangedAttack SINGLETON=new RangedAttack();
	/**
	 * Currently if the AI sees an infinitesimal chance to hit, it'll rather
	 * damage any unit for immediate utility. This is very statoc, with units
	 * always attacking from far away if they have ranged attacks. Let's disable
	 * this for {@link #getoutcomes(Combatant, BattleState)} for now.
	 */
	static final boolean AISKIPUNLIKELY=true;

	private RangedAttack(){
		super("Ranged attack","ranged-hit","ranged-miss");
	}

	@Override
	List<AttackSequence> getattacks(final Combatant active){
		return active.source.ranged;
	}

	@Override
	public int getpenalty(final Combatant attacker,final Combatant target,
			final BattleState s){
		int penalty=super.getpenalty(attacker,target,s);
		if(!attacker.source.hasfeat(PreciseShot.SINGLETON)&&s.isengaged(target))
			penalty+=4;
		if(!attacker.source.hasfeat(ImprovedPreciseShot.SINGLETON)
				&&iscovered(s.haslineofsight(attacker,target),target,s))
			penalty+=4;
		if(target.hascondition(Prone.class)!=null) penalty+=2;
		if(ispointblankshot(attacker,target)) penalty-=1;
		return penalty;
	}

	public static boolean iscovered(Vision sight,Combatant target,BattleState s){
		return sight==Vision.COVERED||sight==Vision.CLEAR
				&&s.map[target.location[0]][target.location[1]].obstructed;
	}

	static boolean ispointblankshot(final Combatant attacker,
			final Combatant target){
		return attacker.source.hasfeat(PointBlankShot.SINGLETON)
				&&Walker.distance(attacker,target)<=6;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(final Combatant active,
			final BattleState gameState){
		if(gameState.isengaged(active)) return Collections.EMPTY_LIST;
		final var successors=new ArrayList<List<ChanceNode>>();
		for(final Combatant target:gameState.gettargets(active))
			for(final Integer attack:getcurrentattack(active)){
				final BattleState newstate=gameState.clone();
				final Combatant newactive=newstate.clone(active);
				newactive.currentranged.setcurrent(attack,newactive.source.ranged);
				var outcome=attack(newstate,newactive,target,newactive.currentranged,0);
				if(skip(active,(DamageNode)outcome.get(0),gameState)) continue;
				successors.add(outcome);
			}
		return successors;
	}

	/**
	 * @return <code>true</code> if this ranged attack has a very unlikely chance
	 *         to hit and only as long as the active {@link Combatant} cannot
	 *         potentially move to reposition himself for a mêlée attack.
	 *
	 * @see #AISKIPUNLIKELY
	 */
	static boolean skip(Combatant active,DamageNode miss,BattleState previous){
		if(!AISKIPUNLIKELY) return false;
		if(Javelin.DEBUG) assert miss.damage.damage==0;
		if(miss.chance<=Javelin.HARD/20f||active.source.melee.isEmpty())
			return false;
		var map=previous.map;
		for(Point p:Point.getadjacent2()){
			p.x+=active.location[0];
			p.y+=active.location[1];
			if(!p.validate(0,0,map.length,map[0].length)) continue;
			if(previous.getcombatant(p.x,p.y)!=null) continue;
			if(!map[p.x][p.y].blocked||active.source.fly>0) return true;
		}
		return false;
	}

	@Override
	protected int getdamagebonus(Combatant attacker,Combatant target){
		return ispointblankshot(attacker,target)?1:0;
	}
}