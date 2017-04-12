package javelin;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javelin.controller.ai.BattleAi;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.factor.SpellsFactor;
import javelin.controller.db.StateManager;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.hazard.PartyHazard;
import javelin.controller.upgrade.Spell;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.item.Item;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.NamingScreen;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Utility class for broad-level rules and game-behavior. Add the VM argument
 * -Ddebug=true to the java command line for easier debugging and logging.
 *
 * @see #DEBUG
 * @author alex
 */
public class Javelin {
	/**
	 * Add -Ddebug=true to the java VM command line for easier debugging and
	 * logging.
	 */
	public static final boolean DEBUG = System.getProperty("debug") != null;

	/** TODO turn into {@link Enum} */
	public static final String PERIODMORNING = "Morning";
	/** TODO turn into {@link Enum} */
	public static final String PERIODNOON = "Noon";
	/** TODO turn into {@link Enum} */
	public static final String PERIODEVENING = "Evening";
	/** TODO turn into {@link Enum} */
	public static final String PERIODNIGHT = "Night";

	private static final String TITLE = "Javelin";

	private static final Preferences RECORD = Preferences
			.userNodeForPackage(Javelin.class);

	/**
	 * Monster descriptions, separate from {@link Monster} data to avoid
	 * duplication in memory when using {@link Monster#clone()}.
	 *
	 * @see Combatant#clonedeeply()
	 */
	public static TreeMap<String, String> DESCRIPTIONS = new TreeMap<String, String>();
	/** All loaded monster mapped by challenge rating. */
	public static TreeMap<Float, List<Monster>> MONSTERSBYCR = new TreeMap<Float, List<Monster>>();
	/** All loaded XML {@link Monster}s. See {@link MonsterReader}. */
	public static List<Monster> ALLMONSTERS = new ArrayList<Monster>();
	/** Singleton. */
	public static JavelinApp app;
	/** Monster captured on a {@link LairFight}. */
	public static Combatant captured = null;

	static {
		try {
			checkjava();
			UpgradeHandler.singleton.gather();
			Spell.init();
			final DefaultHandler defaultHandler = new MonsterReader();
			final XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(defaultHandler);
			reader.setErrorHandler(defaultHandler);
			FileReader filereader = new FileReader("monsters.xml");
			reader.parse(new InputSource(filereader));
			filereader.close();
			Organization.process();
			SpellsFactor.init();
			Spell.init();
			Artifact.init();
			Item.init();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final SAXException e) {
			e.printStackTrace();
		}
	}

	/**
	 * First method to be called.
	 *
	 * @param args
	 *            See {@link #DEBUG}.
	 */
	public static void main(final String[] args) {
		Thread.currentThread().setName("Javelin");
		final JFrame f = new JFrame(TITLE);
		f.setBackground(java.awt.Color.black);
		f.addWindowListener(StateManager.SAVEONCLOSE);
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setFocusTraversalKeysEnabled(false);
		app = new JavelinApp();
		app.frame = f;
		app.setVisible(false);
		f.add(app);
		f.setSize(app.getPreferredSize().width, app.getPreferredSize().height);
		f.addKeyListener(app.keyadapter);
		app.setVisible(true);
		f.setVisible(true);
		app.init();
	}

	private static void checkjava() {
		String[] version = System.getProperty("java.version").split("\\.");
		if (Integer.parseInt(version[0]) != 1) {
			/* java 2.x? Don't even try to guess what to do... */
			return;
		}
		int major = Integer.parseInt(version[1]);
		if (major < 8) {
			String error;
			error = "Javelin needs Java 8 or newer to run properly.";
			error += "\nYou currently have Java " + major + " installed.";
			error += "\nPlease update Java in order play Javelin and to install the newest security updates.";
			error += "\n\nThe following webpage has further information on updating Java on all major operating systems:";
			error += "\nwww.techhelpkb.com/how-to-update-java-on-your-computer";
			JOptionPane.showMessageDialog(null, error);
			System.exit(1);
		}
	}

	/**
	 * TODO move to a new class with proper enum?
	 *
	 * @return {@link #PERIODEVENING}, {@link #PERIODMORNING},
	 *         {@link #PERIODNIGHT} or {@value #PERIODNOON}.
	 */
	public static String getDayPeriod() {
		if (Javelin.app.fight != null) {
			return Javelin.app.fight.period;
		}
		final long hourofday = getHour();
		if (hourofday < 6) {
			return PERIODNIGHT;
		}
		if (hourofday < 12) {
			return PERIODMORNING;
		}
		if (hourofday < 18) {
			return PERIODNOON;
		}
		return PERIODEVENING;
	}

	/**
	 * @return Hour of the day, from 0 to 23.
	 */
	public static long getHour() {
		if (Squad.active == null) {
			/* all training */
			return 0;
		}
		return Squad.active.hourselapsed % 24;
	}

