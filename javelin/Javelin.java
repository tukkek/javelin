package javelin;

import java.awt.BorderLayout;
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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javelin.controller.db.StateManager;
import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.factor.Organization;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import javelin.model.world.WorldMap;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.town.RecruitScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.InfoScreen;
import tyrant.mikera.tyrant.QuestApp;

/**
 * Utility class for broad-level rules and game-behavior.
 * 
 * @author alex
 */
public class Javelin {
	/**
	 * Add -Ddebug=true to the java VM command line for easier debugging and
	 * logging.
	 */
	public static final boolean DEBUG = System.getProperty("debug") != null;
	public static final boolean DEBUGDISABLECOMBAT =
			javelin.controller.db.Properties.getString("cheat.combat") != null;
	public static final Integer DEBUGSTARTINGXP =
			javelin.controller.db.Properties.getInteger("cheat.xp", null);
	public static final Integer DEBUGSTARTINGGOLD =
			javelin.controller.db.Properties.getInteger("cheat.gold", null);
	public static final Integer DEBUGSTARTINGHAX =
			javelin.controller.db.Properties.getInteger("cheat.haxor", null);
	public static final Integer DEBUGSTARTINGLABOR =
			javelin.controller.db.Properties.getInteger("cheat.labor", null);
	public static final Integer DEBUGMINIMUMFOES = null;
	public static final Integer DEBUGWEATHER = null;
	public static final Map DEBUGMAPTYPE = null;
	public static final boolean DEBUG_SPAWNINCURSION = false;
	public static final Float DEBUGSTARTINGCR = null;
	public static final Realm DEBUGSTARTINGKEY = null;
	public static final String DEBUGALLOWMONSTER = null;
	public static final String DEBUGPERIOD = null;

	public static final String PERIOD_MORNING = "Morning";
	public static final String PERIOD_NOON = "Noon";
	public static final String PERIOD_EVENING = "Evening";
	public static final String PERIOD_NIGHT = "Night";

	private static final String TITLE = "Javelin";

	private static final Preferences RECORD =
			Preferences.userNodeForPackage(Javelin.class);
	public static TreeMap<String, String> DESCRIPTIONS =
			new TreeMap<String, String>();
	public static TreeMap<Float, List<Monster>> MONSTERSBYCR =
			new TreeMap<Float, List<Monster>>();
	public static List<Monster> ALLMONSTERS = new ArrayList<Monster>();
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
		Thread.currentThread().setName("Javelin");
		final Frame f = new Frame(TITLE);
		f.setBackground(java.awt.Color.black);
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
		if (next != null) {
			Game.instance().hero = next.visual;
		}
		return next;
	}

	public static Squad nexttoact() {
		Squad next = null;
		for (final Squad s : Squad.squads) {
			if (next == null || s.hourselapsed < next.hourselapsed) {
				next = s;
			}
		}
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
		Game.message(
				"You have lost all your monsters! Game over T_T\n\n" + record(),
				null, Delay.NONE);
		while (InfoScreen.feedback() != '\n') {
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
		RecruitScreen.namingscreen(c.source);
		Squad.active.members.add(c);
		return c;
	}

	public static int difficulty() {
		switch (Javelin.terrain()) {
		case WorldMap.EASY:
			return -1;
		case WorldMap.MEDIUM:
			return 0;
		case WorldMap.HARD:
			return +1;
		case WorldMap.VERYHARD:
			return +2;
		default:
			throw new RuntimeException("Unknown tile difficulty!");
		}
	}

	public static void settexture(Image file) {
		QuestApp.paneltexture = file;
	}

	public static Combatant getCombatant(int id) {
		for (Combatant c : BattleMap.combatants) {
			if (c.id == id) {
				return c;
			}
		}
		return null;
	}

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
	 * @return Current terrain difficulty. For example: {@link WorldMap#EASY}.
	 */
	static public int terrain() {
		Thing h = Game.hero();
		return Javelin.terrain(h.x, h.y);
	}

	/**
	 * @param x
	 *            {@link WorldMap} coordinate.
	 * @param y
	 *            {@link WorldMap} coordinate.
	 * @return Terrain difficulty. For example: {@link WorldMap#EASY}.
	 */
	public static int terrain(int x, int y) {
		return JavelinApp.overviewmap.getTile(x, y);
	}

	/**
	 * @param difficulty
	 *            Terrain difficulty. For example: {@link WorldMap#EASY}.
	 * @return Name of a d20 terrain.
	 */
	public static String terrain(int difficulty) {
		return RPG.pick(terrains(difficulty));
	}

	/**
	 * @param difficulty
	 *            Terrain difficulty. For example: {@link WorldMap#EASY}.
	 * @return All the names of d20 terrains this difficulty encompasses.
	 */
	public static String[] terrains(int difficulty) {
		if (difficulty == WorldMap.EASY) {
			return new String[] { "plains", "plains", "hill" };
		} else if (difficulty == WorldMap.MEDIUM) {
			return new String[] { "forest", "forest", "hill" };
		} else if (difficulty == WorldMap.HARD) {
			return new String[] { "mountains", "desert" };
		} else if (difficulty == WorldMap.VERYHARD) {
			return new String[] { "marsh" };
		} else {
			throw new RuntimeException("Unknown tile difficulty!");
		}
	}

}
