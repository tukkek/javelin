package javelin.view.screen.town;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.Option;

/**
 * Any screen with multiple choices.
 * 
 * @author alex
 */
public abstract class SelectScreen extends InfoScreen {

	private static final DecimalFormat COSTFORMAT =
			new DecimalFormat("####,###,##0");
	/** Default key to proceed ({@value #PROCEED}). */
	public static final char PROCEED = 'q';
	/** List of keys except q. */
	public static final char[] KEYS = new char[] { //
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', //
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'r', 's', 't', 'u', 'v', 'x', 'w', 'y', 'z', //
			'/', '*', '-', '+', '.', ',', '?', '!', '@', '#', '$', '%', '&',
			'(', ')', '_', '=', '[', ']', '{', '}', '<', '>', ';', ':', '\'',
			'"', '\\', '|', };
	/** Current town or <code>null</code>. */
	protected final Town town;
	/** If <code>true</code> will show {@link #title}. */
	public boolean showtitle = true;
	/** Header. */
	protected String title;
	/**
	 * TODO remove on 2.0
	 */
	protected boolean stayopen = true;
	/** If <code>false</code> wills stay open after selection. */
	boolean closeafterselect = false;
	/**
	 * If <code>true</code> will display a message about using {@link #PROCEED}
	 * to quit the screen.
	 */
	protected boolean showquit = true;

	/** Constructor. */
	public SelectScreen(final String name, final Town t) {
		super("");
		town = t;
		title = name + "\n\n";
	}

	@Override
	public void show() {
		text = showtitle ? title : "";
		final List<Option> options = getoptions();
		options.addAll(getfixedoptions());
		for (final Option o : options) {
			roundcost(o);
		}
		sort(options);
		for (int i = 0; i < options.size(); i++) {
			final Option o = options.get(i);
			text += (o.key == null ? KEYS[i] : o.key) + " - " + o.toString()
					+ printpriceinfo(o) + "\n";
		}
		final String extrainfo = printInfo();
		if (!extrainfo.isEmpty()) {
			text += "\n" + extrainfo + "\n";
		}
		if (showquit) {
			text += "\nPress " + PROCEED + " to quit this screen\n";
		}
		IntroScreen.configurescreen(this);
		processinput(options);
		if (stayopen && !Squad.getall(Squad.class).isEmpty()) {
			show();
		} else {
			onexit();
		}
	}

	/**
	 * @param options
	 *            All options to be sorted.
	 */
	protected void sort(final List<Option> options) {
		Collections.sort(options, sort());
	}

	/** Called when closing this screen. */
	public void onexit() {
		// delegate
	}

	/**
	 * @return Textual representation of {@link Option#price}.
	 */
	public String printpriceinfo(Option o) {
		return " " + getCurrency() + formatcost(o.price);
	}

	/**
	 * @return A comparator for the default implementation of
	 *         {@link #sort(List)}.
	 */
	protected Comparator<Option> sort() {
		return new Comparator<Option>() {
			@Override
			public int compare(final Option o1, final Option o2) {
				return new Long(Math.round(o1.price - o2.price)).intValue();
			}
		};
	}

	/**
	 * @param options
	 *            Read player input until a selection is made or the screen is
	 *            closed.
	 */
	public void processinput(final List<Option> options) {
		char feedback = ' ';
		while (feedback != PROCEED) {
			Javelin.app.switchScreen(this);
			feedback = InfoScreen.feedback();
			if (select(feedback, options)) {
				return;
			}
		}
		if (feedback == PROCEED) {
			stayopen = false;
		}
	}

	/**
	 * @param feedback
	 *            Player input.
	 * @return <code>false</code> if no {@link Option} was chosen by the given
	 *         input, otherwise the return value of {@link #select(Option)}.
	 */
	protected boolean select(char feedback, final List<Option> options) {
		Option o = convertselectionkey(feedback, options);
		if (o == null) {
			int selected = convertnumericselection(feedback, KEYS);
			if (selected < 0 || selected >= options.size()) {
				return false;
			}
			o = options.get(selected);
			if (o.key != null) {
				/*
				 * Don't allow options with explicit keys to be select by
				 * numbers.
				 */
				return false;
			}
		}
		return select(o);
	}

	Option convertselectionkey(Character feedback, List<Option> options) {
		for (Option o : options) {
			if (o.key == feedback) {
				return o;
			}
		}
		return null;
	}

	/**
	 * @param options
	 * @return selection index or -1 if not chosen.
	 */
	static public int convertnumericselection(char feedback, char[] keys) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == feedback) {
				return i;
			}
		}
		return -1;
	}

	/** Allows subclasses to round {@link Option#price}s. */
	public void roundcost(final Option o) {
		return;
	}

	/**
	 * @return Textual representation of the givne {@link Option#price}.
	 */
	static public String formatcost(double price) {
		return COSTFORMAT.format(price);
	}

	/**
	 * @return Static options.
	 */
	protected List<Option> getfixedoptions() {
		return new ArrayList<Option>();
	}

	/**
	 * @return How to address currency (ex: $).
	 */
	public abstract String getCurrency();

	/**
	 * @return Footer note.
	 */
	public abstract String printInfo();

	/**
	 * Called after an Option is selected.
	 * 
	 * @param o
	 *            Selection.
	 * @return <code>true</code> to exit the screen, <code>false</code> to
	 *         continue with selection.
	 * @see #stayopen
	 */
	public abstract boolean select(Option o);

	/**
	 * @return Dynamic options.
	 * @see #getfixedoptions()
	 */
	public abstract List<Option> getoptions();

	/**
	 * @param feedback
	 *            Given the user-input key
	 * @return the index of that key in {@link #KEYS}.
	 */
	public static int convertkeytoindex(char feedback) {
		for (int i = 0; i < KEYS.length; i++) {
			char c = KEYS[i];
			if (c == feedback) {
				return i;
			}
		}
		return -1;
	}
}
