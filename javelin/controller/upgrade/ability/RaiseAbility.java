package javelin.controller.upgrade.ability;

import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises one of the six attributes by +2 score points (equivalent of +1 ability
 * score modifier bonus).
 * 
 * @author alex
 */
public abstract class RaiseAbility extends Upgrade {
	private final String abilityname;

	public RaiseAbility(String abilityname) {
		super("Raise ability: " + abilityname);
		this.abilityname = abilityname;
	}

	@Override
	public String info(Combatant m) {
		String out = "Current " + abilityname + ": ";
		int bonus = Monster.getbonus(getabilityvalue(m.source));
		if (bonus > 0) {
			out += "+";
		}
		return out + bonus;
	}

	abstract int getabilityvalue(Monster m);

	@Override
	public boolean apply(Combatant m) {
		int score = getabilityvalue(m.source);
		return score > 0 && setattribute(m, score + 2);
	}

	abstract boolean setattribute(Combatant m, int l);

}