	/**
	 * This is also the only function that should write to {@link Squad#active}
	 * so it should be called with extreme caution to avoid changing it in the
	 * middle of an action.
	 *
	 * @return Next squad to act.
	 */
	public static Squad act() {
		Squad next = nexttoact();
		Squad.active = next;
		if (WorldScreen.lastday == -1) {
			WorldScreen.lastday = Math.ceil(Squad.active.hourselapsed / 24.0);
		}
		return next;
	}

	/**
	 * @return Next squad to act.
	 * @see Squad#hourselapsed
	 */
	public static Squad nexttoact() {
		Squad next = null;
		for (final Actor a : World.getall(Squad.class)) {
			Squad s = (Squad) a;
			if (next == null || s.hourselapsed < next.hourselapsed) {
				next = s;
			}
		}
		return next;
	}

	/**
	 * Pure fluff/flavor.
	 *
	 * @return Welcomes the playet to the game based on the current time of the
	 *         day.
	 */
	public static String sayWelcome() {
		final String period = getDayPeriod();
		String flavor;
		if (period == PERIODMORNING) {
			flavor = "What dangers lie ahead..?";
		} else if (period == PERIODNOON) {
			flavor = "Onwards to victory!";
		} else if (period == PERIODEVENING) {
			flavor = "Cheers!";
		} else if (period == PERIODNIGHT) {
			flavor = "What a horrible night to suffer an invasion...";
		} else {
			throw new RuntimeException("No welcome message");
		}
		return "Welcome! " + flavor
				+ "\n\n(press h at the overworld or battle screens for help)";
	}

	/**
	 * Once the player has no more {@link Squad}s and {@link Combatant}s under
	 * his control call this to stop the current game, invalidate the save file,
	 * record the highscore and exit the application.
	 */
	public static void lose() {
		if (Academy.train()) {
			return;
		}
		Javelin.app.switchScreen(BattleScreen.active);
		StateManager.clear();
		BattleScreen.active.messagepanel.clear();
		Game.message(
				"You have lost all your monsters! Game over T_T\n\n" + record(),
				Delay.NONE);
		while (InfoScreen.feedback() != '\n') {
			continue;
		}
		System.exit(0);
	}

	/**
	 * Sets the highscore and...
	 *
	 * @return a message with previous and current score.
	 */
	public static String record() {
		final long stored = gethighscore();
		final long current = WorldScreen.currentday();
		String message = "Previous record: " + stored;
		if (stored < current) {
			message += "\nNew record: " + current + "!";
			sethighscore(current);
		} else {
			message += "\nCurrent game: " + current;
		}
		return message;
	}

	/**
	 * @param score
	 *            Updates the highscore record with this value.
	 */
	public static void sethighscore(final long score) {
		RECORD.putLong("record", score);
	}

	/**
	 * @return The current highscore value.
	 */
	public static long gethighscore() {
		return RECORD.getLong("record", 0);
	}

	/**
	 * @param pick
	 *            Source statistics to make an unit from.
	 * @return An actual unit with said statistics.
	 * @see Combatant#clone()
	 * @see RecruitScreen#namingscreen(String)
	 */
	public static Combatant recruit(Monster pick) {
		Combatant c = new Combatant(pick.clone(), true);
		c.source.customName = NamingScreen.getname(c.toString());
		Squad.active.members.add(c);
		/*
		 * night-only is largely cosmetic so just don't appear for player units
		 */
		c.source.nightonly = false;
		return c;
	}

	/**
	 * @param file
	 *            Uses the given image as background.
	 */
	public static void settexture(Image file) {
		QuestApp.paneltexture = file;
	}

	/** TODO remove? */
	public static Combatant getCombatant(int id) {
		for (Combatant c : Fight.state.getcombatants()) {
			if (c.id == id) {
				return c;
			}
		}
		return null;
	}

	/**
	 * @param rolltohit
	 *            The number that needs to be rolled on a d20 for this action to
	 *            succeed.
	 * @return A textual representation of how easy or hard this action is to
	 *         achieve.
	 */
	static public String translatetochance(int rolltohit) {
		if (rolltohit <= 4) {
			return "effortless";
		}
		if (rolltohit <= 8) {
			return "easy";
		}
		if (rolltohit <= 12) {
			return "fair";
		}
		if (rolltohit <= 16) {
			return "hard";
		}
		return "unlikely";
	}

	/**
	 * 2 chances of an easy encounter, 10 chances of a moderate encounter, 4
	 * chances of a difficult encounter and 1 chance of an overwhelming
	 * encounter
	 */
	public static Integer randomdifficulty() {
		final LinkedList<Integer> elchoices = new LinkedList<Integer>();
		for (int j = 0; j < 2; j++) {
			elchoices.add(RPG.r(-5, -8));
		}
		for (int j = 0; j < 10; j++) {
			elchoices.add(-4);
		}
		for (int j = -3; j <= 0; j++) {
			elchoices.add(j);
		}
		elchoices.add(1);
		return RPG.pick(elchoices);
	}

