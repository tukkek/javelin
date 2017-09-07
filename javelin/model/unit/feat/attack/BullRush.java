package javelin.model.unit.feat.attack;

import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;

/**
 * This works a bit differently than in the official rules: on a successful
 * charge it automatically pushes the opponent back 5 feet, causing no attacks
 * of opportunity. Not otherwise available to prevent overloading the AI with
 * options.
 * 
 * @author alex
 */
public class BullRush extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final BullRush SINGLETON = new BullRush();

	/** Constructor. */
	private BullRush() {
		super("Improved bull rush");
		prerequisite = PowerAttack.SINGLETON;
	}

	@Override
	public String inform(Combatant c) {
		return "";
	}
}
