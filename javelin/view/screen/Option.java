package javelin.view.screen;

import java.io.Serializable;

import javelin.view.screen.town.SelectScreen;

/**
 * @see SelectScreen
 *
 * @author alex
 */
public class Option implements Serializable {
	/** Cost. */
	public double price;
	/** Description. */
	public String name;
	/** Corresponding input. */
	public Character key;
	/**
	 * This is the first sorting consideration. Lower number will appear first
	 * on the {@link SelectScreen} list.
	 *
	 * @see #sort()
	 */
	public float priority = 1;

	/** Constructor. */
	public Option(final String name, final double d, Character keyp) {
		super();
		this.name = name;
		price = d;
		key = keyp;
	}

	/**
	 * This is the second sorting priority. It's used to compare between Options
	 * with the same {@link #priority}.
	 *
	 * @return A value that is then fed to {@link Double#compareTo(Double)}.
	 */
	public double sort() {
		return price;
	}

	/**
	 * Same as {@link #Option(String, double, Character)} but handles input
	 * internally.
	 */
	public Option(String name, double price) {
		this(name, price, null);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(((Option) obj).name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}