package javelin.model.feat.attack;

import javelin.model.feat.Feat;

/**
 * @see ImprovedGrapple
 * @author alex
 */
public class ImprovedFeint extends Feat {

	public static ImprovedFeint singleton;

	public ImprovedFeint() {
		super("Improved feint");
		ImprovedFeint.singleton = this;
	}
}
