package javelin.model.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Stunned extends Condition {

	public Stunned(Combatant c, Integer casterlevelp) {
		super(c.ap + 1, c, Effect.NEGATIVE, "stunned", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.ap += 1;
		c.acmodifier -= 2 + getbonus(c);
	}

	int getbonus(Combatant c) {
		return Math.max(0, Monster.getbonus(c.source.dexterity));
	}

	@Override
	public void end(Combatant c) {
		c.acmodifier += 2 + getbonus(c);
	}

}
