package javelin.model.feat.attack;

import javelin.model.feat.Feat;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedTrip extends Feat {

	public static ImprovedTrip singleton;

	public ImprovedTrip() {
		super("Improved trip");
		ImprovedTrip.singleton = this;
	}

}
