package javelin.controller.terrain.hazard;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.condition.Fatigued;
import javelin.model.unit.skill.Skill;
import tyrant.mikera.engine.RPG;

/**
 * Damages characters if Squad has no resource reserves (gold) left.
 *
 * @author alex
 */
public class Dehydration extends PartyHazard {
	@Override
	protected String affect(Combatant c, int hoursellapsed) {
		c.damage(RPG.r(1, 6));
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
		return Squad.active.gold == 0 && Squad.active.getbest(Skill.SURVIVAL)
				.roll(Skill.SURVIVAL) < 25;
	}
}