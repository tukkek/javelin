package javelin.view.screen.town;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.world.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.option.Option;
import tyrant.mikera.tyrant.InfoScreen;

public abstract class SelectScreen extends InfoScreen {

	private static final DecimalFormat COSTFORMAT = new DecimalFormat(
			"####,###,##0");
	static final char PROCEED = 'q';
	final Town town;
	private final boolean printpriceinfo;
	public boolean showtitle = true;
	private final String title;

	public SelectScreen(final String name, final boolean printpriceinfo,
			final Town t) {
		super(Javelin.app, "");
		this.printpriceinfo = printpriceinfo;
		town = t;
		title = name + ":\n\n";
	}

	@Override
	public void show() {
		text = showtitle ? title : "";
		final List<Option> options = getOptions();
		options.addAll(getfixedoptions());
		for (final Option o : options) {
			roundcost(o);
		}
		Collections.sort(options, sort());
		int i = 1;
		for (final Option o : options) {
			String priceinfo = "";
			if (printpriceinfo && !o.hidepricatag) {
				priceinfo = " " + getCurrency() + formatcost(o.price);
			}
			text += "[" + (i == 10 ? 0 : i) + "] " + o.name + priceinfo + "\n";
			i += 1;
		}
		final String extrainfo = printInfo();
		if (!extrainfo.isEmpty()) {
			text += "\n" + extrainfo + "\n";
		}
		text += "\nPress " + PROCEED + " to quit this screen\n";
		IntroScreen.configurescreen(this);
		processinput(options);
		onexit();
	}

	protected Comparator<Option> sort() {
		return new Comparator<Option>() {
			@Override
			public int compare(final Option o1, final Option o2) {
				return new Long(Math.round(o1.price - o2.price)).intValue();
			}
		};
	}

	protected void onexit() {
		return;
	}

	public void processinput(final List<Option> options) {
		Character feedback = ' ';
		while (feedback != PROCEED) {
			Javelin.app.switchScreen(this);
			feedback = IntroScreen.feedback();
			int selected;
			try {
				selected = Integer.parseInt(feedback.toString()) - 1;
				if (selected == -1) {
					selected = 9;
				}
			} catch (final NumberFormatException e) {
				continue;
			}
			if (0 <= selected && selected < options.size()) {
				if (select(options.get(selected))) {
					return;
				}
			}
		}
	}

	void roundcost(final Option o) {
		return;
	}

	static public String formatcost(double price) {
		return COSTFORMAT.format(price);
	}

	protected List<Option> getfixedoptions() {
		return new ArrayList<Option>();
	}

	public SelectScreen(final String t, final Town town) {
		this(t, true, town);
	}

	public abstract String getCurrency();

	abstract String printInfo();

	abstract boolean select(Option o);

	abstract List<Option> getOptions();
}
