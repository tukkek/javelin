package javelin.view.screen.town.option;

import java.io.Serializable;

public class Option implements Serializable {
	public double price;
	public String name;
	public boolean hidepricatag = false;

	public Option(final String name, final double d) {
		super();
		this.name = name;
		price = d;
	}
}