package javelin.controller.upgrade.feat;

import javelin.controller.upgrade.Upgrade;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

abstract class FeatUpgrade extends Upgrade {
	protected final Feat feat;
	Feat prerequisite = null;
	boolean stack;

	@Deprecated
	FeatUpgrade(final String name, final Feat featp) {
		super(name);
		feat = featp;
	}

	public FeatUpgrade(Feat featp) {
		this(
				featp.name.substring(0, 1).toUpperCase()
						+ featp.name.substring(1), featp);
	}

	@Override
	public boolean apply(final Combatant m) {
		if (!stack && check(m, feat)) {
			return false;
		}
		if (prerequisite != null && !check(m, prerequisite)) {
			return false;
		}
		m.source.addfeat(feat);
		return true;
	}

	public boolean check(final Combatant m, Feat feat2) {
		return m.source.hasfeat(feat2) > 0;
	}
}