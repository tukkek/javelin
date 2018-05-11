package javelin.controller.upgrade.ability;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseConsitution extends RaiseAbility {

	public static final Upgrade SINGLETON = new RaiseConsitution();

	private RaiseConsitution() {
		super("constitution");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.constitution;
	}

	@Override
	boolean setattribute(Combatant c, int l) {
		c.source.changeconstitutionmodifier(c, 1);
		return true;
	}

	@Override
	public int getattribute(Monster source) {
		return source.constitution + source.poison;
	}
}
