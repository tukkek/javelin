package javelin.controller.upgrade;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.town.Town;

/**
 * A improvement to a {@link Combatant}'s {@link Monster} which has to be
 * trained (which has a gold and time cost). Upgrades are distributed among
 * {@link Town}s, like {@link Item}s.
 * 
 * @author alex
 */
public abstract class Upgrade implements Serializable {

	/** Short description. */
	public String name;

	/** Constructor. */
	public Upgrade(final String name) {
		super();
		this.name = name;
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
	public abstract String info(final Combatant c);

	/**
	 * @param c
	 *            Given an unit apply the upgrade on it.
	 * @return <code>false</code> if this is not a valid update. For example:
	 *         already reached the maximum level on a class, cannot learn this
	 *         spell level yet.
	 */
	abstract protected boolean apply(final Combatant c);

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Upgrade) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @param c
	 *            Removes and reapplies his equipment before and after applying
	 *            the upgrade.
	 * @return See {@link #apply(Combatant)}.
	 * @see Combatant#equipped
	 */
	public boolean upgrade(Combatant c) {
		ArrayList<Artifact> equipment = new ArrayList<Artifact>(c.equipped);
		for (Artifact a : equipment) {
			a.remove(c);
		}
		boolean applied = apply(c);
		for (Artifact a : equipment) {
			a.usepeacefully(c);
		}
		return applied;
	}
}
