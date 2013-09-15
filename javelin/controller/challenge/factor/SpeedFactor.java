package javelin.controller.challenge.factor;

import java.util.List;

import javelin.controller.upgrade.Flying;
import javelin.controller.upgrade.Swimming;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.WalkingSpeed;
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
 * @author alex
 */
public class SpeedFactor extends CrFactor {
	public static final int[] TYPICAL_SPEED = new int[] { 5, 10, 15, 20, 30,
			40, 50, 60, 70 };
	private static final int[] TYPICAL_FLIGHT_SPEED = new int[] { 10, 20, 30,
			40, 60, 80, 100, 120, 140 };

	@Override
	public float calculate(Monster m) {
		long speed = m.fly;
		boolean isflying = speed > 0;
		float cr = 0;
		if (isflying) {
			cr += .6f;
		} else {
			if (m.swim > 0) {
				/*
				 * flying has the same benefits of swimming and more so it
				 * superseeds it
				 */
				cr += .2f;
			}
			speed = m.walk > m.swim ? m.walk : m.swim;
		}
		if (speed == 0) {
			throw new RuntimeException("No speed for " + m);
		}
		int typical = (isflying ? TYPICAL_FLIGHT_SPEED : TYPICAL_SPEED)[m.size];
		if (speed == typical) {
			return cr;
		}
		if (speed > typical) {
			cr += .2 * (speed / typical) / 2;
		} else {
			cr -= .2 * (typical / speed) / 2;
		}
		return cr;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		final List<Upgrade> walk = handler.addset();
		walk.add(new WalkingSpeed("Walking speed: human", 30));
		walk.add(new WalkingSpeed("Walking speed: cheetah", 50));
		walk.add(new Flying("Flying: hawk", 60));
		walk.add(new Flying("Flying: pegasus", 100));
		walk.add(new Flying("Flying: dragon", 150));
		walk.add(new Swimming("Swimming: snake", 20));
		walk.add(new Swimming("Swimming: sea lion", 40));
		walk.add(new Swimming("Swimming: shark", 60));
	}
}
