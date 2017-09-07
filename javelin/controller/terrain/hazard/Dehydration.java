package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Fatigued;
import tyrant.mikera.engine.RPG;

/**
 * Damages characters if Squad has no resource reserves (gold) left.
 * 
 * @author alex
 */
public class Dehydration extends PartyHazard {
	@Override
	protected String affect(Combatant c, int hoursellapsed) {
		c.hp -= RPG.r(1, 6);
		if (c.hp < 1) {
			c.hp = 1;
		}
		c.addcondition(new Fatigued(c, null, 8));
		return c + " is dehydratading";
	}

	@Override
	protected boolean save(int hoursellapsed, Combatant c) {
		return c.hp == 1
				|| c.source.save(c.source.fortitude(), 10 + hoursellapsed / 2);
	}

	@Override
	public boolean validate() {
		return Squad.active.gold == 0
				&& Javelin.roll(Squad.active.survive()) < 25;
	}
}