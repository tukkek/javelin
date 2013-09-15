package javelin.model.condition;

import javelin.model.unit.Combatant;

public class Charging extends Condition {

	public Charging(float expireat, Combatant c) {
		super(expireat, c);
	}

	@Override
	void start(Combatant c) {
		c.acmodifier -= 2;
	}

	@Override
	void end(Combatant c) {
		c.acmodifier += 2;
	}

	@Override
	public String describe() {
		return "charging";
	}

}
