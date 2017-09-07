package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Fatigued;
import javelin.model.world.Season;
import tyrant.mikera.engine.RPG;

/**
 * Frostbite.
 * 
 * @author alex
 */
public class Cold extends PartyHazard {
	@Override
	protected boolean save(int hoursellapsed, Combatant c) {
		return c.hp == 1
				|| c.source.save(c.source.fortitude(), 15 + hoursellapsed / 2);
	}

	@Override
	protected String affect(Combatant c, int hoursellapsed) {
		for (int i = 0; i < hoursellapsed; i++) {
			c.hp -= RPG.r(1, 6);
		}
		if (c.hp < 1) {
			c.hp = 1;
		}
		c.addcondition(new Fatigued(c, null, 8));
		return c + " is suffering from frostbite";
	}

	@Override
	public boolean validate() {
		int level = 0;
		if (Season.current == Season.WINTER) {
			level += 2;
		} else if (Season.current == Season.SUMMER) {
			level -= 2;
		}
		if (Weather.current != Weather.DRY) {
			level += 1;
		}
		if (Javelin.getDayPeriod() == Javelin.PERIODNIGHT) {
			level += 1;
		} else if (Javelin.getDayPeriod() == Javelin.PERIODNOON) {
			level -= 1;
		}
		return level >= 2;
	}

}
