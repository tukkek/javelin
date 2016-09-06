package javelin.model.world.location.unique;

import java.util.HashSet;

import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.ability.RaiseCharisma;
import javelin.controller.upgrade.ability.RaiseDexterity;
import javelin.controller.upgrade.classes.Expert;
import javelin.controller.upgrade.feat.FeatUpgrade;
import javelin.controller.upgrade.skill.Disguise;
import javelin.controller.upgrade.skill.Stealth;
import javelin.model.feat.skill.Deceitful;
import javelin.model.world.location.fortification.Academy;

/**
 * An academy dedicated to learning how to Infiltrate.
 * 
 * @author alex
 */
public class AssassinsGuild extends Academy {
	static final String DESCRITPION = "Assassins guild";

	/** Constructor. */
	public AssassinsGuild() {
		super(DESCRITPION, DESCRITPION, 6, 10, new HashSet<Upgrade>());
		upgrades.add(Disguise.SINGLETON);
		upgrades.add(Stealth.SINGLETON);
		upgrades.add(RaiseCharisma.INSTANCE);
		upgrades.add(RaiseDexterity.SINGLETON);
		upgrades.add(new FeatUpgrade(Deceitful.SINGLETON));
		upgrades.add(Expert.SINGLETON);
		sort(upgrades);
	}

	@Override
	protected void generate() {
		while (x < 0 || Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL)) {
			super.generate();
		}
	}
}
