package javelin.model.condition;

import javelin.model.unit.Combatant;

public class Defending extends Condition {

	public Defending(float expireatp, Combatant c) {
		super(expireatp, c);
	}

	@Override
	void start(Combatant c) {
		c.acmodifier += 4;
	}

	@Override
	void end(Combatant c) {
		c.acmodifier -= 4;
	}

	@Override
	public String describe() {
		return "defending";
	}

}
