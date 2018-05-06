package javelin.model.unit.condition;

import javelin.model.unit.Combatant;

/**
 * TODO should affect ability checks, better left for when these are enum-based
 * and easier to add a "ability obnus" for ability checks in specific (not
 * ability scores!)
 * 
 * @author alex
 */
public class Sickened extends Condition {

	public Sickened(float expireatp, Combatant c) {
		super(expireatp, c, Effect.NEGATIVE, "sickened", null);
	}

	@Override
	public void start(Combatant c) {
		c.source = c.source.clone();
		Heroic.raiseallattacks(c.source, -2, -2);
		Heroic.raisesaves(c.source, -2);
		c.source.skills.changeall(-2);
	}

	@Override
	public void end(Combatant c) {
		c.source = c.source.clone();
		Heroic.raiseallattacks(c.source, +2, +2);
		Heroic.raisesaves(c.source, +2);
		c.source.skills.changeall(+2);
	}
}
