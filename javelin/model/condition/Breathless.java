package javelin.model.condition;

import javelin.model.unit.Combatant;

public class Breathless extends Condition {

	public Breathless(float expireatp, Combatant c) {
		super(expireatp, c);
	}

	@Override
	void start(Combatant c) {
	}

	@Override
	void end(Combatant c) {
	}

	@Override
	public String describe() {
		return "breathless";
	}

}
