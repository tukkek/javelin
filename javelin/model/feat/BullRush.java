package javelin.model.feat;

/**
 * This works a bit differently than in the official rules: on a successful
 * charge it automatically pushes the opponent back 5 feet, causing no attacks
 * of opportunity. Not otherwise available to prevent overloading the AI with
 * options.
 * 
 * @author alex
 */
public class BullRush extends Feat {
	public static final BullRush SINGLETON = new BullRush();

	public BullRush() {
		super("Improved bull rush");
	}
}
