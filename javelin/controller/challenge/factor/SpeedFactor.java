package javelin.controller.challenge.factor;

import javelin.controller.upgrade.Swimming;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.movement.Burrow;
import javelin.controller.upgrade.movement.Flying;
import javelin.controller.upgrade.movement.WalkingSpeed;
import javelin.model.unit.Monster;

/**
 * Only walk and fly are supported.
 * 
 * Burrow would be interesting but it could be a big change and I can imagine
 * the AI not wanting to unburrow (if it was done in a similar fashion to
 * Starcraft 2).
 * 
 * Swim would only be useful on a single map right now and only in a small area.
 * 
 * Fly means that the character can fly over any obstacle but cannot really fly
 * high up enough for 3D combat mechanics to be needed. This of course is just
 * an approximation to the d20 system but is simpler, fun and easier to code.
 * Since this approach has about the same number of benefits from good/perfect
 * flight maneuverabilty as deficits from poor/clumsy the CR value is
 * approximated to the average: .6 for flight.
 * 
 * @see CrFactor
 * 
 * @author alex
 */
public class SpeedFactor extends CrFactor {
	public static final int[] TYPICAL_SPEED =
			new int[] { 5, 10, 15, 20, 30, 40, 50, 60, 70 };
	private static final int[] TYPICAL_FLIGHT_SPEED =
			new int[] { 10, 20, 30, 40, 60, 80, 100, 120, 140 };

	@Override
	public float calculate(Monster m) {
		long speed;
		float cr;
		if (m.fly > 0) {
			/*
			 * flying has the same benefits of swimming and more so it
			 * supersedes it
			 */
			cr = .6f;
			speed = m.fly;
		} else if (m.swim > 0) {
			speed = Math.max(m.swim, m.walk);
			cr = .2f;
		} else {
			cr = 0;
			speed = m.walk;
		}
		if (m.burrow > 0) {
			cr += .2f;
			speed = Math.max(speed, m.burrow);
		}
		if (speed == 0) {
			throw new RuntimeException("No speed for " + m);
		}
		int typical =
				(m.fly > 0 ? TYPICAL_FLIGHT_SPEED : TYPICAL_SPEED)[m.size];
		if (speed == typical) {
			return cr;
		}
		if (speed > typical) {
			return cr + (.2f * (speed / typical) / 2f);
		} else {
			return cr - (.2f * (typical / speed) / 2f);
		}
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(new WalkingSpeed("Speed: human", 30));
		handler.earth.add(new WalkingSpeed("Speed: cheetah", 50));
		handler.wind.add(new Flying("Flying: raven", 40));
		handler.water.add(new Swimming("Swimming: snake", 20));
		handler.earth.add(new Burrow("Burrow: badger", 10));
	}
}
