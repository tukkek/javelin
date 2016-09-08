package javelin.model.feat.attack;

import javelin.controller.action.ai.AbstractAttack;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 * 
 * @see AbstractAttack#cleave()
 */
public class Cleave extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Cleave SINGLETON = new Cleave();

	/** Constructor. */
	private Cleave() {
		super("Cleave");
		prerequisite = PowerAttack.SINGLETON;
	}

	@Override
	public String inform(Combatant c) {
		return "";
	}

}
