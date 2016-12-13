package javelin.controller.db;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.Properties;

import javelin.controller.Weather;
import javelin.controller.action.ActionDescription;
import javelin.controller.ai.BattleAi;
import javelin.controller.ai.ThreadManager;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.old.Game.Delay;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.unique.Haxor;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.KeysScreen;
import javelin.view.frame.keys.BattleKeysScreen;
import javelin.view.frame.keys.PreferencesScreen;
import javelin.view.frame.keys.WorldKeysScreen;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.tyrant.TextZone;

/**
 * Used to read the file "preferences.properties". See the file
 * "preferences.properties" for more details on standard options and this link
 * for development options:
 * 
 * https://github.com/tukkek/javelin/wiki/Development-options
 *
 * @see PreferencesScreen
 * @author alex
 */
public class Preferences {
	/** Configuration file key for {@link #MONITORPERFORMANCE}. */
	public static final String KEYCHECKPERFORMANCE = "ai.checkperformance";
	/** Configuration file key for {@link #MAXTHREADS}. */
	public static final String KEYMAXTHREADS = "ai.maxthreads";
	static final String FILE = "preferences.properties";
	/** Key for {@link #TILESIZEBATTLE}. */
	public static final String KEYTILEBATTLE = "ui.battletile";
	/** Key for {@link #TILESIZEWORLD}. */
	public static final String KEYTILEWORLD = "ui.worldtile";
	/** Key for {@link #TILESIZEDUNGEON}. */
	public static final String KEYTILEDUNGEON = "ui.dungeontile";
	static Properties PROPERTIES = new Properties();

	/** If <code>true</code> will use {@link AiCache}. */
	public static boolean AICACHEENABLED;
	/**
	 * In some system will prevent the CPU temperature from going above this
	 * value.
	 */
	public static Integer MAXTEMPERATURE;
	/** Max {@link BattleAi} thinking time. */
	public static int MAXMILISECONDSTHINKING;
	/** Thread limit for {@link ThreadManager}. */
	public static int MAXTHREADS;
	/** If <code>true</code> {@link ThreadManager} will be self-monitoring. */
	public static boolean MONITORPERFORMANCE;
	/** Time to wait for {@link Delay#WAIT}. */
	public static int MESSAGEWAIT;
	/** Font color. */
	public static String TEXTCOLOR;
	/**
	 * If <code>true</code> backups the game on initialization.
	 * 
	 * @see StateManager
	 */
	public static boolean BACKUP;
	/** How often to save the game. */
	public static int SAVEINTERVAL;
	/** Tile size for {@link BattleScreen}. */
	public static int TILESIZEBATTLE;
	/** Tile size for {@link WorldScreen}. */
	public static int TILESIZEWORLD;
	/** Tile size for {@link DungeonScreen}. */
	public static int TILESIZEDUNGEON;

	/** Debug option. */
	public static boolean DEBUGDISABLECOMBAT;
	/** Debug option. */
	public static boolean DEBUGESHOWMAP;
	/** Debug option. */
	public static Integer DEBUGSXP;
	/** Debug option. */
	public static Integer DEBUGSGOLD;
	/** Debug option. */
	public static Integer DEBUGRUBIES;
	/** Debug option. */
	public static Integer DEBUGLABOR;
	/** Debug option. */
	public static Integer DEBUGCOINS;
	// /** Debug option. */
	// public static boolean DEBUGCLEARGARRISON;
	/** Debug option. */
	public static String DEBUGFOE;
	/** Debug option. */
	public static String DEBUGPERIOD;
	/** Debug option. */
	public static String DEBUGMAPTYPE;
	/** Debug option. */
	public static Integer DEBUGMINIMUMFOES;
	/** Debug option. */
	public static String DEBUGWEATHER;
	/** Debug option. */
	public static String DEBUGSEASON;
	/** Debug option. */
	public static boolean DEBUGUNLOCKTEMPLES;

	/**
	 * TODO make this take a {@link String} from the properties file.
	 */
	public static Item DEBUGSTARTINGITEM = null;

	static {
		load();
	}

