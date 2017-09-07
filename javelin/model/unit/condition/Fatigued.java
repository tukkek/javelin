package javelin.model.unit.condition;

import javelin.model.unit.attack.Combatant;

/**
 * A fatigued character can neither run nor charge and takes a -2 penalty to
 * Strength and Dexterity. Spells that use this must understand that generally a
 * fatigued creature becomes exhausted if it receives another level of fatigue.
 * 
 * @author alex
 */
public class Fatigued extends Condition {

	protected Fatigued(Combatant c, String descriptionp, Integer casterlevelp,
			Integer hours) {
		super(Float.MAX_VALUE, c, Effect.NEGATIVE, descriptionp, casterlevelp,
				hours);
	}

	public Fatigued(Combatant c, Integer casterlevelp, Integer hours) {
		this(c, "fatigued", casterlevelp, hours);
	}

	@Override
	public void start(Combatant c) {
		c.source = c.source.clone();
		c.source.raisestrength(-1);
		c.source.raiseconstitution(c, -1);
	}

	@Override
	public void end(Combatant c) {
		c.source.raisestrength(+1);
		c.source.raiseconstitution(c, +1);
	}

}
