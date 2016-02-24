package javelin.controller.upgrade;

import java.io.Serializable;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Town;

/**
 * A improvement to a {@link Combatant}'s {@link Monster} which has to be
 * trained (which has a gold and time cost). Upgrades are distributed among
 * {@link Town}s, like {@link Item}s.
 * 
 * @author alex
 */
public abstract class Upgrade implements Serializable {

	public String name;

	public Upgrade(final String name) {
		super();
		this.name = name;
	}

	public abstract String info(final Combatant c);

	abstract public boolean apply(final Combatant c);

	/**
	 * Has a lot of drawbacks currently, namely spending too much xp on a single
	 * choice TODO interface overhaul
	 */
	@Deprecated
	public boolean isstackable() {
		return false;
	}
}
