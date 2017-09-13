package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Fatigued;
import javelin.model.world.Season;
import tyrant.mikera.engine.RPG;

/**
 * Extreme heat. Happens during {@link Javelin#PERIODNOON} and also during
 * {@link Javelin#PERIODMORNING} in the {@link Season#SUMMER}.
 * 
 * @author alex
 */
public class Heat extends PartyHazard {
	@Override
	public boolean validate() {
		int level = 0;
		if (Season.current == Season.SUMMER) {
			level += 2;
		} else if (Season.current == Season.WINTER) {
			level -= 2;
		}
		if (Javelin.getDayPeriod() == Javelin.PERIODNOON) {
			level += 1;
		} else if (Javelin.getDayPeriod() != Javelin.PERIODMORNING) {
			level -= 1;
		}
		/* doesn't rain on the desert so don't consider weather */
		return level >= 2;
	}

	@Override
	protected boolean save(int hoursellapsed, Combatant c) {
		return c.hp == 1
				|| c.source.save(c.source.fortitude(), 15 + hoursellapsed / 2);
	}

	@Override
	protected String affect(Combatant c, int hoursellapsed) {
		c.damage(RPG.r(1, 4) * hoursellapsed);
		c.addcondition(new Fatigued(c, null, 8));
		return c + " is suffering from heatstroke";
	}
}