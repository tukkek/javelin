package javelin.model.unit.condition;

import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;

/**
 * @see CombatExpertise
 * @author alex
 */
public class DefensiveStance extends Condition {

	private int acbonus;

	public DefensiveStance(float expireatp, Combatant c, int acbonus) {
		super(expireatp, c, Effect.POSITIVE, "defensive stance", null);
		this.acbonus = acbonus;
	}

	@Override
	public void start(Combatant c) {
		c.acmodifier += acbonus;
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier -= acbonus;
	}
}
