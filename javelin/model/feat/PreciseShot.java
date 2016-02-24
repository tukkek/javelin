package javelin.model.feat;

/**
 * See the d20 SRD for more info.
 */
public class PreciseShot extends Feat {

	public PreciseShot() {
		super("Precise shot");
	}

	public static final Feat SINGLETON = new PreciseShot();

}
