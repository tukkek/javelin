package javelin.model.condition;

import javelin.model.spell.enchantment.compulsion.HoldMonster;
import javelin.model.unit.Combatant;

/**
 * @see HoldMonster
 * @author alex
 */
public class Paralyzed extends Condition {

	private int dex;
	private int delta;

	public Paralyzed(float expireatp, Combatant c, Integer casterlevelp) {
		super(expireatp, c, Effect.NEGATIVE, "paralyzed", casterlevelp);
	}

	@Override
	public void start(Combatant c) {
		c.source = c.source.clone();
		dex = c.source.dexterity;
		delta = (int) Math.round(Math.floor(dex / 2f));
		c.source.raisedexterity(-delta);
		c.ap = expireat;
	}

	@Override
	public void end(Combatant c) {
		c.source = c.source.clone();
		c.source.raisedexterity(+delta);
	}
}
