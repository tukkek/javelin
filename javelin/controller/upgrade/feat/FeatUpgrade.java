package javelin.controller.upgrade.feat;

import javelin.controller.ai.BattleAi;
import javelin.controller.upgrade.Upgrade;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Adds a feat as an upgrade. It can be used by just giving a {@link Feat} to
 * the constructor but most feats usually need more setup, be it due to
 * prerequisites (other feats, ability scores, base attack bonus...),
 * modifications to be introduced during {@link #apply(Combatant)}, etc.
 * 
 * Probably better to keep this separate from {@link Feat} to ensure minimum
 * weitght for {@link Monster}s when being processed by the {@link BattleAi}.
 * 
 * @author alex
 */
public class FeatUpgrade extends Upgrade {
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

	@Override
	public String info(Combatant c) {
		int count = c.source.countfeat(feat);
		String name = feat.toString().toLowerCase();
		if (count == 0) {
			return c + " doesn't have " + name;
		} else {
			return c + " has bought " + name + " " + count + " times";
		}
	}
}