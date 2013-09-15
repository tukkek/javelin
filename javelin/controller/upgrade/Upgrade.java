package javelin.controller.upgrade;

import java.io.Serializable;

import javelin.model.unit.Combatant;

public abstract class Upgrade implements Serializable {

	public String name;

	public Upgrade(final String name) {
		super();
		this.name = name;
	}

	public abstract String info(final Combatant m);

	abstract public boolean apply(final Combatant m);

	/**
	 * Has a lot of drawbacks currently, namely spending too much xp on a single
	 * choice TODO interface overhaul
	 */
	@Deprecated
	public boolean isstackable() {
		return false;
	}
}