	synchronized static void load() {
		try {
			PROPERTIES.clear();
			PROPERTIES.load(new FileReader(FILE));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String getString(String key) {
		try {
			return PROPERTIES.getProperty(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	static Integer getInteger(String key, Integer fallback) {
		String value = getString(key);
		/* Don't inline. */
		if (value == null) {
			return fallback;
		}
		return Integer.parseInt(value);
	}

	/**
	 * @return the entire text of the properties file.
	 */
	synchronized public static String getfile() {
		try {
			String s = "";
			BufferedReader reader = new BufferedReader(new FileReader(FILE));
			String line = reader.readLine();
			while (line != null) {
				s += line + "\n";
				line = reader.readLine();
			}
			reader.close();
			return s;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * TODO would be better to have an option #setpreference(String key,String
	 * value) that would read the file, replace a line with the given key and
	 * save it in the proper order instead of forever appending to the file. It
	 * could be used in a few places this is being used instead.
	 * 
	 * @param content
	 *            Overwrite the properties file with this content and reloads
	 *            all options.
	 */
	synchronized public static void savefile(String content) {
		try {
			write(content, FILE);
			load();
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param content
	 *            Write content...
	 * @param f
	 *            to this file.
	 * @throws IOException
	 */
	public static void write(String content, String f) throws IOException {
		FileWriter writer = new FileWriter(f);
		writer.write(content);
		writer.close();
	}

	/**
	 * Initializes or reloads all preferences.
	 */
	public static void init() {
		AICACHEENABLED = getString("ai.cache").equals("true");
		MAXTEMPERATURE = getInteger("ai.maxtemperature", 0);
		MAXMILISECONDSTHINKING = Math
				.round(1000 * getFloat("ai.maxsecondsthinking"));
		int cpus = Runtime.getRuntime().availableProcessors();
		MAXTHREADS = Preferences.getInteger(KEYMAXTHREADS, cpus);
		if (MAXTHREADS > cpus) {
			MAXTHREADS = cpus;
		}
		MONITORPERFORMANCE = Preferences.getString(KEYCHECKPERFORMANCE)
				.equals("true");
		if (MONITORPERFORMANCE) {
			if (ThreadManager.performance == Integer.MIN_VALUE) {
				ThreadManager.performance = 0;
			}
		} else {
			ThreadManager.performance = Integer.MIN_VALUE;
		}

		BACKUP = getString("fs.backup").equals("true");
		SAVEINTERVAL = getInteger("fs.saveinterval", 9);

		TILESIZEWORLD = getInteger(KEYTILEWORLD, 32);
		TILESIZEBATTLE = getInteger(KEYTILEBATTLE, 32);
		TILESIZEDUNGEON = getInteger(KEYTILEDUNGEON, 32);
		MESSAGEWAIT = Math.round(1000 * getFloat("ui.messagedelay"));
		TEXTCOLOR = getString("ui.textcolor").toUpperCase();
		try {
			TextZone.fontcolor = (Color) Color.class
					.getField(Preferences.TEXTCOLOR).get(null);
		} catch (Exception e) {
			TextZone.fontcolor = Color.BLACK;
		}

		initkeys("keys.world", new WorldKeysScreen());
		initkeys("keys.battle", new BattleKeysScreen());

		readdebug();
	}

	private static void initkeys(String propertyname,
			KeysScreen worldKeyScreen) {
		String keys = getString(propertyname);
		if (keys == null) {
			return;
		}
		ArrayList<ActionDescription> actions = worldKeyScreen.getactions();
		for (int i = 0; i < keys.length(); i++) {
			actions.get(i).setMainKey(Character.toString(keys.charAt(i)));
		}
	}

	static void readdebug() {
		DEBUGDISABLECOMBAT = getString("cheat.combat") != null
				&& getString("cheat.combat").equals("false");
		DEBUGESHOWMAP = javelin.controller.db.Preferences
				.getString("cheat.world") != null;
		DEBUGSXP = javelin.controller.db.Preferences.getInteger("cheat.xp",
				null);
		DEBUGSGOLD = javelin.controller.db.Preferences.getInteger("cheat.gold",
				null);
		DEBUGRUBIES = javelin.controller.db.Preferences
				.getInteger("cheat.rubies", null);
		DEBUGLABOR = javelin.controller.db.Preferences.getInteger("cheat.labor",
				null);
		DEBUGCOINS = javelin.controller.db.Preferences.getInteger("cheat.coins",
				null);
		// DEBUGCLEARGARRISON = javelin.controller.db.Preferences
		// .getString("cheat.garrison") != null;
		DEBUGFOE = getString("cheat.monster");
		DEBUGPERIOD = getString("cheat.period");
		DEBUGMAPTYPE = getString("cheat.map");
		DEBUGMINIMUMFOES = getInteger("cheat.foes", null);
		DEBUGWEATHER = getString("cheat.weather");
		DEBUGSEASON = getString("cheat.season");
		DEBUGUNLOCKTEMPLES = getString("cheat.temples") != null;
		initdebug();
	}

	static void initdebug() {
		if (DEBUGESHOWMAP && BattleScreen.active != null
				&& BattleScreen.active.getClass().equals(WorldScreen.class)) {
			for (int x = 0; x < World.SIZE; x++) {
				for (int y = 0; y < World.SIZE; y++) {
					WorldScreen.setVisible(x, y);
				}
			}
		}
		for (WorldActor a : Squad.getall(Squad.class)) {
			initsquaddebug((Squad) a);
		}
		if (DEBUGRUBIES != null && Haxor.singleton != null) {
			Haxor.singleton.rubies = DEBUGRUBIES;
		}
		if (DEBUGCOINS != null && Arena.get() != null) {
			Arena.get().coins = DEBUGCOINS;
		}
		// if (DEBUGLABOR != null) {
		// for (WorldActor a : Town.getall(Town.class)) {
		// Town t = (Town) a;
		// if (!t.ishostile()) {
		// t.turn(time, screen);
		// }
		// }
		// }
		if (DEBUGWEATHER != null) {
			DEBUGWEATHER = DEBUGWEATHER.toLowerCase();
			Weather.read(0); // tests cheat.weather value
		}
		if (DEBUGSEASON != null) {
			Season.current = Season.valueOf(DEBUGSEASON.toUpperCase());
		}
	}

	static void initsquaddebug(Squad s) {
		if (DEBUGSGOLD != null) {
			s.gold = DEBUGSGOLD;
		}
		if (DEBUGSXP != null) {
			for (Combatant c : s.members) {
				c.xp = new BigDecimal(DEBUGSXP / 100f);
			}
		}
	}

	private static float getFloat(String key) {
		return Float.parseFloat(getString(key));
	}

	/**
	 * Reads and writes to actual file.
	 * 
	 * @param key
	 *            Replaces the line with this option or creates a new line at
	 *            the end of the properties file.
	 * @param value
	 *            Option value.
	 */
	synchronized static public void setoption(String key, Object value) {
		String from = getfile();
		String to = "";
		boolean replaced = false;
		for (String line : from.split("\n")) {
			if (line.startsWith(key)) {
				to += key + "=" + value + "\n";
				replaced = true;
			} else {
				to += line + "\n";
			}
		}
		if (!replaced) {
			to += "\n" + key + "=" + value + "\n";
		}
		savefile(to);
	}
}
