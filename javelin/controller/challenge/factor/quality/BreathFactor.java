package javelin.controller.challenge.factor.quality;

import javelin.controller.challenge.factor.CrFactor;
import javelin.controller.upgrade.BreathUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.BreathWeapon;
import javelin.model.unit.abilities.BreathWeapon.BreathArea;
import javelin.model.unit.abilities.BreathWeapon.SavingThrow;

/**
 * @see CrFactor
 */
public class BreathFactor extends CrFactor {

	int[] CONERANGE = new int[] { 5, 10, 15, 20, 30, 40, 50, 60, 70 };
	int[] LINERANGE = new int[] { 10, 20, 30, 40, 60, 80, 100, 120, 140 };

	@Override
	public float calculate(final Monster monster) {
		float total = 0;
		for (BreathWeapon breath : monster.breaths) {
			final float damage = breath.damage[0] * (breath.damage[1] + 1) / 2f;
			float breathcr = .03f * damage;
			final float typical = (breath.type == BreathArea.CONE ? CONERANGE
					: LINERANGE)[monster.size];
			float doubling = 0;
			if (breath.range > typical) {
				doubling = breath.range / typical / 2f;
			} else if (breath.range < typical) {
				doubling = -(typical / breath.range) / 2f;
			}
			breathcr += .2f * doubling;
			if (breathcr < 0) {
				breathcr = 0;
			}
			if (!breath.delay) {
				breathcr = breathcr * 1.5f;
			}
			total += breathcr;
		}
		return total;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler) {
		/* Too weak, doesnt resolve to positive CR */
		// handler.fire.add(new BreathUpgrade(
		// new BreathWeapon("cone of magma", BreathArea.CONE, 10, 1, 4, 0,
		// SavingThrow.REFLEXES, 13, .5f, true)));
		handler.water.add(new BreathUpgrade(
				new BreathWeapon("caustic liquid", BreathArea.CONE, 15, 1, 8, 0,
						SavingThrow.REFLEXES, 12, .5f, true)));

		handler.evil
				.add(new BreathUpgrade(new BreathWeapon("acid", BreathArea.LINE,
						80, 12, 4, 0, SavingThrow.REFLEXES, 23, .5f, true)));
		handler.wind.add(
				new BreathUpgrade(new BreathWeapon("lightning", BreathArea.LINE,
						100, 12, 8, 0, SavingThrow.REFLEXES, 25, .5f, true)));
		handler.fire
				.add(new BreathUpgrade(new BreathWeapon("fire", BreathArea.CONE,
						50, 12, 10, 0, SavingThrow.REFLEXES, 25, .5f, true)));
		handler.earth.add(new BreathUpgrade(
				new BreathWeapon("corrosive gas", BreathArea.CONE, 50, 12, 6, 0,
						SavingThrow.REFLEXES, 25, .5f, true)));
		handler.wind
				.add(new BreathUpgrade(new BreathWeapon("cold", BreathArea.CONE,
						40, 6, 6, 0, SavingThrow.REFLEXES, 23, .5f, true)));
	}

	@Override
	public String log(Monster m) {
		return m.breaths.isEmpty() ? "" : m.breaths.toString();
	}
}
