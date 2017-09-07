package javelin.model.unit.feat.attack;

import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 * 
 * @see Cleave
 */
public class GreatCleave extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final GreatCleave SINGLETON = new GreatCleave();

	/** Constructor. */
	private GreatCleave() {
		super("Great cleave");
		prerequisite = Cleave.SINGLETON;
	}

	@Override
	public String inform(Combatant c) {
		return "Base attack bonus: +" + c.source.getbaseattackbonus();
	}

	@Override
	public boolean apply(Combatant m) {
		return m.source.getbaseattackbonus() >= 4 && super.apply(m);
	}
}
