package javelin.model.unit.feat.skill;

import javelin.model.unit.Skills;
import javelin.model.unit.feat.Feat;

/**
 * +2 on perception and sense motive rolls by Pathfinder rules.
 *
 * @see Skills
 * @author alex
 */
public class Alertness extends Feat {
	/** Single instance. */
	public static final Feat SINGLETON = new Alertness();

	/** Constructor. */
	private Alertness() {
		super("alertness");
		arena = false;
	}
}
