package javelin.model.feat.skill;

import javelin.model.feat.Feat;
import javelin.model.unit.Skills;

/**
 * +2 on perception and sense motive rolls by Pathfinder rules.
 * 
 * @see Skills
 * @author alex
 */
public class Alertness extends Feat {
	/** Single instance. */
	public static final Feat INSTANCE = new Alertness();
	/** +1 since we don't support Sense Motive in the game. */
	public static final int BONUS = 3;

	/** Constructor. */
	public Alertness() {
		super("alertness");
	}
}
