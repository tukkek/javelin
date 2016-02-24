package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.feat.BullRushUpgrade;
import javelin.controller.upgrade.feat.CleaveUpgrade;
import javelin.controller.upgrade.feat.CombatExpertiseUpgrade;
import javelin.controller.upgrade.feat.GreatCleaveUpgrade;
import javelin.controller.upgrade.feat.GreatFortitude;
import javelin.controller.upgrade.feat.ImprovedFeintUpgrade;
import javelin.controller.upgrade.feat.ImprovedGrappleUpgrade;
import javelin.controller.upgrade.feat.ImprovedInititative;
import javelin.controller.upgrade.feat.ImprovedPreciseShot;
import javelin.controller.upgrade.feat.ImprovedTripUpgrade;
import javelin.controller.upgrade.feat.IronWill;
import javelin.controller.upgrade.feat.LightningReflexes;
import javelin.controller.upgrade.feat.MeleeFocus;
import javelin.controller.upgrade.feat.PointBlankShot;
import javelin.controller.upgrade.feat.PowerAttackUpgrade;
import javelin.controller.upgrade.feat.PreciseShot;
import javelin.controller.upgrade.feat.RangedFocus;
import javelin.controller.upgrade.feat.RapidShot;
import javelin.controller.upgrade.feat.Toughness;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class FeatsFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		final long normalprogression =
				1 + Math.round(Math.floor(monster.originalhd / 3.0));
		final long extra = monster.countfeats() - normalprogression;
		return extra * .2f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.earth.add(new Toughness());

		handler.water.add(new IronWill());
		handler.wind.add(new LightningReflexes());
		handler.earth.add(new GreatFortitude());

		handler.wind.add(new PointBlankShot());
		handler.wind.add(new PreciseShot());
		handler.wind.add(new ImprovedPreciseShot());
		handler.wind.add(new RapidShot());
		handler.wind.add(new RangedFocus("Ranged focus"));

		handler.fire.add(new MeleeFocus("Mêlée focus"));
		handler.fire.add(new PowerAttackUpgrade());
		handler.fire.add(new CleaveUpgrade());
		handler.fire.add(new GreatCleaveUpgrade());
		handler.fire.add(new BullRushUpgrade());

		handler.fire.add(new CombatExpertiseUpgrade());
		handler.wind.add(new ImprovedFeintUpgrade());
		handler.earth.add(new ImprovedGrappleUpgrade());
		handler.water.add(new ImprovedTripUpgrade());

		handler.wind.add(new ImprovedInititative());
	}
}
