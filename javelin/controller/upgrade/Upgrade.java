package javelin.controller.upgrade;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Fight;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
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
	 * Short description.
	 *
	 * @deprecated See {@link #getname()}
	 */
	@Deprecated
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
	 * @return See {@link #apply(Combatant)}.
	 * @see Combatant#equipped
	 */
	public boolean upgrade(Combatant c){
		ArrayList<Artifact> equipment=new ArrayList<>(c.equipped);
		for(Artifact a:equipment)
			a.remove(c);
		boolean applied=apply(c);
		for(Artifact a:equipment)
			a.usepeacefully(c);
		return applied;
	}

	/**
	 * @param c Uses a deep clone of this unit.
	 * @return <code>true</code> if can {@link #apply(Combatant)} this upgrade.
	 */
	public final boolean validate(Combatant c){
		c=c.clone().clonesource();
		return upgrade(c);
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

	/**
	 * @return Name of this upgrade.
	 */
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
}
