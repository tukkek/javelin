package javelin.controller.upgrade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.kit.Kit;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.location.town.Town;

/**
 * A improvement to a {@link Combatant}'s {@link Monster} which has to be
 * trained (which has a gold and time cost). Upgrades are distributed among
 * {@link Town}s, like {@link Item}s.
 *
 * @see Combatant#postupgrade(boolean, Upgrade)
 * @see Combatant#postupgradeautomatic(boolean, Upgrade)
 * @author alex
 */
public abstract class Upgrade implements Serializable{
	/**
	 * Upgrade name.
	 *
	 * @see #getname()
	 */
	public String name;
	/**
	 * Indicates that this upgrade is immediately relevant during {@link Fight}s.
	 */
	protected boolean usedincombat=true;

	/** Constructor. */
	public Upgrade(final String name){
		super();
		this.name=name;
	}

	/**
	 * To be show before upgrade confirmation, containing relevant information
	 * that might help decide to buy it or not.
	 *
	 * For example, a {@link Spell} could show how many times it can already be
	 * cast by the {@link Combatant} in question.
	 *
	 * @return one line of text.
	 */
	public abstract String inform(final Combatant c);

	/**
	 * @param c Given an unit apply the upgrade on it.
	 * @return <code>false</code> if this is not a valid update. For example:
	 *         already reached the maximum level on a class, cannot learn this
	 *         spell level yet.
	 */
	abstract protected boolean apply(final Combatant c);

	@Override
	public boolean equals(Object obj){
		return name.equals(((Upgrade)obj).name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @param c Removes/reapplies equipment before/after applying the upgrade.
	 * @param spendxp If <code>true</code>, will update {@link Combatant#xp} if
	 *          applied.
	 * @return See {@link #apply(Combatant)}.
	 * @see Combatant#equipped
	 */
	public boolean upgrade(Combatant c,boolean spendxp){
		var oldcr=spendxp?ChallengeCalculator.calculaterawcr(c.source)[1]:0;
		var equipment=new ArrayList<>(c.equipped);
		for(var e:equipment)
			e.remove(c);
		var applied=apply(c);
		for(var e:equipment)
			e.usepeacefully(c);
		if(applied&&spendxp){
			var newcr=ChallengeCalculator.calculaterawcr(c.source)[1];
			c.xp=c.xp.subtract(new BigDecimal(newcr-oldcr));
		}
		return applied;
	}

	/**
	 * @return As {@link #upgrade(Combatant, boolean)} but does not update
	 *         {@link Combatant#xp}.
	 */
	public boolean upgrade(Combatant c){
		return upgrade(c,false);
	}

	/**
	 * @param c Uses a deep clone of this unit.
	 * @param checkxp <code>true</code> to check if given enough has enough
	 *          {@link Combatant#xp} to aplly this.
	 * @return <code>true</code> if can {@link #apply(Combatant)} this upgrade.
	 */
	public final boolean validate(Combatant c,boolean checkxp){
		var cost=getcost(c);
		return cost!=null&&(!checkxp||cost<=c.xp.floatValue());
	}

	/**
	 * 1 CR = 100 XP.
	 *
	 * @return The CR cost to upgrade this {@link Combatant} or <code>null</code>
	 *         if cannot {@link #apply(Combatant)}.
	 */
	public Float getcost(Combatant c){
		final Combatant clone=c.clone().clonesource();
		if(!upgrade(clone)) return null;
		float oldcr=ChallengeCalculator.calculaterawcr(c.source)[1];
		float newcr=ChallengeCalculator.calculaterawcr(clone.source)[1];
		return newcr-oldcr;
	}

	/** @return Name of this upgrade, possibly formatted or enhanced. */
	public String getname(){
		return name;
	}

	/**
	 * TODO currently necessary due to {@link Upgrade#usedincombat} and
	 * {@link Spell#castinbattle}. Unify.
	 */
	public boolean isusedincombat(){
		return usedincombat;
	}

	/** @return All available upgrades (found through {@link Kit}. */
	public static Set<Upgrade> getall(){
		var all=new HashSet<Upgrade>(Kit.KITS.size()*9);
		for(var k:Kit.KITS)
			all.addAll(k.getupgrades());
		return all;
	}

	/** @return Same as {@link #getall()} but filters by instance type. */
	@SuppressWarnings("unchecked")
	public static <K extends Upgrade> Set<K> getall(Class<K> type){
		return getall().stream().filter(u->type.isInstance(u)).map(u->(K)u)
				.collect(Collectors.toSet());
	}
}
