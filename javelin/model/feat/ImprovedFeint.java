package javelin.model.feat;

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
