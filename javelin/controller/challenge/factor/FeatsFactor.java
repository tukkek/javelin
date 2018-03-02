package javelin.controller.challenge.factor;

import java.util.HashSet;

import javelin.controller.upgrade.FeatUpgrade;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.CombatCasting;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.ImprovedInitiative;
import javelin.model.unit.feat.Toughness;
import javelin.model.unit.feat.attack.BullRush;
import javelin.model.unit.feat.attack.Cleave;
import javelin.model.unit.feat.attack.ExoticWeaponProficiency;
import javelin.model.unit.feat.attack.GreatCleave;
import javelin.model.unit.feat.attack.Multiattack;
import javelin.model.unit.feat.attack.MultiweaponFighting;
import javelin.model.unit.feat.attack.PowerAttack;
import javelin.model.unit.feat.attack.WeaponFinesse;
import javelin.model.unit.feat.attack.expertise.CombatExpertise;
import javelin.model.unit.feat.attack.expertise.ImprovedFeint;
import javelin.model.unit.feat.attack.expertise.ImprovedGrapple;
import javelin.model.unit.feat.attack.expertise.ImprovedTrip;
import javelin.model.unit.feat.attack.focus.MeleeFocus;
import javelin.model.unit.feat.attack.focus.RangedFocus;
import javelin.model.unit.feat.attack.focus.WeaponFocus;
import javelin.model.unit.feat.attack.shot.ImprovedPreciseShot;
import javelin.model.unit.feat.attack.shot.PointBlankShot;
import javelin.model.unit.feat.attack.shot.PreciseShot;
import javelin.model.unit.feat.attack.shot.RapidShot;
import javelin.model.unit.feat.save.GreatFortitude;
import javelin.model.unit.feat.save.IronWill;
import javelin.model.unit.feat.save.LightningReflexes;
import javelin.model.unit.feat.skill.Acrobatic;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.feat.skill.Deceitful;

/**
 * @see CrFactor
 */
public class FeatsFactor extends CrFactor {
	public static final float CR = .2f;

	static final Feat[] EVIL = new Feat[] { Deceitful.SINGLETON };

	static final Feat[] GOOD = new Feat[] { Alertness.SINGLETON };

	static final Feat[] FIRE = new Feat[] { IronWill.SINGLETON,
			MeleeFocus.SINGLETON };

	static final Feat[] WIND = new Feat[] { RangedFocus.SINGLETON,
			LightningReflexes.SINGLETON, ImprovedInitiative.SINGLETON };

	static final Feat[] EARTH = new Feat[] { Toughness.SINGLETON,
			GreatFortitude.SINGLETON, CombatCasting.SINGLETON };

	static final Feat[] WATER = new Feat[] { Acrobatic.SINGLETON };

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

	static {
		/*
		 * TODO need a similar field for internal, which can then go to null
		 * #arena as well by default. this is necessary for example to filter
		 * useless feats from the Academy. Alternatively, make it a list and
		 * just use contains().
		 */
		for (Feat f : INTERNAL) {
			f.arena = false;
		}
	}

	@Override
	public float calculate(final Monster m) {
		return m.feats.count() * CR - getnormalprogression(m) * CR;
	}

	static public int getnormalprogression(final Monster m) {
		return 1 + Math.round(Math.round(Math.floor(m.hd.count() / 3f)));
	}

	@Override
	public void registerupgrades(UpgradeHandler handler) {
		register(handler.earth, EARTH);
		register(handler.wind, WIND);
		register(handler.fire, FIRE);
		register(handler.water, WATER);
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

	@Override
	public String log(Monster m) {
		return m.feats.isEmpty() ? "" : m.feats.toString();
	}
}
