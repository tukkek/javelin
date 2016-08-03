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

	/** Constructor. */
	public Option(final String name, final double d, Character keyp) {
		super();
		this.name = name;
		price = d;
		key = keyp;
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
}