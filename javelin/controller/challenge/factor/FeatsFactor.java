package javelin.controller.challenge.factor;

import java.util.HashSet;
import java.util.List;

import javelin.controller.content.upgrade.FeatUpgrade;
import javelin.controller.content.upgrade.Upgrade;
import javelin.model.unit.HD;
import javelin.model.unit.Monster;
import javelin.model.unit.feat.Feat;
import javelin.model.unit.feat.attack.focus.WeaponFocus;
import javelin.model.unit.feat.internal.ExoticWeaponProficiency;
import javelin.model.unit.feat.internal.Multiattack;
import javelin.model.unit.feat.internal.MultiweaponFighting;
import javelin.model.unit.feat.internal.WeaponFinesse;

/**
 * @see CrFactor
 */
public class FeatsFactor extends CrFactor{
	static final float CR=.2f;

	/**
	 * Internal feats are mostly used to map feats that should be considered for
	 * {@link #CR} purposes but whose effects come pre-calculated on the stat
	 * sheets. They basically only need to be "acknowledged" by Javelin.
	 */
	public static final List<Feat> INTERNAL=List.of(WeaponFocus.SINGLETON,
			ExoticWeaponProficiency.SINGLETON,Multiattack.SINGLETON,
			MultiweaponFighting.SINGLETON,WeaponFinesse.SINGLETON);

	static{
		/*
		 * TODO need a similar field for internal, which can then go to null
		 * #arena as well by default. this is necessary for example to filter
		 * useless feats from the Academy. Alternatively, make it a list and
		 * just use contains().
		 */
		for(Feat f:INTERNAL)
			f.arena=false;
	}

	@Override
	public float calculate(final Monster m){
		return m.feats.count()*CR-getnormalprogression(m)*CR;
	}

	/**
	 * @return The number of {@link Feat}s this monster should/could have based on
	 *         its {@link HD#count()}.
	 */
	static public int getnormalprogression(final Monster m){
		return 1+Math.round(Math.round(Math.floor(m.hd.count()/3f)));
	}

	void register(HashSet<Upgrade> upgrades,Feat[] feats){
		for(Feat f:feats)
			upgrades.add(new FeatUpgrade(f));
	}

	@Override
	public String log(Monster m){
		return m.feats.isEmpty()?"":m.feats.toString();
	}
}
