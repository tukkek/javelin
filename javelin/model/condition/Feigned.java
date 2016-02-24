package javelin.model.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @se ImprovedFeint
 * @author alex
 */
public class Feigned extends Condition {

	public Feigned(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "feigned");
	}

	@Override
			void start(Combatant c) {
		c.acmodifier -= Monster.getbonus(c.source.dexterity);
	}

	@Override
			void end(Combatant c) {
		c.acmodifier += Monster.getbonus(c.source.dexterity);
	}
}
