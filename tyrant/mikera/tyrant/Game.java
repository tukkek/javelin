package tyrant.mikera.tyrant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javelin.controller.db.Properties;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.MapPanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.author.MapMaker;
import tyrant.mikera.tyrant.util.Text;

public final class Game extends BaseObject {
	private static final int MESSAGEWAIT = Math.round(
			1000 * Float.parseFloat(Properties.getString("ui.messagedelay")));

	private static final long serialVersionUID = 3544670698288460592L;

	// temp: static game instance
	private static Game instance = new Game();

	public static transient IMessageHandler messagepanel;

	// version number is updated in QuestApp.init()
	public static String VERSION = null;

	// reference to the Hero object
	public Thing hero;

	private transient InputHandler inputHandler = null;

	// The actor field stores which being is currently
	// taking an action. Primary use is to determine
	// whether the hero is responsible for inflicting
	// damage, i.t. whether beasties get angry or not.
	public static Thing actor;

	// Game over flag
	// Set to true during game to terminate main loop
	public static boolean over = true;

	// Interface helper object
	private static Interface userinterface;

	// Flag that shows if save() call is first or no
	private static boolean saveHasBeenCalledAlready = true;

	// Thread for recieveing user input
	public static Thread thread;

	public static int seed() {
		return hero().getStat("Seed");
	}

	public static void loadVersionNumber() {
		if (VERSION != null) {
			return;
		}
		java.util.Properties props = null;

		// load version number
		try {
			final InputStream fis =
					Game.class.getResourceAsStream("/version.txt");
			props = new java.util.Properties();
			props.load(fis);
			Game.VERSION = props.getProperty("version");
		} catch (final Exception e) {
			Game.warn("Version number problem!");
			e.printStackTrace();
		}
	}

	/**
	 * List of recent messages
	 */
	private final ArrayList<String> messageList = new ArrayList<String>();
	private boolean debug = false;
	// toggle for visual effects
	public static boolean visuals = false;

	public static boolean delayblock = false;

	public ArrayList<String> getMessageList() {
		return messageList;
	}

