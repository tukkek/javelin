package javelin.model.unit.abilities.discipline;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.ActionCost;
import javelin.controller.action.ai.attack.AbstractAttack;
import javelin.controller.action.ai.attack.DamageChance;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.controller.action.ai.attack.RangedAttack;
import javelin.controller.action.target.MeleeTarget;
import javelin.controller.action.target.RangedTarget;
import javelin.controller.action.target.Target;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
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
		try{
			/**
			 * TODO this assumes you can only attack if you're engaged. implementing
			 * reach may change this? how does engagement work with reach?
			 */
			boolean engaged=Fight.state.isengaged(c);
			ArrayList<Attack> attacks=getattacks(c,
					engaged?c.source.melee:c.source.ranged);
			Attack a;
			if(attacks.size()==1)
				a=attacks.get(0);
			else{
				final String prompt="Which attack will you use?";
				int choice=Javelin.choose(prompt,attacks,attacks.size()>3,false);
				if(choice==-1) throw new RepeatTurn();
				a=attacks.get(choice);
			}
			AbstractAttack.setmaneuver(this);
			final Target action=engaged?new MeleeTarget(a,ap,'m')
					:new RangedTarget(a,ap,'m');
			action.perform(c);
			return true;
		}finally{
			AbstractAttack.setmaneuver(null);
		}
	}

	/**
	 * @return First attack of each {@link AttackSequence}.
	 */
	ArrayList<Attack> getattacks(Combatant c,List<AttackSequence> attacktype){
		AttackSet attacks=new AttackSet();
		for(AttackSequence sequence:attacktype)
			for(Attack a:sequence)
				attacks.addattack(a);
		return attacks;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant c,BattleState s){
		try{
			AbstractAttack.setmaneuver(this);
			ArrayList<List<ChanceNode>> outcomes=new ArrayList<>();
			getoutcomes(c,s,c.source.melee,MeleeAttack.SINGLETON,outcomes);
			getoutcomes(c,s,c.source.ranged,RangedAttack.SINGLETON,outcomes);
			return outcomes;
		}finally{
			AbstractAttack.setmaneuver(null);
		}
	}

	void getoutcomes(Combatant c,BattleState s,ArrayList<AttackSequence> attacks,
			AbstractAttack action,ArrayList<List<ChanceNode>> outcomes){
		for(Attack a:getattacks(c,attacks))
			outcomes.addAll(action.getoutcomes(c,s));
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
	 * {@link #prehit(Combatant, Combatant, Attack, DamageChance, BattleState)}.
	 */
	abstract public void preattacks(Combatant current,Combatant target,Attack a,
			BattleState s);

	/**
	 * Like
	 * {@link #posthit(Combatant, Combatant, Attack, DamageChance, BattleState)}
	 * but for {@link #postattacks(Combatant, Combatant, Attack, BattleState)}.
	 */
	abstract public void postattacks(Combatant current,Combatant target,Attack a,
			BattleState s);

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
	 *
	 * @param dc Make sure to use this to apply extra damage so it will handle
	 *          death, {@link Monster#dr}, etc.
	 */
	abstract public void prehit(Combatant current,Combatant target,Attack a,
			DamageChance dc,BattleState s);

	/**
	 * Called after all damage consequences have been ealt with. Mostly used to
	 * clean-up
	 * {@link #prehit(Combatant, Combatant, Attack, DamageChance, BattleState)}
	 * actions.
	 */
	abstract public void posthit(Combatant current,Combatant target,Attack a,
			DamageChance dc,BattleState s);
}