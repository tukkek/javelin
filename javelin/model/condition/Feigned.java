package javelin.model.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @se ImprovedFeint
 * @author alex
 */
public class Feigned extends Condition {

	public Feigned(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "feigned", null);
	}

	@Override
	public void start(Combatant c) {
		c.acmodifier -= Monster.getbonus(c.source.dexterity);
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier += Monster.getbonus(c.source.dexterity);
	}
}
