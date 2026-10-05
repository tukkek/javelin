package javelin.model.unit;

/**
 * Utility class for {@link Monster#size}-related values.
 *
 * @author alex
 */
public class Size{
	/** All valid sizes. */
	public static String[] SIZES={"fine","diminutive","tiny","small",
			"medium-size","large","huge","gargantuan","colossal"};
	/** @see Monster#size */
	public static final int FINE=0;
	/** @see Monster#size */
	public static final int DIMINUTIVE=1;
	/** @see Monster#size */
	public static final int TINY=2;
	/** @see Monster#size */
	public static final int SMALL=3;
	/** @see Monster#size */
	public static final int MEDIUM=4;
	/** @see Monster#size */
	public static final int LARGE=5;
	/** @see Monster#size */
	public static final int HUGE=6;
	/** @see Monster#size */
	public static final int GARGANTUAN=7;
	/** @see Monster#size */
	public static final int COLOSSAL=8;
}