	/**
	 * Utility function for user-input selection.
	 *
	 * @param output
	 *            Text to show the user.
	 * @param names
	 *            Will show each's {@link Object#toString()} as an option.
	 * @param fullscreen
	 *            <code>true</code> to open in a new screen. Otherwise uses the
	 *            message panel.
	 * @param forceselection
	 *            If <code>false</code> will allow the user to abort the
	 *            operation.
	 * @return The index of the selected element or -1 if aborted.
	 */
	static public int choose(String output, List<?> names, boolean fullscreen,
			boolean forceselection) {
		if (!forceselection) {
			output += " (q to quit)";
		}
		output += " \n\n";
		boolean multicolumn = names.size() > 20;
		ArrayList<Object> options = new ArrayList<Object>();
		for (int i = 0; i < names.size(); i++) {
			boolean leftcolumn = i % 2 == 0;
			String name = names.get(i).toString();
			options.add(name);
			String item = "[" + SelectScreen.KEYS[i] + "] " + name;
			if (multicolumn && leftcolumn) {
				while (item.length() < 40) {
					item += " ";
				}
			}
			output += item;
			if (!multicolumn || !leftcolumn) {
				output += "\n";
			}
		}
		if (fullscreen) {
			app.switchScreen(new InfoScreen(output));
		} else {
			app.switchScreen(BattleScreen.active);
			Game.messagepanel.clear();
			Game.message(output, Delay.NONE);
		}
		while (true) {
			try {
				Character feedback = InfoScreen.feedback();
				if (!forceselection && feedback == 'q') {
					return -1;
				}
				int selected = SelectScreen.convertkeytoindex(feedback);
				if (0 <= selected && selected < names.size()) {
					return selected;
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * Main output function for {@link WorldScreen}. Waits for user input for
	 * confirmation.
	 *
	 * @param text
	 *            Prints this message in the status panel.
	 * @param requireenter
	 *            If <code>true</code> will wait for the player to press ENTER,
	 *            otherwise any key will do.
	 * @return the key pressed by the user as confirmation for seeing the
	 *         message.
	 * @see Game#message(String, Combatant, Delay)
	 */
	public static KeyEvent message(String text, boolean requireenter) {
		Game.messagepanel.clear();
		Game.message(text + "\nPress " + (requireenter ? "ENTER" : "any key")
				+ " to continue...", Delay.NONE);
		KeyEvent input = Game.getInput();
		while (requireenter && input.getKeyChar() != '\n') {
			input = Game.getInput();
		}
		return input;
	}

	/**
	 * Prompts a message in the {@link WorldScreen}.
	 *
	 * @param prompt
	 *            Text to show.
	 * @return Any {@link InfoScreen#feedback()}.
	 */
	static public Character prompt(final String prompt) {
		Game.messagepanel.clear();
		Game.message(prompt, Delay.NONE);
		return InfoScreen.feedback();
	}

	/**
	 * @param name
	 *            Monster type. Example: orc, kobold, young white dragon... Case
	 *            insensitive.
	 * @return A clone.
	 * @see Monster#clone()
	 */
	public static Monster getmonster(String name) {
		Monster monster = null;
		for (Monster m : ALLMONSTERS) {
			if (m.name.equalsIgnoreCase(name)) {
				monster = m.clone();
				break;
			}
		}
		if (monster == null) {
			return null;
		}
		ChallengeRatingCalculator.calculatecr(monster);
		return monster;
	}

	/**
	 * Most of the time take-10 rules are used to prevent players from boring
	 * themselves. For example: trying repeatedly to use
	 * {@link Skills#perception} to see what's inside a {@link Fortification}.
	 * That's no fun. On other scenarios though it's more interesting to make
	 * these rolls random - for example, if always using take-10 rolls
	 * {@link PartyHazard}s would become obsolete when a character with enough
	 * {@link Skills#survival} is in the party.
	 *
	 * Note also that randomization can't be used without proper treatment
	 * inside {@link BattleAi} computing, which is why take-10 rolls makes
	 * things easier while in-battle.
	 *
	 * @param take10
	 *            A take-10 result.
	 * @return result of the same roll, but rolling a d20 instead.
	 */
	public static int roll(int take10) {
		return take10 - 10 + RPG.r(1, 20);
	}

	public static String describedifficulty(int dc) {
		if (dc <= 0) {
			return "very easy";
		}
		if (dc <= 5) {
			return "easy";
		}
		if (dc <= 10) {
			return "average";
		}
		if (dc <= 15) {
			return "tough";
		}
		if (dc <= 20) {
			return "challenging";
		}
		if (dc <= 25) {
			return "formidable";
		}
		if (dc <= 30) {
			return "heroic";
		}
		return "nearly impossible";
	}

	/**
	 * @param message
	 *            Shows this in a fullscreen, requires enter to leave.
	 */
	public static void show(String message) {
		InfoScreen s = new InfoScreen("");
		s.print(message);
		while (s.getInput() != '\n') {
			// wait for enter
		}
	}
}
