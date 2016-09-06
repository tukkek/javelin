package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.feat.BullRushUpgrade;
import javelin.controller.upgrade.feat.CleaveUpgrade;
import javelin.controller.upgrade.feat.CombatExpertiseUpgrade;
import javelin.controller.upgrade.feat.FeatUpgrade;
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
import javelin.model.feat.skill.Alertness;
import javelin.model.feat.skill.Deceitful;
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
		handler.earth.add(new GreatFortitude());

		handler.wind.add(new RangedFocus("Ranged focus"));
		handler.wind.add(new LightningReflexes());
		handler.wind.add(new ImprovedInititative());

		handler.fire.add(new IronWill());
		handler.fire.add(new MeleeFocus("Mêlée focus"));

		handler.good.add(new FeatUpgrade(Alertness.INSTANCE));

		handler.evil.add(new FeatUpgrade(Deceitful.SINGLETON));

		handler.shots.add(new PointBlankShot());
		handler.shots.add(new PreciseShot());
		handler.shots.add(new ImprovedPreciseShot());
		handler.shots.add(new RapidShot());

		handler.power.add(new PowerAttackUpgrade());
		handler.power.add(new BullRushUpgrade());
		handler.power.add(new CleaveUpgrade());
		handler.power.add(new GreatCleaveUpgrade());

		handler.expertise.add(new CombatExpertiseUpgrade());
		handler.expertise.add(new ImprovedFeintUpgrade());
		handler.expertise.add(new ImprovedGrappleUpgrade());
		handler.expertise.add(new ImprovedTripUpgrade());
	}
}
