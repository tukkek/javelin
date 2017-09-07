package javelin.model.unit.feat.attack;

import javelin.controller.action.ai.attack.AbstractAttack;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

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
