package javelin.controller.terrain.hazard;

import javelin.controller.Weather;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import tyrant.mikera.engine.RPG;

/**
 * During a storm a tree may fall - it's easy to avoid but causes massive damage
 * otherwise.
 * 
 * @author alex
 */
public class FallingTrees extends PartyHazard {

	@Override
	public boolean validate() {
		return Weather.current == Weather.STORM && !Squad.active.fly();
	}

	@Override
	protected boolean save(int hoursellapsed, Combatant c) {
		return c.source.save(c.source.ref, 12) && c.hp > 1;
	}

	@Override
	protected String affect(Combatant c, int hoursellapsed) {
		for (int i = 0; i < 6; i++) {
			c.hp -= RPG.r(1, 6);
		}
		if (c.hp < 1) {
			c.hp = 1;
		}
		return c + " gets hit by a falling tree";
	}

}
