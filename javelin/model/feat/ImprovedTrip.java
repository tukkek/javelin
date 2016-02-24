package javelin.model.feat;

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
