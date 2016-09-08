package javelin.controller.challenge.factor;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.feat.ImprovedInitiative;
import javelin.model.feat.Toughness;
import javelin.model.feat.attack.BullRush;
import javelin.model.feat.attack.Cleave;
import javelin.model.feat.attack.ExoticWeaponProficiency;
import javelin.model.feat.attack.GreatCleave;
import javelin.model.feat.attack.Multiattack;
import javelin.model.feat.attack.MultiweaponFighting;
import javelin.model.feat.attack.PowerAttack;
import javelin.model.feat.attack.WeaponFinesse;
import javelin.model.feat.attack.focus.WeaponFocus;
import javelin.model.feat.attack.martial.CombatExpertise;
import javelin.model.feat.attack.martial.ImprovedFeint;
import javelin.model.feat.attack.martial.ImprovedGrapple;
import javelin.model.feat.attack.martial.ImprovedTrip;
import javelin.model.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.feat.attack.shot.PointBlankShot;
import javelin.model.feat.attack.shot.PreciseShot;
import javelin.model.feat.attack.shot.RapidShot;
import javelin.model.feat.save.IronWill;
import javelin.model.feat.save.LightningReflexes;
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
		handler.earth.add(new FeatUpgrade(Toughness.SINGLETON));
		handler.earth.add(new FeatUpgrade(
				javelin.model.feat.save.GreatFortitude.SINGLETON));

		handler.wind.add(new FeatUpgrade(
				javelin.model.feat.attack.focus.RangedFocus.SINGLETON));
		handler.wind.add(new FeatUpgrade(LightningReflexes.singleton));
		handler.wind.add(new FeatUpgrade(ImprovedInitiative.SINGLETON));

		handler.fire.add(new FeatUpgrade(IronWill.SINGLETON));
		handler.fire.add(new FeatUpgrade(
				javelin.model.feat.attack.focus.MeleeFocus.SINGLETON));

		handler.good.add(new FeatUpgrade(Alertness.SINGLETON));

		handler.evil.add(new FeatUpgrade(Deceitful.SINGLETON));

		handler.shots.add(new FeatUpgrade(PointBlankShot.SINGLETON));
		handler.shots.add(new FeatUpgrade(PreciseShot.SINGLETON));
		handler.shots.add(new FeatUpgrade(ImprovedPreciseShot.SINGLETON));
		handler.shots.add(new FeatUpgrade(RapidShot.SINGLETON));

		handler.power.add(new FeatUpgrade(PowerAttack.SINGLETON));
		handler.power.add(new FeatUpgrade(BullRush.SINGLETON));
		handler.power.add(new FeatUpgrade(Cleave.SINGLETON));
		handler.power.add(new FeatUpgrade(GreatCleave.SINGLETON));

		handler.expertise.add(new FeatUpgrade(CombatExpertise.SINGLETON));
		handler.expertise.add(new FeatUpgrade(ImprovedFeint.SINGLETON));
		handler.expertise.add(new FeatUpgrade(ImprovedGrapple.SINGLETON));
		handler.expertise.add(new FeatUpgrade(ImprovedTrip.SINGLETON));

		handler.internal.add(new FeatUpgrade(WeaponFocus.SINGLETON));
		handler.internal
				.add(new FeatUpgrade(ExoticWeaponProficiency.SINGLETON));
		handler.internal.add(new FeatUpgrade(Multiattack.SINGLETON));
		handler.internal.add(new FeatUpgrade(MultiweaponFighting.SINGLETON));
		handler.internal.add(new FeatUpgrade(WeaponFinesse.SINGLETON));
	}
}
