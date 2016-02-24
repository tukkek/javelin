package javelin.view.screen.town.option;

import java.io.Serializable;

import javelin.view.screen.town.SelectScreen;

/**
 * @see SelectScreen
 * 
 * @author alex
 */
public class Option implements Serializable {
	public double price;
	public String name;

	public Option(final String name, final double d) {
		super();
		this.name = name;
		price = d;
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