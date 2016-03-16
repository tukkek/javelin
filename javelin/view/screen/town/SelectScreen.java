package javelin.view.screen.town;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.world.Squad;
import javelin.model.world.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.option.Option;
import tyrant.mikera.tyrant.InfoScreen;

/**
 * Any screen with multiple choices.
 * 
 * @author alex
 */
public abstract class SelectScreen extends InfoScreen {

	private static final DecimalFormat COSTFORMAT =
			new DecimalFormat("####,###,##0");
	static final char PROCEED = 'q';
	public static final char[] SELECTIONKEYS = new char[] { '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
			'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u',
			'v', 'x', 'w', 'y', 'z', };
	protected final Town town;
	public boolean showtitle = true;
	private final String title;
	protected boolean sortoptions = true;
	/**
	 * TODO probably unecessary at this point
	 */
	boolean stayopen = true;

	/**
	 * TODO Town could be WorldPlace
	 */
	public SelectScreen(final String name, final Town t) {
		super(Javelin.app, "");
		town = t;
		title = name + "\n\n";
	}

	@Override
	public void show() {
		text = showtitle ? title : "";
		final List<Option> options = getOptions();
		options.addAll(getfixedoptions());
		for (final Option o : options) {
			roundcost(o);
		}
		if (sortoptions) {
			Collections.sort(options, sort());
		}
		for (int i = 0; i < options.size(); i++) {
			final Option o = options.get(i);
			text += "[" + SELECTIONKEYS[i] + "] " + o.name + printpriceinfo(o)
					+ "\n";
		}
		final String extrainfo = printInfo();
		if (!extrainfo.isEmpty()) {
			text += "\n" + extrainfo + "\n";
		}
		text += "\nPress " + PROCEED + " to quit this screen\n";
		IntroScreen.configurescreen(this);
		processinput(options);
		if (stayopen && !Squad.squads.isEmpty()) {
			show();
		} else {
			onexit();
		}
	}

	public void onexit() {
		// if (!(this instanceof TownScreen)) {
		// new TownScreen(town).show();
		// }
	}

	public String printpriceinfo(Option o) {
		return " " + getCurrency() + formatcost(o.price);
	}

	protected Comparator<Option> sort() {
		return new Comparator<Option>() {
			@Override
			public int compare(final Option o1, final Option o2) {
				return new Long(Math.round(o1.price - o2.price)).intValue();
			}
		};
	}

	public void processinput(final List<Option> options) {
		char feedback = ' ';
		while (feedback != PROCEED) {
			Javelin.app.switchScreen(this);
			feedback = InfoScreen.feedback();
			int selected = convertselectionkey(feedback);
			if (0 <= selected && selected < options.size()
					&& select(options.get(selected))) {
				return;
			}
		}
		if (feedback == PROCEED) {
			stayopen = false;
		}
	}

	/**
	 * @return selection index or -1 if not chosen.
	 */
	static public int convertselectionkey(char feedback) {
		for (int i = 0; i < SELECTIONKEYS.length; i++) {
			if (SELECTIONKEYS[i] == feedback) {
				return i;
			}
		}
		return -1;
	}

	public void roundcost(final Option o) {
		return;
	}

	static public String formatcost(double price) {
		return COSTFORMAT.format(price);
	}

	protected List<Option> getfixedoptions() {
		return new ArrayList<Option>();
	}

	public abstract String getCurrency();

	public abstract String printInfo();

	/**
	 * Called after an Option is selected.
	 * 
	 * @param o
	 *            Selection.
	 * @return <code>true</code> to exit the screen, <code>false</code> to
	 *         continue with selection.
	 */
	public abstract boolean select(Option o);

	public abstract List<Option> getOptions();
}
