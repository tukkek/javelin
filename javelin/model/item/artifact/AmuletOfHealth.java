package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises {@link Monster#constitution}
 *
 * @author alex
 */
public class AmuletOfHealth extends Artifact {
	int bonus;

	/** Constructor. */
	public AmuletOfHealth(int bonus, int price) {
		super("Amulet of health +" + bonus, price, Slot.COLLAR);
		this.bonus = bonus;
	}

	@Override
	protected void apply(Combatant c) {
		c.source.raiseconstitution(c, bonus / 2);
	}

	@Override
	protected void negate(Combatant c) {
		c.source.raiseconstitution(c, -bonus / 2);
	}

}
