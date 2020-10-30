package javelin.model.unit.condition;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.controller.fight.Fight;
import javelin.model.Cloneable;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.abjuration.DispelMagic;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * A condition is a temporary effect on a {@link Combatant}.
 *
 * @see Combatant#conditions
 *
 * @author alex
 */
public abstract class Condition
		implements Cloneable,Serializable,Comparable<Condition>{
	/**
	 * Buff means an effect is benefitial while debuff means it's a penalty.
	 *
	 * @author alex
	 */
	public enum Effect{
		POSITIVE,NEUTRAL,NEGATIVE
	}

	public static void raiseattacks(ArrayList<AttackSequence> melee,
			int attackbonus,int damagebonus){
		for(final AttackSequence sequence:melee)
			for(final Attack a:sequence){
				a.bonus+=attackbonus;
				a.damage[2]+=damagebonus;
			}
	}

	/**
	 * TODO make more like {@link Combatant#skillmodifier}
	 */
	public static void raiseallattacks(final Monster m,int attackbonus,
			int damagebonus){
		raiseattacks(m.melee,attackbonus,damagebonus);
		raiseattacks(m.ranged,attackbonus,damagebonus);
	}

	/**
	 * TODO make more like {@link Combatant#skillmodifier}
	 */
	public static void raisesaves(final Monster m,int amount){
		m.fort+=amount;
		m.ref+=amount;
		m.addwill(amount);
	}

	/**
	 * AP at which this effect will wear off. Use {@link Float#MAX_VALUE} to make
	 * it permanent for the duration of battle.
	 */
	public float expireat;
	/** @see Effect */
	public Effect effect;
	/** Short description. */
	public String description;
	/**
	 * The number of hours for this to persist after battle. If <code>null</code>
	 * will call {@link #end(Combatant)} at the end of combat if it hasn't
	 * experired before that. If 0 will stop at the end of combat but
	 * {@link #end(Combatant)} will affect the original {@link Combatant} instead
	 * of the clone used for the {@link Fight}.
	 *
	 * Subclasses that wish to implement their own {@link #expire(int, Combatant)}
	 * mechanisms can pass {@link Integer#MAX_VALUE} here.
	 *
	 * @see Fight#originalblueteam
	 */
	public Integer longterm;
	/** {@link Spell#level} or <code>null</code>. */
	public Integer spelllevel;
	/**
	 * <code>null</code> if this is not magical and can't be affected by
	 * {@link DispelMagic}.
	 */
	public Integer casterlevel;
	/**
	 * If <code>true</code>, allows multiple instances of this condition to affect
	 * the same {@link Combatant} - otherwise, will
	 * {@link #merge(Combatant, Condition)} them instead.
	 */
	public boolean stack=false;

	/** Full constructor. */
	public Condition(String descriptionp,Integer spelllevel,Integer casterlevel,
			float expireatp,Integer longtermp,Effect effectp){
		expireat=expireatp;
		effect=effectp;
		description=descriptionp;
		longterm=longtermp;
		this.spelllevel=spelllevel;
		this.casterlevel=casterlevel;
	}

	/** Constructor with {@link Spell} and {@link #longterm}. */
	public Condition(String description,Spell s,float expireatp,Integer longtermp,
			final Effect effectp){
		this(description,s==null?null:s.level,s==null?null:s.casterlevel,expireatp,
				longtermp,effectp);
	}

	/** Constructor with {@link Spell} and no {@link #longterm}. */
	public Condition(String description,Spell s,float expireatp,
			final Effect effectp){
		this(description,s,expireatp,null,effectp);
	}

	public abstract void start(Combatant c);

	/**
	 * In-battle check if a Condition has expired.
	 *
	 * @return <code>true</code> if has expired and has been removed.
	 * @see #expireat
	 */
	public boolean expireinbattle(final Combatant c){
		if(c.ap<expireat) return false;
		c.removecondition(this);
		return true;
	}

	/**
	 * Removes all effects of this condition from {@link Combatant}. This may be
	 * called during battle, right after battle or at a later point depending on
	 * the values of {@link #expireat} and {@link #longterm}.
	 */
	public abstract void end(Combatant c);

	@Override
	public boolean equals(final Object obj){
		return getClass().equals(obj.getClass());
	}

	@Override
	public int hashCode(){
		return getClass().hashCode();
	}

	public void finish(BattleState s){
		// does nothing by default
	}

	@Override
	public Condition clone(){
		try{
			return (Condition)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called one or more times, eventually calling
	 * {@link Combatant#removecondition(Condition)} (and thus
	 * {@link #end(Combatant)}) once {@link #longterm} decreases to zero or less.
	 *
	 * @param time Elapsed time in hours.
	 */
	public void terminate(int time,Combatant c){
		if(longterm==null||expire(time,c)) c.removecondition(this);
	}

	/**
	 * Expire out of battle.
	 *
	 * @param Time elapsed, in hours.
	 * @return <code>true</code> if is over and has to be removed.
	 * @see #longterm
	 */
	protected boolean expire(int time,Combatant c){
		longterm-=time;
		return longterm<=0;
	}

	@Override
	public String toString(){
		return description;
	}

	/**
	 * This is called when a condition is removed by {@link DispelMagic}, mostly
	 * as a way to prevent negative effects from happening in
	 * {@link #end(Combatant)}, when applicable.
	 *
	 * See {@link Poisoned} as an example.
	 */
	public void dispel(){
		// nothing
	}

	/**
	 * @return If <code>false</code>, will discard this condition.
	 */
	public boolean validate(Combatant c){
		return true;
	}

	/** @param condition Condition to be merged. Will be discarded afterwards. */
	public void merge(Combatant c,Condition condition){
		//nothing by default
	}

	@Override
	public int compareTo(Condition c){
		return description.compareTo(c.description);
	}
}
