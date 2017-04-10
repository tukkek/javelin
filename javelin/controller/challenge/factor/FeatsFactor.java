package javelin.controller.challenge.factor;

import java.util.HashSet;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.feat.CombatCasting;
import javelin.model.feat.Feat;
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
import javelin.model.feat.attack.focus.MeleeFocus;
import javelin.model.feat.attack.focus.RangedFocus;
import javelin.model.feat.attack.focus.WeaponFocus;
import javelin.model.feat.attack.martial.CombatExpertise;
import javelin.model.feat.attack.martial.ImprovedFeint;
import javelin.model.feat.attack.martial.ImprovedGrapple;
import javelin.model.feat.attack.martial.ImprovedTrip;
import javelin.model.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.feat.attack.shot.PointBlankShot;
import javelin.model.feat.attack.shot.PreciseShot;
import javelin.model.feat.attack.shot.RapidShot;
import javelin.model.feat.save.GreatFortitude;
import javelin.model.feat.save.IronWill;
import javelin.model.feat.save.LightningReflexes;
import javelin.model.feat.skill.Alertness;
import javelin.model.feat.skill.Deceitful;
import javelin.model.unit.Monster;

/**
 * @see CrFactor
 */
public class FeatsFactor extends CrFactor {
	static final Feat[] EVIL = new Feat[] { Deceitful.SINGLETON };

	static final Feat[] GOOD = new Feat[] { Alertness.SINGLETON };

	static final Feat[] FIRE = new Feat[] { IronWill.SINGLETON,
			MeleeFocus.SINGLETON };

	static final Feat[] WIND = new Feat[] { RangedFocus.SINGLETON,
			LightningReflexes.singleton, ImprovedInitiative.SINGLETON };

	static final Feat[] EARTH = new Feat[] { Toughness.SINGLETON,
			GreatFortitude.SINGLETON, CombatCasting.SINGLETON };

	static final Feat[] EXPERTISE = new Feat[] { CombatExpertise.SINGLETON,
			ImprovedFeint.SINGLETON, ImprovedGrapple.SINGLETON,
			ImprovedTrip.SINGLETON };

	static final Feat[] POWER = new Feat[] { PowerAttack.SINGLETON,
			BullRush.SINGLETON, Cleave.SINGLETON, GreatCleave.SINGLETON };

	static final Feat[] SHOTS = new Feat[] { PointBlankShot.SINGLETON,
			PreciseShot.SINGLETON, ImprovedPreciseShot.SINGLETON,
			RapidShot.SINGLETON };

	static final Feat[] INTERNAL = new Feat[] { WeaponFocus.SINGLETON,
			ExoticWeaponProficiency.SINGLETON, Multiattack.SINGLETON,
			MultiweaponFighting.SINGLETON, WeaponFinesse.SINGLETON };

	@Override
	public float calculate(final Monster monster) {
		final long normalprogression = 1
				+ Math.round(Math.floor(monster.originalhd / 3.0));
		final long extra = monster.countfeats() - normalprogression;
		return extra * .2f;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		register(handler.earth, EARTH);
		register(handler.wind, WIND);
		register(handler.fire, FIRE);
		register(handler.good, GOOD);
		register(handler.evil, EVIL);
		register(handler.shots, SHOTS);
		register(handler.power, POWER);
		register(handler.expertise, EXPERTISE);
		register(handler.internal, INTERNAL);
	}

	private void register(HashSet<Upgrade> upgrades, Feat[] feats) {
		for (Feat f : feats) {
			upgrades.add(new FeatUpgrade(f));
		}
	}
}
