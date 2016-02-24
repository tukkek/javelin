package javelin.controller.upgrade.feat;

import javelin.controller.upgrade.Upgrade;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * Adds a feat as an upgrade.
 * 
 * @author alex
 */
public abstract class FeatUpgrade extends Upgrade {
	protected final Feat feat;
	Feat prerequisite = null;
	boolean stack = false;

	@Deprecated
	FeatUpgrade(final String name, final Feat featp) {
		super(name);
		feat = featp;
	}

	public FeatUpgrade(Feat featp) {
		this(featp.name.substring(0, 1).toUpperCase() + featp.name.substring(1),
				featp);
	}

	@Override
	public boolean apply(final Combatant c) {
		if (!stack && check(c, feat)) {
			return false;
		}
		if (prerequisite != null && !check(c, prerequisite)) {
			return false;
		}
		c.source.addfeat(feat);
		return true;
	}

	public boolean check(final Combatant c, Feat f) {
		return c.source.hasfeat(f);
	}
}