	public String messageList() {
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < messageList.size(); i++) {
			sb.append("\n" + messageList.get(i));
		}
		return sb.toString();
	}

	/**
	 * Print a general game message All messages should be routed through here
	 * or quotedMessage()
	 */
	public static void messageTyrant(final String s) {
		s.getClass();
		// if (instance().messageStack.isEmpty()) {
		// instance().displayMessage(s);
		// } else {
		// final ArrayList al = (ArrayList) instance().messageStack.peek();
		// al.add(s);
		// }
	}

	public enum Delay {
		NONE, WAIT, BLOCK
	}

	/**
	 * Print a general game message All messages should be routed through here
	 * or quotedMessage()
	 */
	public static void message(final String in, final Combatant t,
			final Delay d) {
		final String out = t == null ? in : t + ":" + " " + in + ".";
		messagepanel.add(out);
		switch (d) {
		case WAIT:
			try {
				redraw();
				Thread.sleep(MESSAGEWAIT);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			messagepanel.clear();
			break;
		case BLOCK:
			messagepanel.add("\n" + TextZone.BLACK + "-- ENTER --");
			delayblock = true;
			break;
		}
	}

	/**
	 * Print a general game message, surrounded by quotation marks All messages
	 * should be routed through here or message()
	 */
	public static void quotedMessage(final String s) {
		final String sToAdd = "\"" + s + "\"";
		if (instance().messageStack.isEmpty()) {
			// instance().displayMessage(sToAdd);
		} else {
			final ArrayList<Object> al = instance().messageStack.peek();
			al.add(sToAdd);
		}
	}

	/**
	 * Prints a set of messages stored in an ArrayList This is designed for use
	 * with popMessages();
	 * 
	 * @param al
	 *            ArrayList containing String objects for each message
	 */
	public static void message(final ArrayList al) {
		for (int i = 0; i < al.size(); i++) {
			messageTyrant((String) al.get(i));
		}
	}

	/*
	 * Holds messages in a stack so that they can be displayed later
	 */
	public void pushMessages() {
		messageStack.push(new ArrayList());
	}

	/*
	 * Pulls a stored set of messages off the stack
	 */
	public ArrayList popMessages() {
		return messageStack.pop();
	}

	public void clearMessageList() {
		messageStack = new Stack<ArrayList<Object>>();
	}

	public static char showData(final String s) {
		final java.awt.Component comp = getQuestapp().getScreen();
		final InfoScreen is = new InfoScreen(getQuestapp(), "");
		final TextArea ma = new TextArea();
		is.setLayout(new BorderLayout());
		ma.setText(s);
		ma.setBackground(Color.darkGray);
		ma.setForeground(Color.lightGray);
		ma.addKeyListener(getQuestapp().keyadapter);
		is.add(ma);
		getQuestapp().switchScreen(is);
		ma.setCaretPosition(1000000);
		final char c = Game.getChar();
		getQuestapp().switchScreen(comp);
		return c;
	}

	/**
	 * Stack to store messages before they are displayed.
	 * 
	 * This is useful so that you can defer the display of messages until after
	 * the result of several actions has been dtermined
	 * 
	 * Game.pushMessages(); .... Do something complex here, possibly generating
	 * many messages ArrayList al=Game.popMessages(); Game.message("The
	 * following interesting things happen:); Game.message(al); // now display
	 * the messages
	 * 
	 * You can also use this method to suppress messages (use exactly the same
	 * technique, but omit the final line).
	 */
	/**
	 * TODO parametrize
	 */
	public Stack<ArrayList<Object>> messageStack =
			new Stack<ArrayList<Object>>();
	private boolean isDesigner = false;

	private void displayMessage(String s) {
		if (s == null) {
			Game.warn("Null message!");
			return;
		}
		s = Text.capitalise(s);
		if (!s.equals("")) {
			int number = 1;
			if (messageList.size() > 0) {
				final String last = messageList.get(messageList.size() - 1);
				final int x = last.indexOf("x");
				if (last.startsWith(s) && x > 0) {
					try {
						number = Integer.parseInt(last.substring(x + 1)) + 1;
					} catch (final Exception e) {
						Game.warn("Count problem: " + last);
						number = 1;
					}
				} else if (last.equals(s)) {
					number = 2;
				}
			}

			// add count to repeated messages
			if (number > 1) {
				messageList.set(messageList.size() - 1, s + " x" + number);
			} else {
				messageList.add(s);
			}
		}
	}

	public InputHandler createInputHandler() {
		return new InputHandler() {
			@Override
			public char getCharacter() {
				return getKeyEvent().getKeyChar();
			}

			@Override
			public KeyEvent getKeyEvent() {
				getUserinterface().getInput();
				return getUserinterface().keyevent;
			}
		};
	}

	/**
	 * Wait for an single key press
	 */
	public static KeyEvent getInput() {
		return getInput(true);
	}

	public static KeyEvent getInput(final boolean redraw) {
		if (false && redraw && getMappanel() != null
				&& getQuestapp().isGameScreen()) {
			redraw();
		}
		if (messagepanel != null && messagepanel instanceof MessagePanel) {
			((MessagePanel) messagepanel).repaint();
		}

		final Game g = Game.instance();
		if (g.inputHandler == null) {
			g.inputHandler = g.createInputHandler();
		}

		return g.inputHandler.getKeyEvent();
	}

	public static void redraw() {
		getMappanel().render();
		getMappanel().repaint();
		messagepanel.getPanel().repaint();
	}

	/**
	 * Request a line of text from the user
	 * 
	 * @param prompt
	 *            The text prompt for the input
	 */
	public static String getLine(final String prompt) {
		return getLine(prompt, "");
	}

	/**
	 * Request a line of text from the user
	 * 
	 * @param prompt
	 *            The text prompt for the input
	 * @param result
	 *            The existing/default text
	 */
	public static String getLine(final String prompt, String result) {
		messagepanel.add(prompt + result);
		messagepanel.getPanel().invalidate();
		messagepanel.getPanel().repaint();
		while (true) {
			final KeyEvent k = getInput(false);
			final char ch = k.getKeyChar();
			if (k.getKeyCode() == KeyEvent.VK_ENTER) {
				break;
			}
			if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
				result = "ESC";
				break;
			}
			if (k.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
				// add the character to the input string if typed
				// i.e. don't include SHIFT, ALT etc.
				if (Character.isLetterOrDigit(ch)
						|| " -.:+'[]()=<>".indexOf(ch) >= 0) {
					result = result + ch;
				}
			} else if (result.length() > 0) {
				result = result.substring(0, result.length() - 1);
			}
			messagepanel.clear();
			messagepanel.add(prompt + result);
		}
		Game.messageTyrant("");
		return result;
	}

	/**
	 * Simulate a key press Useful for handling equivalent mouse clicks
	 */
	public static void simulateKey(final char c) {
		if (getUserinterface() != null) {
			final KeyEvent k = new KeyEvent(getMappanel(), 0,
					System.currentTimeMillis(), 0, 0, 'c');
			k.setKeyChar(c);
			getUserinterface().go(k);
		}
	}

	public static void infoScreen(final String s) {
		final Screen old = getQuestapp().getScreen();
		final InfoScreen is = new InfoScreen(getQuestapp(), s);
		getQuestapp().switchScreen(is);
		Game.getInput();
		getQuestapp().switchScreen(old);
	}

	public static void scrollTextScreen(final String s) {
		scrollTextScreen(s, false);
	}

	public static void scrollTextScreen(final String s, final boolean showEnd) {

		final QuestApp questapp = Game.getQuestapp();
		final Screen old = questapp.getScreen();
		final InfoScreen is = new InfoScreen(questapp, "");
		final TextArea ma = new TextArea();
		is.setLayout(new BorderLayout());

		ma.setText(s);
		ma.setBackground(Color.darkGray);
		ma.setForeground(Color.lightGray);
		ma.addKeyListener(questapp.keyadapter);
		is.add(ma);
		questapp.switchScreen(is);

		if (showEnd) {
			ma.setCaretPosition(1000000);
		}
		Game.getInput();
		questapp.switchScreen(old);
	}

	public static void viewMap(final BattleMap m) {
		if (getQuestapp().getScreen() == null) {
			getQuestapp().setupScreen();
		}
		final BattleScreen gs = getQuestapp().getScreen();

		gs.map = m;
		gs.getMappanel().map = m;

	}

	// transport to location of particular map
	public static void enterMap(final BattleMap m, final int tx, final int ty) {
		viewMap(m);
		Game.messageTyrant("jTacticalRpg");
		// update highest reached level if necessary
		if (hero().getStat(RPG.ST_SCORE_BESTLEVEL) < m.getLevel()) {
			hero().set(RPG.ST_SCORE_BESTLEVEL, m.getLevel());
		}
	}

	/**
	 * Gets the map sorage HashMap
	 * 
	 * @return
	 */
	public HashMap getMapStore() {
		HashMap h = (HashMap) Game.instance().get("MapStore");
		if (h == null) {
			h = new HashMap();
			Game.instance().set("MapStore", h);
		}
		return h;
	}

	private HashMap<String, ArrayList<Thing>> getMapObjectStore() {
		HashMap<String, ArrayList<Thing>> h =
				(HashMap<String, ArrayList<Thing>>) Game.instance()
						.get("MapObjectStore");
		if (h == null) {
			h = new HashMap<String, ArrayList<Thing>>();
			Game.instance().set("MapObjectStore", h);
		}
		return h;
	}

	private ArrayList<Thing> getMapObjectList(final String mapName) {
		final HashMap<String, ArrayList<Thing>> h = getMapObjectStore();
		ArrayList<Thing> al = h.get(mapName);
		if (al == null) {
			al = new ArrayList<Thing>();
			h.put(mapName, al);
		}
		return al;
	}

	/**
	 * Adds a thing to a map, storing it in a temporary queue if the map is not
	 * yet created
	 * 
	 * @param t
	 *            The thing to add
	 * @param mapName
	 *            The map name
	 */
	public void addMapObject(final Thing t, final String mapName) {
		addMapObject(t, mapName, 0, 0);
	}

	public void addMapObject(final Thing t, final String mapName, final int x,
			final int y) {
		t.remove();
		t.x = x;
		t.y = y;

		final BattleMap map = (BattleMap) getMapStore().get(mapName);
		if (map == null) {
			final ArrayList<Thing> al = getMapObjectList(mapName);
			al.add(t);
		} else {
			addMapObject(t, map);
		}
	}

	private void addMapObject(final Thing t, final BattleMap map) {
		if (t.x == 0 && t.y == 0) {
			map.addThing(t);
		} else {
			map.addThing(t, t.x, t.y);
		}
	}

	public void addMapObjects(final BattleMap map) {
		final ArrayList<Thing> obs =
				getMapObjectList(map.getString("HashName"));
		for (final Thing t : obs) {
			addMapObject(t, map);
		}
		obs.clear();
	}

	public BattleMap createWorld() {
		set("MapStore", null);
		return Portal.getMap("karrain", 1, 0);
	}

	public void compressAllData() {
		final HashMap hs = new HashMap();

		final HashMap store = getMapStore();
		final Set keySet = getMapStore().keySet();

		for (final Iterator it = keySet.iterator(); it.hasNext();) {
			final BattleMap m = (BattleMap) store.get(it.next());
			compressMapData(hs, m);
		}
	}

	private void compressMapData(final HashMap hs, final BattleMap m) {
		final Thing[] ts = m.getThings();
		for (final Thing element : ts) {
			element.compressData(hs);
		}
	}

	public static void assertTrue(final boolean condition) {
		if (!condition) {
			try {
				throw new AssertionError();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// has same effect as pressing stipulated direction key
	public static void simulateDirection(final int dx, final int dy) {
		switch (dy) {
		case -1:
			switch (dx) {
			case 1:
				simulateKey('9');
				return;
			case 0:
				simulateKey('8');
				return;
			case -1:
				simulateKey('7');
				return;
			}
		case 0:
			switch (dx) {
			case 1:
				simulateKey('6');
				return;
			case 0:
				simulateKey('5');
				return;
			case -1:
				simulateKey('4');
				return;
			}
		case 1:
			switch (dx) {
			case 1:
				simulateKey('3');
				return;
			case 0:
				simulateKey('2');
				return;
			case -1:
				simulateKey('1');
				return;
			}
		}

		return;
	}

	// Waits for user to select one of specified keys
	// returns: key pressed (from list)
	// or: 'q' if ESC is pressed
	// or: 'e' if ENTER is pressed
	public static char getOption(final String s) {
		while (true) {
			final KeyEvent k = getInput();

			final char c = BattleScreen.getKey(k);
			if (s.indexOf(c) >= 0) {
				return c;
			}
			if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
				return 'q';
			}
			if (k.getKeyCode() == KeyEvent.VK_ENTER) {
				return 'e';
			}
		}
	}

	public static char getChar() {
		final KeyEvent k = getInput();
		return k.getKeyChar();
	}

	/**
	 * Get a number
	 * 
	 * @param prompt
	 * @param max
	 * @return Number entered, max if too high or 0 if too low or ESC is pressed
	 */
	public static int getNumber(final String prompt, final int max) {
		final String line = getLine(prompt);
		try {
			if (line.equals("ESC")) {
				return 0;
			}
			if (line.equals("") || line.equals("all")) {
				return max;
			}
			int r = Integer.parseInt(line);
			r = RPG.middle(0, r, max);
			return r;
		} catch (final Exception e) {
			Game.warn("Invalid number in Game.getNumber(...)");
		}
		return 0;
	}

	public static void warn(final String s) {
		if (Game.isDebug()) {
			System.out.println(s);
		}
	}

	/**
	 * Temporary access method for Game.instance
	 * 
	 * @return
	 */
	public static Game instance() {
		return instance;
	}

	/**
	 * Returns the current level (hero's level)
	 * 
	 * @return Difficulty level
	 */
	public static int level() {
		return hero() == null ? 1 : hero().getLevel();
	}

	/**
	 * Choose a direction, given as a Point offset
	 */
	public static Point getDirection() {
		while (true) {
			final KeyEvent e = Game.getInput();

			char k = Character.toLowerCase(e.getKeyChar());
			final int i = e.getKeyCode();

			// handle key conversions
			if (i == KeyEvent.VK_UP) {
				k = '8';
			}
			if (i == KeyEvent.VK_DOWN) {
				k = '2';
			}
			if (i == KeyEvent.VK_LEFT) {
				k = '4';
			}
			if (i == KeyEvent.VK_RIGHT) {
				k = '6';
			}
			if (i == KeyEvent.VK_HOME) {
				k = '7';
			}
			if (i == KeyEvent.VK_END) {
				k = '1';
			}
			if (i == KeyEvent.VK_PAGE_UP) {
				k = '9';
			}
			if (i == KeyEvent.VK_PAGE_DOWN) {
				k = '3';
			}
			if (i == KeyEvent.VK_ESCAPE) {
				k = 'q';
			}
			if (k == 'q') {
				return null;
			}
			final Point direction = getQuestapp().getScreen().gameHandler
					.convertKeyToDirection(k);
			if (direction != null) {
				return direction;
			}
		}
	}

	/**
	 * 
	 * Gets player to select a string from given list Calls inventory-style
	 * screen Restores original screen before returning
	 */
	public static String selectString(final String message,
			final String[] strings) {
		final Screen old = getQuestapp().getScreen();
		final ListScreen ls = new ListScreen(message, strings);
		getQuestapp().switchScreen(ls);
		final String ret = (String) ls.getObject();
		getQuestapp().switchScreen(old);
		return ret;
	}

	public static String selectString(final String message,
			final ArrayList strings) {
		return selectString(message, strings, strings);
	}

	public static String selectString(final String message,
			final ArrayList strings, final ArrayList results) {
		final String[] ss = new String[strings.size()];
		for (int i = 0; i < ss.length; i++) {
			ss[i] = (String) strings.get(i);
		}
		final int i = strings.indexOf(selectString(message, ss));
		return i >= 0 ? (String) results.get(i) : null;
	}

	/**
	 * Gets player to select an item from given list
	 */
	public static Thing selectItem(final String message, final Thing[] things) {
		return null;
		// return selectItem(message, things, false);
	}

	// /**
	// * Gets player to select an item from given list
	// */
	// public static Thing selectItem(final String message, final Thing[]
	// things,
	// final boolean rememberFilter) {
	// Item.tryIdentify(Game.hero(), things);
	// final Screen old = getQuestapp().getScreen();
	// final InventoryScreen is = getQuestapp().getScreen()
	// .getInventoryScreen();
	// is.setUp(message, null, things);
	// if (!rememberFilter) {
	// is.inventoryPanel.clearFilter();
	// }
	// getQuestapp().switchScreen(is);
	// final Thing ret = is.getObject();
	// getQuestapp().switchScreen(old);
	// return ret;
	// }
	//
	// /**
	// * Allow player to select an item to sell
	// *
	// * Used by shopkeeper (chat)
	// *
	// * @param message
	// * Message to display at top of inventory
	// * @param seller
	// * Seller of the items, i.e. the hero
	// * @param buyer
	// * Buyer of the items, i.e. the shopkeeper
	// * @return
	// */
	// public static Thing selectSaleItem(final String message,
	// final Thing seller, final Thing buyer) {
	// final Thing[] things = seller.getItems();
	// Item.tryIdentify(seller, things);
	// final Screen old = getQuestapp().getScreen();
	// final InventoryScreen is = getQuestapp().getScreen()
	// .getInventoryScreen();
	// is.setUp(message, buyer, things);
	// getQuestapp().switchScreen(is);
	// final Thing ret = is.getObject();
	// getQuestapp().switchScreen(old);
	// return ret;
	// }
	//
	// public static int selectSaleNumber(final String message,
	// final Thing seller, final Thing buyer, final int max) {
	// final Thing[] things = seller.getItems();
	// Item.tryIdentify(seller, things);
	// final InventoryScreen is = Game.getQuestapp().getScreen()
	// .getInventoryScreen();
	// is.setUp(message, buyer, things);
	// Game.getQuestapp().switchScreen(is);
	// final String line = is.getLine();
	// try {
	// if (line.equals("ESC")) {
	// return 0;
	// }
	// if (line.equals("") || line.equals("all")) {
	// return max;
	// }
	// int r = Integer.parseInt(line);
	// r = RPG.middle(0, r, max);
	// return r;
	// } catch (final Exception e) {
	// Game.warn("Invalid number in Game.getNumber(...)");
	// }
	// return 0;
	// }

	public static Thing selectItem(final String message, final Thing owner) {
		return selectItem(message, owner.getItems());
	}

	public static boolean saveMap(final BattleMap m) {
		try {
			String filename = "map.xml";
			final FileDialog fd =
					new FileDialog(new Frame(), "Save Map", FileDialog.SAVE);
			fd.setFile(filename);
			fd.setVisible(true);

			if (fd.getFile() != null) {
				filename = fd.getFile();
			} else {
				// cancel
				return false;
			}

			final FileOutputStream f = new FileOutputStream(filename);
			final PrintWriter pw = new PrintWriter(f);
			final String mapXML = m.getLevelXML();
			pw.write(mapXML);
			Game.warn(mapXML);
			pw.flush();
			f.close();

			Game.messageTyrant("Map saved - " + filename);

			return true;
		} catch (final Exception e) {
			Game.messageTyrant("Error encountered while saving the map");
			if (QuestApp.isapplet) {
				Game.messageTyrant(
						"This may be due to your browser security restrictions");
				Game.messageTyrant(
						"If so, run the web start or downloaded application version instead");
			}
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * Save game to local ZIP file&#1102;
	 * 
	 * @return <0, when the saving failed. 0, when user refused to save the
	 *         game. >0, when the saving succeded.
	 */
	// save game to local ZIP file
	public static int save() {
		try {
			String filename = "tyrant.sav";
			final FileDialog fd =
					new FileDialog(new Frame(), "Save Game", FileDialog.SAVE);
			fd.setFile(filename);
			fd.setVisible(true);

			if (fd.getFile() != null) {
				filename = fd.getDirectory() + fd.getFile();
			} else {
				// return zero on cancel
				return 0;
			}

			final FileOutputStream f = new FileOutputStream(filename);
			final ZipOutputStream z = new ZipOutputStream(f);

			z.putNextEntry(new ZipEntry("data.xml"));

			if (!save(new ObjectOutputStream(z))) {
				throw new Error("Save game failed");
			}

			Game.messageTyrant("Game saved - " + filename);
			z.closeEntry();
			z.close();

			if (saveHasBeenCalledAlready) {
				Game.messageTyrant(
						"Please note that you can only restore the game with the same version of Tyrant (v"
								+ VERSION + ").");
			}
			saveHasBeenCalledAlready = false;
		} catch (final Exception e) {
			Game.messageTyrant("Error while saving: " + e.toString());
			if (QuestApp.isapplet) {
				Game.messageTyrant(
						"This may be due to your browser security restrictions");
				Game.messageTyrant(
						"If so, run the web start or downloaded application version instead");
			}
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	public BattleMap loadMap(final String path) {
		try {
			final InputStream inStream = getClass().getResourceAsStream(path);

			final StringBuffer contents = new StringBuffer();
			String line = null;
			final BufferedReader reader =
					new BufferedReader(new InputStreamReader(inStream));
			while ((line = reader.readLine()) != null) {
				contents.append(line);
				contents.append(MapMaker.NL);
			}
			final BattleMap map = new tyrant.mikera.tyrant.author.MapMaker()
					.create(contents.toString(), false);

			return map;
		} catch (final Throwable x) {
			x.printStackTrace();
			return null;
		}
	}

	/**
	 * Tries to restore game from file specified in the argument.
	 * 
	 * @return null when the restoring was OK; otherwise string with description
	 *         of the problem.
	 */
	public static String tryToRestore(final String filename) {
		String ret = null;
		try {
			final FileInputStream f = new FileInputStream(filename);
			final ZipInputStream z = new ZipInputStream(f);
			z.getNextEntry();
			if (!restore(new ObjectInputStream(z))) {
				ret = "Cannot load " + filename + " game file";
			}
			z.closeEntry();
			z.close();
		} catch (final Exception e) {
			e.printStackTrace();
			if (e.toString() == null) {
				ret = "Unknown problem"; // I hope situation when
				// "e.toString() == null"
				// is not possible; but just in case
			} else {
				ret = e.toString();
			}
		}
		return ret;
	}

	// restore game from local zip file
	public static boolean restore() {
		String ret;
		try {
			String filename = "tyrant.sav";
			final FileDialog fd =
					new FileDialog(new Frame(), "Load Game", FileDialog.LOAD);
			fd.setFile(filename);
			fd.setVisible(true);

			if (fd.getFile() != null) {
				filename = fd.getDirectory() + fd.getFile();
			} else {
				// cancel
				return false;
			}

			ret = tryToRestore(filename);
			if (ret == null) {
				return true;
			}
		} catch (final Exception e) {
			ret = e.toString();
			e.printStackTrace();
		}
		Game.messageTyrant("Load game failed: " + ret);
		if (QuestApp.isapplet) {
			Game.messageTyrant(
					"This may be due to browser security restrictions");
			Game.messageTyrant(
					"If so, run the downloaded application version instead");
		}
		return false;
	}

	// creates a pseudo-random number based on:
	// 1. The seed vale
	// 2. The hero instance
	// 3. The max value
	public static int hash(final int seed, final int max) {
		return (seed ^ hero().getStat("Seed")) % max;
	}

	/**
	 * Perform important initialisation of static fields
	 * 
	 * @param h
	 */
	public void initialize(final Thing h) {
		hero = h;

		if (h == null) {
			throw new Error("Null hero in Game.initialize()");
		}

		Lib library = (Lib) get("Library");
		if (library != null) {
			Lib.setInstance(library);
		} else {
			library = Lib.instance();
			if (library == null) {
				throw new Error("No library in Game.initialize()");
			}
			set("Library", library);
		}

	}

	public static void create() {
		instance = new Game();
	}

	public static boolean save(final ObjectOutputStream o) {
		try {
			final Game g = Game.instance();
			g.compressAllData();

			o.writeObject(Game.instance());
			o.flush();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean restore(final ObjectInputStream i) {
		try {
			Lib.clear();

			instance = (Game) i.readObject();

			// do post-load initialisation of data structures
			instance.initialize(instance.hero);

		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	// called when something is kiled or destroyed
	public static void registerDeath(final Thing t) {
		Being.registerKill(Game.actor, t);
	}

	/**
	 * @param hero
	 *            The hero to set.
	 */
	public void setHero(final Thing hero) {
		Game.instance().hero = hero;
		hero.set("Game", instance());
	}

	/**
	 * @return Returns the hero.
	 */
	public static Thing hero() {
		return instance().hero;
	}

	public char getCharacter() {
		final KeyEvent k = getInput();
		return BattleScreen.getKey(k);
	}

	public void setInputHandler(final InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}

	/**
	 * @return Returns the current mappanel.
	 */
	public static MapPanel getMappanel() {
		final QuestApp q = Game.getQuestapp();
		if (q == null) {
			return null;
		}
		final BattleScreen gs = q.getScreen();
		if (gs == null) {
			return null;
		}
		return gs.getMappanel();
	}

	/**
	 * @param debug
	 *            The debug to set.
	 */
	public static void setDebug(final boolean debug) {
		Game.instance().debug = debug;
	}

	/**
	 * @return Returns the debug.
	 */
	public static boolean isDebug() {
		return Game.instance().debug;
	}

	/**
	 * @param questapp
	 *            The questapp to set.
	 */
	public static void setQuestapp(final QuestApp questapp) {
		QuestApp.setInstance(questapp);
	}

	/**
	 * @return Returns the questapp.
	 */
	public static QuestApp getQuestapp() {
		return QuestApp.getInstance();
	}

	/**
	 * @param userinterface
	 *            The userinterface to set.
	 */
	public static void setUserinterface(final Interface userinterface) {
		Game.userinterface = userinterface;
	}

	/**
	 * @return Returns the userinterface.
	 */
	public static Interface getUserinterface() {
		return userinterface;
	}

	public void setDesignerMode(final boolean isDesigner) {
		this.isDesigner = isDesigner;
	}

	public boolean isDesigner() {
		return isDesigner;
	}

	public static void asynchronousCreateLib() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Lib.instance();
			}
		}).start();
	}
}