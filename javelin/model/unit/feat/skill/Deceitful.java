package javelin.model.unit.feat.skill;

import javelin.model.unit.Skills;
import javelin.model.unit.feat.Feat;

/**
 * @see Skills#disguise
 * @author alex
 */
public class Deceitful extends Feat {
	/** Unique instance of this feat. */
	public static final Feat SINGLETON = new Deceitful();

	private Deceitful() {
		super("Deceitful");
		arena = false;
	}
}
