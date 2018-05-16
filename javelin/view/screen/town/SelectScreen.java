package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.comparator.OptionsByPriority;
import javelin.model.unit.Squad;
import javelin.model.world.World;
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
	/** Default key to proceed ({@value #PROCEED}). */
	public static final char PROCEED = 'q';
	/**
	 * List of keys except q.
	 *
	 * TODO would probably work better as a list (with indexof(), etc). Can use
	 * {@link Arrays#asList(Object...)}.
	 */
	static final char[] KEYS = "1234567890abcdefghijklmnoprstuvxwyz/*-+.!@#$%&()_=[]{}<>;:\"\\|"
			.toCharArray();
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
	public boolean forceclose = false;
	String originaltext;

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
		if (options.isEmpty()) {
			stayopen = false;
		} else {
			for (final Option o : options) {
				roundcost(o);
			}
			options.sort(sort());
			printoptions(options);
			final String extrainfo = printinfo();
			if (!extrainfo.isEmpty()) {
				text += "\n" + extrainfo + "\n";
			}
			if (showquit) {
				text += "\nPress " + PROCEED + " to quit this screen\n";
			}
			IntroScreen.configurescreen(this);
			processinput(options);
		}
		if (stayopen && !World.getall(Squad.class).isEmpty()) {
			show();
		} else {
			onexit();
		}
	}

	protected Comparator<Option> sort() {
		return OptionsByPriority.INSTANCE;
	}

	public void printoptions(final List<Option> options) {
		for (int i = 0; i < options.size(); i++) {
			final Option o = options.get(i);
			text += (o.key == null ? KEYS[i] : o.key) + " - " + o.toString()
					+ printpriceinfo(o) + "\n";
		}
	}

	/** Called when closing this screen. */
	public void onexit() {
		// delegate
	}

	/**
	 * @return Textual representation of {@link Option#price}.
	 */
	public String printpriceinfo(Option o) {
		return " " + getCurrency() + Javelin.format(o.price);
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
				originaltext = null;
				return;
			}
		}
		if (feedback == PROCEED) {
			proceed();
		}
	}

	protected void proceed() {
		stayopen = false;
	}

	/**
	 * @param feedback
	 *            Player input.
	 * @return <code>false</code> if no {@link Option} was chosen by the given
	 *         input, otherwise the return value of {@link #select(Option)}.
	 * @return <code>true</code> to exit this screen.
	 */
	protected boolean select(char feedback, final List<Option> options) {
		if (originaltext == null) {
			originaltext = text;
		}
		print(originaltext);
		Option o = convertselectionkey(feedback, options);
		if (o == null) {
			int selected = convertnumericselection(feedback);
			if (selected < 0 || selected >= options.size()) {
				return false;
			}
			o = options.get(selected);
			if (o.key != null) {
				/*
				 * Don't allow options with explicit keys to be selected by
				 * numbers.
				 */
				return false;
			}
		}
		return select(o);
	}

	static Option convertselectionkey(Character c, List<Option> options) {
		for (Option o : options) {
			if (o.key == c) {
				return o;
			}
		}
		return null;
	}

	/**
	 * @return selection index or -1 if not chosen.
	 */
	static public int convertnumericselection(char feedback) {
		for (int i = 0; i < KEYS.length; i++) {
			if (KEYS[i] == feedback) {
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
	public abstract String printinfo();

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
	 * @param c
	 *            Given the user-input key
	 * @return the index of that key in {@link #KEYS}.
	 */
	public static int convertkeytoindex(char c) {
		if (c == '\t') {
			return 0;
		}
		for (int i = 0; i < KEYS.length; i++) {
			char key = KEYS[i];
			if (key == c) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return The corresponding key from {@link #KEYS}. If the index is higher
	 *         than actionable keys will return ? instead.
	 */
	public static char getkey(int i) {
		return i < KEYS.length ? KEYS[i] : '?';
	}
}
