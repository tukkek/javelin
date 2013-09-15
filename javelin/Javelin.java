package javelin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javelin.controller.db.StateManager;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.Organization;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.world.WorldScreen;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Tile;

public class Javelin {
	public static final boolean DEBUGDISABLECOMBAT = javelin.controller.db.Preferences
			.getString("cheat.combat") != null;
	public static final Integer DEBUGSTARTINGXP = javelin.controller.db.Preferences
			.getInteger("cheat.xp", null);
	public static final Integer DEBUGSTARTINGGOLD = javelin.controller.db.Preferences
			.getInteger("cheat.gold", null);
	public static final Integer DEBUGMINIMUMFOES = null;
	public static final Integer DEBUGWEATHER = null;
	public static final String DEBUGMAPTYPE = null;
	public static final String DEBUGPERIOD = null;

	public static final String PERIOD_MORNING = "Morning";
	public static final String PERIOD_NOON = "Noon";
	public static final String PERIOD_EVENING = "Evening";
	public static final String PERIOD_NIGHT = "Night";

	private static final String TITLE = "Javelin";

	/**
	 * Add -Ddebug=true to the java VM command line for easier debugging and
	 * logging. TODO
	 */
	public static final boolean DEBUG = System.getProperty("debug") != null;
	private static final Preferences RECORD = Preferences
			.userNodeForPackage(Javelin.class);
	public static TreeMap<String, String> DESCRIPTIONS = new TreeMap<String, String>();
	public static TreeMap<Float, List<Monster>> MONSTERS = new TreeMap<Float, List<Monster>>();
	public static List<Monster> ALLMONSTERS = new ArrayList<Monster>();

	public static String mapType;
	public static JavelinApp app;
	public static Combatant captured = null;

	static {
		try {
			final DefaultHandler defaultHandler = new MonsterReader();
			final XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(defaultHandler);
			reader.setErrorHandler(defaultHandler);
			reader.parse(new InputSource(new FileReader("monsters.xml")));
			Organization.process();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final SAXException e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		final Frame f = new Frame(TITLE);
		f.setBackground(Color.black);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
		f.setLayout(new BorderLayout());
		app = new JavelinApp();
		QuestApp.isapplet = false;
		if (args.length > 0) {
			QuestApp.gameFileFromCommandLine = args[0];
		}
		// do we have a script to execute?
		// activate debug mode if we do....
		final java.io.File script = new java.io.File("mikeradebug");

		// old method to activate debug cheat
		Game.setDebug(script.exists());

		app.setVisible(false);
		f.add(app);

		f.setSize(app.getPreferredSize().width, app.getPreferredSize().height);

		f.addKeyListener(app.keyadapter);

		app.setVisible(true);
		f.setVisible(true);

		app.init();
	}

	public static Combatant getCombatant(final Thing t) {
		for (final Combatant c : BattleMap.combatants) {
			if (c.visual == t) {
				return c;
			}
		}

		return null;
	}

	public static Combatant getLowestAp() {
		return BattleScreen.active.map.getState().next;
		//
	}

	public static String getDayPeriod() {
		final long hourofday = getHour();
		if (hourofday < 6) {
			return PERIOD_NIGHT;
		}
		if (hourofday < 12) {
			return PERIOD_MORNING;
		}
		if (hourofday < 18) {
			return PERIOD_NOON;
		}
		return PERIOD_EVENING;
	}

	public static long getHour() {
		return Squad.active.hourselapsed % 24;
	}

	/**
	 * This is also the only funcntion that should write to {@link Squad#active}
	 * so it should be called with extreme caution to avoid changing it in the
	 * middle of an action.
	 * 
	 * @return Next squad to act.
	 */
	public static Squad act() {
		Squad next = null;
		for (final Squad s : Squad.squads) {
			if (next == null || s.hourselapsed < next.hourselapsed) {
				next = s;
			}
		}
		Squad.active = next;
		if (WorldScreen.lastday == -1) {
			WorldScreen.lastday = Math.ceil(Squad.active.hourselapsed / 24.0);
		}
		Game.instance().hero = next.visual;
		BattleMap.blueTeam = next.members;
		return next;
	}

	public static String sayWelcome() {
		final String period = getDayPeriod();
		String flavor;
		if (period == PERIOD_MORNING) {
			flavor = "What dangers lie ahead..?";
		} else if (period == PERIOD_NOON) {
			flavor = "Onwards to victory!";
		} else if (period == PERIOD_EVENING) {
			flavor = "Cheers!";
		} else if (period == PERIOD_NIGHT) {
			flavor = "What a horrible night to suffer an invasion...";
		} else {
			throw new RuntimeException("No welcome message");
		}
		return "Welcome! " + flavor
				+ "\n\n(press h at the overworld or battle screens for help)";
	}

	public static void lose() {
		StateManager.clear();
		BattleScreen.active.messagepanel.clear();
		Game.message("You have lost all your monsters! Game over T_T\n\n"
				+ record(), null, Delay.NONE);
		while (IntroScreen.feedback() != '\n') {
			continue;
		}
		System.exit(0);
	}

	public static String record() {
		final long stored = gethighscore();
		final long current = WorldScreen.current.currentday();
		String message = "Previous record: " + stored;
		if (stored < current) {
			message += "\nNew record: " + current + "!";
			sethighscore(current);
		} else {
			message += "\nCurrent game: " + current;
		}
		return message;
	}

	public static void sethighscore(final long score) {
		RECORD.putLong("record", score);
	}

	public static long gethighscore() {
		return RECORD.getLong("record", 0);
	}

	public static Combatant recruit(Monster pick) {
		Combatant c = new Combatant(null, pick.clone(), true);
		Squad.active.members.add(c);
		return c;
	}

	public static boolean namerecruits() {
		boolean renamed = false;
		for (final Combatant m : BattleMap.blueTeam) {
			if (m.source.customName == null) {
				JavelinApp.namingscreen(m.source);
				renamed = true;
			}
		}
		return renamed;
	}

	public static int difficulty(final int tile) {
		switch (tile) {
		case Tile.PLAINS:
			return -1;
		case Tile.FORESTS:
			return 0;
		case Tile.HILLS:
			return +1;
		case Tile.GUNK:
			return +2;
		default:
			throw new RuntimeException("Unknown tile difficulty!");
		}
	}

	public static void settexture(Image file) {
		QuestApp.paneltexture = file;
	}
}
