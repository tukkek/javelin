package javelin.controller.upgrade.ability;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * @see RaiseAbility
 */
public class RaiseDexterity extends RaiseAbility {
	/** Unique instance for this {@link Upgrade}. */
	public static final RaiseDexterity SINGLETON = new RaiseDexterity();

	private RaiseDexterity() {
		super("dexterity");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.dexterity;
	}

	@Override
	boolean setattribute(Combatant m, int l) {
		m.source.raisedexterity(+1);
		return true;
	}

	@Override
	public int getattribute(Monster source) {
		return source.dexterity;
	}
}
