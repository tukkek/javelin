package javelin.model.unit.abilities.discipline;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.ai.ChanceNode;
import javelin.controller.content.action.ActionCost;
import javelin.controller.content.action.ai.attack.AbstractAttack;
import javelin.controller.content.action.ai.attack.MeleeAttack;
import javelin.controller.content.action.ai.attack.RangedAttack;
import javelin.controller.content.action.target.MeleeTarget;
import javelin.controller.content.action.target.RangedTarget;
import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.discipline.serpent.DizzyingVenomPrana;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * Lets the user select a standard attack (not a full sequence) then sets (and
 * later clears) {@link AbstractAttack#CURRENTMANEUVER} and delegates to either
 * {@link MeleeAttack} or {@link RangedAttack}.
 *
 * The line between Strikes and Boosts is often blurred, especially when the
 * Boost only gives you a bonus to the next attack (like with
 * {@link DizzyingVenomPrana}), in which case they are, effectively, a Strike.
 *
 * For {@link Maneuver#ap}, uses a {@link ActionCost#SWIFT} action (for the
 * maneuver) plus a {@link ActionCost#STANDARD} action (for the attack), by
 * default.
 *
 * {@link #validate(Combatant)} requires that the given {@link Combatant} have
 * at least one ranged or melee attack.
 *
 * * There isn't a pre/post "miss" methods yet because that would interfere with
 * the {@link AbstractAttack} logic a little bit.
 *
 * TODO this is currently an experimental (but stable) API. After more
 * experience with {@link Maneuver}s in general, it might be possible to remove
 * methods.
 *
 * @author alex
 */
public abstract class Strike extends Maneuver{
	class AttackSet extends ArrayList<Attack>{
		public void addattack(Attack a){
			int previous=indexOf(a);
			if(previous==-1)
				add(a);
			else if(a.bonus>get(previous).bonus) set(previous,a);
		}
	}

	public Strike(String name,int level){
		super(name,level);
		ap=ActionCost.SWIFT+ActionCost.STANDARD;
		instant=false;
	}

	@Override
	public boolean perform(Combatant c){
		/**
		 * TODO this assumes you can only attack if you're engaged. implementing
		 * reach may change this? how does engagement work with reach?
		 */
		var engaged=Fight.state.isengaged(c);
		var sequence=c.chooseattack(engaged?c.source.melee:c.source.ranged);
		var a=sequence.get(0);
		var action=engaged?new MeleeTarget(a,sequence,'m',new MeleeAttack(this))
				:new RangedTarget(a,sequence,'m',new RangedAttack(this));
		action.perform(c);
		return true;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant c,BattleState s){
		var outcomes=new ArrayList<List<ChanceNode>>();
		outcomes.addAll(new MeleeAttack(this).getoutcomes(c,s));
		outcomes.addAll(new RangedAttack(this).getoutcomes(c,s));
		return outcomes;
	}

	@Override
	public boolean validate(Combatant c){
		if(!super.validate(c)) return false;
		return !c.source.melee.isEmpty()||!c.source.ranged.isEmpty();
	}

	/**
	 * Called before any {@link DamageNode}s are created. Useful for setting up
	 * effects and the like. This is also called only once per
	 * {@link AbstractAttack} run, making it preferable to
	 * {@link #prehit(Combatant, Combatant, Attack, BattleState)}.
	 */
	public void preattacks(Combatant current,Combatant target,
			AttackSequence sequence,BattleState s){
		//nothing by default
	}

	/**
	 * Like
	 * {@link #posthit(Combatant, Combatant, Attack, DamageChance, BattleState)}
	 * but for {@link #postattacks(Combatant, Combatant, Attack, BattleState)}.
	 */
	public void postattacks(Combatant current,Combatant target,
			AttackSequence sequence,BattleState s){
		//nothing by default
	}

	/**
	 * Called before a strike hits. Mostly used to setup bonus damage, secondary
	 * effects, etc.
	 *
	 * {@link BattleState} and {@link Combatant}s are already cloned. Make sure to
	 * clone {@link Combatant#source} internally, if necessary.
	 *
	 * Does not support randomness, so use take-10 rules whenever possible (for
	 * saving throws, etc).
	 *
	 * This is called before {@link Attack#effect} is taken into effect (if it is
	 * at all).
	 */
	public void prehit(Combatant current,Combatant target,Attack a,BattleState s){
		//nothing by default
	}

	/**
	 * Simialr to {@link #prehit(Combatant, Combatant, Attack, BattleState)} but
	 * called afterward, for clean-up.
	 */
	public void posthit(Combatant c,Combatant target,Attack a,BattleState s){
		//nothing by default
	}
}