package javelin.controller.challenge.factor;

import java.util.ArrayList;

import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.feat.GreatFortitude;
import javelin.controller.upgrade.feat.ImprovedInititative;
import javelin.controller.upgrade.feat.ImprovedPreciseShot;
import javelin.controller.upgrade.feat.IronWill;
import javelin.controller.upgrade.feat.LightningReflexes;
import javelin.controller.upgrade.feat.MeleeFocus;
import javelin.controller.upgrade.feat.PointBlankShot;
import javelin.controller.upgrade.feat.PreciseShot;
import javelin.controller.upgrade.feat.RangedFocus;
import javelin.controller.upgrade.feat.RapidShot;
import javelin.controller.upgrade.feat.Toughness;
import javelin.model.feat.ImprovedInitiative;
import javelin.model.unit.Monster;

public class FeatsFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		final long normalprogression = 1 + Math.round(Math
				.floor(monster.originalhd / 3.0));
		final long extra = monster.countfeats() - normalprogression;
		return new Float(extra * .2);
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.defensive.add(new Toughness("Toughness",
				javelin.model.feat.Toughness.singleton));

		ArrayList<Upgrade> saves = handler.addset();
		saves.add(new IronWill());
		saves.add(new LightningReflexes());
		saves.add(new GreatFortitude());

		ArrayList<Upgrade> ranged = handler.addset();
		ranged.add(new PointBlankShot());
		ranged.add(new PreciseShot());
		ranged.add(new ImprovedPreciseShot());
		ranged.add(new RapidShot());
		ranged.add(new RangedFocus("Ranged focus"));

		handler.misc.add(new MeleeFocus("Mêlée focus"));
		handler.misc.add(new ImprovedInititative("Improved initiative",
				ImprovedInitiative.singleton));
	}
}
