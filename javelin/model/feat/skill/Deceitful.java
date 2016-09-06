package javelin.model.feat.skill;

import javelin.model.feat.Feat;
import javelin.model.unit.Skills;

/**
 * @see Skills#disguise
 * @author alex
 */
public class Deceitful extends Feat {
	/** Unique instance of this feat. */
	public static final Feat SINGLETON = new Deceitful();

	private Deceitful() {
		super("Deceitful");
	}
}
