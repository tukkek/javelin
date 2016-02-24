package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class PointBlankShot extends Feat {

	public PointBlankShot() {
		super("Point blank shot");
	}

	public static final Feat SINGLETON = new PointBlankShot();

}
