package tyrant.mikera.tyrant;

// This is the main Applet class for Tyrant

import java.applet.Applet;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

import javelin.controller.fight.IncursionFight;
import javelin.model.BattleMap;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.LairScreen;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;

/**
 * The main Tyrant applet class, also used as the root UI component when run as
 * an application
 * 
 * TODO: move game-related code out of QuestApp.
 */
public class QuestApp extends Applet implements Runnable {
	private static final long serialVersionUID = 3257569503247284020L;

	public static final Image DEFAULTTEXTURE =
			QuestApp.getImage("/images/texture3.png");

	public static Image tiles;
	public static Image greytiles;
	public static Image scenery;
	public static Image creatures;
	public static Image items;
	public static Image effects;
	public static Image title;
	public static Image penalized;
	public static Image crafting;
	public static Image upgrading;
	public static Image banner;
	public static Image dead;
	public static Image crystal;
	public static Image paneltexture = QuestApp.DEFAULTTEXTURE;

	public static Hashtable images = new Hashtable();

	public static Font mainfont = new Font("Monospaced", Font.BOLD, 15);

	public static int charsize;

	public static final Color TEXTCOLOUR = new Color(192, 192, 192);

	public static final Color BACKCOLOUR = new Color(0, 0, 0);

	// public static final Color panelcolor = new Color(64, 64, 64);
	// public static final Color panelhighlight = new Color(96, 96, 96);
	// public static final Color panelshadow = new Color(32, 32, 32);

	public static final Color PANELCOLOUR = new Color(64, 64, 64);

	public static final Color PANELHIGHLIGHT = new Color(120, 80, 20);

	public static final Color PANELSHADOW = new Color(40, 20, 5);

	public static final Color INFOSCREENCOLOUR = new Color(0, 0, 0);

	public static final Color INFOTEXTCOLOUR = new Color(240, 200, 160);

	public static final Color INFOTEXTCOLOUR_GRAY = new Color(100, 100, 100);

	private BattleScreen screen;

	private Component mainComponent = null;

	private static QuestApp instance;

	public static boolean isapplet = true;

	public static String gameFileFromCommandLine;

	public static String fileEncoding = System.getProperty("file.encoding");

	@Override
	public Dimension getPreferredSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	// stop the applet, freeing all resources used
	@Override
	public void stop() {
		super.stop();
		QuestApp.setInstance(null);
		Game.messagepanel = null;
		Game.thread = null;
	}

	// image filter object to create greyed tiles
	static class GreyFilter extends RGBImageFilter {
		public GreyFilter() {
			canFilterIndexColorModel = true;
		}

		@Override
		public int filterRGB(final int x, final int y, final int rgb) {
			return rgb & 0xff000000 | 0x10101 * (((rgb & 0xff0000) >> 18)
					+ ((rgb & 0xff00) >> 10) + ((rgb & 0xff) >> 2));
		}
	}

	public void init(final Runnable runnable) {
		// recreate lib in background

		QuestApp.setInstance(this);
		Game.setQuestapp(this);

		super.init();
		setLayout(new BorderLayout());
		setBackground(Color.black);
		setFont(QuestApp.mainfont);

		// Game.warn("Focus owned by:
		// "+KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());

		// set game in action
		Game.setUserinterface(new Interface());
		Game.thread = new Thread(runnable);
		Game.thread.start();
	}

	// inits the applet, loading all necessary resources
	// also kicks off the actual game thread
	@Override
	public void init() {
		init(this);
	}

	public QuestApp() {
		super();
	}

	// creates a hero according to specified parameters
	public Thing createHero(final boolean prompts) {
		final long start = System.currentTimeMillis();
		String race = null;
		String profession = null;

		if (!prompts) {
			race = "human";
			profession = "fighter";
			Game.setDebug(true);
		}

		// get list of races
		final String[] raceherostrings = Hero.heroRaces();
		final String[] racedescriptions = Hero.heroRaceDescriptions();
		if (race == null) {
			final DetailedListScreen ls = new DetailedListScreen(
					"What race are you?", raceherostrings, racedescriptions);
			ls.setForeground(new Color(128, 128, 128));
			ls.setBackground(new Color(0, 0, 0));
			ls.bottomString = "Press a letter key to select your race";
			switchScreen(ls);
			while (true) {
				race = (String) ls.getObject();
				if (race != null || Game.isDebug()) {
					break;
				}
			}
		}

		if (race == null) {
			// Debug mode only
			// have escaped, so choose randomly
			race = raceherostrings[RPG.r(raceherostrings.length)];
			final String[] herostrings = Hero.heroProfessions(race);
			profession = herostrings[RPG.r(herostrings.length)];
		}

		// get list of possible prfessions
		final String[] professionstrings = Hero.heroProfessions(race);
		final String[] professiondescriptions =
				Hero.heroProfessionDescriptions(race);
		if (profession == null) {

			final DetailedListScreen ls =
					new DetailedListScreen("What is your profession?",
							professionstrings, professiondescriptions);
			ls.bottomString = "Press a letter key to select your profession";
			ls.setForeground(new Color(128, 128, 128));
			ls.setBackground(new Color(0, 0, 0));
			switchScreen(ls);

			while (profession == null) {
				profession = (String) ls.getObject();
			}
		}

		final Thing h = Hero.createHero(prompts ? null : "QuickTester", race,
				profession);

		// hero name and history display
		String name = "QuickTester";
		if (prompts) {
			// setup screen to get name
			final Screen ss = new Screen(this);
			ss.setBackground(new Color(0, 0, 0));
			ss.setLayout(new BorderLayout());
			{
				final InfoScreen ts =
						new InfoScreen(this, h.getString("HeroHistory"));
				ts.setBackground(new Color(0, 0, 0));
				ss.add("Center", ts);
			}
			final MessagePanel mp = new MessagePanel(this);
			Game.messagepanel = mp;
			ss.add("South", mp);

			switchScreen(ss);

			name = getHeroName(true);
			if (name == null) {
				return null;
			}
		}
		Hero.setHeroName(h, name);

		/*
		 * System.out.println(System.currentTimeMillis() - start +
		 * "ms to createHero");
		 */
		return h;
	}

	public boolean isGameScreen() {
		return screen != null && mainComponent == screen;
	}

	// switches to a new screen, discarding the old one
	public void switchScreen(final Component s) {
		if (s == null) {
			return;
		}
		if (mainComponent == s) {
			// alreay on correct component!
			s.repaint();
			return;
		}
		setVisible(false);
		removeAll();
		add(s);
		invalidate();
		validate();
		setVisible(true);
		/*
		 * CBG This is needed to give the focus to the contained screen.
		 * RequestFocusInWindow is preferable to requestFocus.
		 */
		s.requestFocus();
		mainComponent = s;
	}

	public String getHeroName(final boolean notNull) {

		// ss.invalidate();
		// ss.validate();
		// ss.repaint();

		// get hero name
		String hname = null;

		Game.messageTyrant("");
		while (hname == null || hname.equals("")) {
			hname = Game.getLine("Enter your name: ");
			if ("ESC".equals(hname)) {
				return null;
			}
			if (!notNull && (hname == null || hname.equals(""))) {
				return null;
			}
		}
		return hname;
	}

	// this is the actual game thread start
	// it loops for each complete game played
	@Override
	public void run() {
		while (true) {

			final Screen ss = new Screen(this);
			ss.setBackground(new Color(0, 0, 0));
			ss.setLayout(new BorderLayout());
			{
				final TitleScreen ts = new TitleScreen(this);
				ts.setBackground(new Color(0, 0, 0));
				ss.add("Center", ts);
			}
			final MessagePanel mp = new MessagePanel(this);
			Game.messagepanel = mp;
			ss.add("South", mp);

			switchScreen(ss);

			repaint();

			if (!QuestApp.isapplet
					&& QuestApp.gameFileFromCommandLine != null) {
				Game.messageTyrant("Loading " + QuestApp.gameFileFromCommandLine
						+ " game file...");
				final String ret =
						Game.tryToRestore(QuestApp.gameFileFromCommandLine);
				if (ret == null) {
					setupScreen();
					getScreen().mainLoop();
					continue;
				}

				Game.messageTyrant("Load game failed: " + ret);
				Game.messageTyrant("Press any key (except Tab) to continue");
				Game.getInput(false); // !!! not very good - this does not
				// recognize Tab key, for instance

			}

			Game.messageTyrant("");
			Game.messageTyrant("Welcome to Tyrant. You are playing version "
					+ Game.VERSION + ". Would you like to:");
			Game.messageTyrant(" [a] Create a new character");
			Game.messageTyrant(" [b] Load a previously saved game");
			Game.messageTyrant(" [c] Play in debug mode");
			Game.messageTyrant(" [d] QuickStart debug mode");
			Game.messageTyrant(" [e] Edit a map");
			mp.repaint();

			// create lib in background
			Game.asynchronousCreateLib();

			final char c = Game.getOption("abcdeQ");

			Game.setDebug(false);
			Game.visuals = true;

			if (c == 'b') {
				if (Game.restore()) {
					setupScreen();
					getScreen().mainLoop();

				}

			} else if (c == 'c') {
				// do hero creation
				Game.create();
				final Thing h = createHero(true);
				if (h == null) {
					continue;
				}

				Game.setDebug(true);
				setupScreen();
				preparebattlemap();

			} else if (c == 'e') {
				// // Designer
				// Game.messageTyrant("");
				// Game.messageTyrant("Launching Designer...");
				// tyrant.mikera.tyrant.author.Designer
				// .main(new String[] { "embedded" });
				// continue;

			} else {

				Game.create();
				final Thing h = createHero(true);

				if (h == null) {
					continue;
				}

				// first display starting info....
				final InfoScreen l = new InfoScreen(this,
						"                                 Introduction\n" + "\n"
								+ "Times are hard for the humble adventurer. Lawlessness has ravaged the land, and few can afford to pay for your services.\n"
								+ "\n"
								+ "After many weeks of travel, you find yourself in the valley of North Karrain. This region has suffered less badly from the incursions of evil, and you hear that some small towns are still prosperous. Perhaps here you can find a way to make your fortune.\n"
								+ "\n"
								+ "After a long day of travel, you see a small inn to the west. Perhaps this would be a good place to meet some and learn some more about these strange lands.\n"
								+ "\n"
								+ "                           [ Press a key to continue ]\n"
								+ "\n" + "\n" + "\n" + "\n" + "\n");

				l.setForeground(new Color(192, 160, 64));
				l.setBackground(new Color(0, 0, 0));
				switchScreen(l);
				Game.getInput();
				setupScreen();
				preparebattlemap();

				// Debug mode should not start when pressing Enter!!
				// Game.create();
				// Game.setDebug(true);
				// createHero(false);
				// setupScreen();
				// gameStart();
			}
		}
	}

	public void setupScreen() {
		// if (getScreen() == null) {
		// setScreen(new BattleScreen(this));
		// } else {
		// // only need to reset the messages,
		// // otherwise we will start to
		// // leak memory/threads
		// Game.messagepanel = getScreen().messagepanel;
		// Game.messagepanel.clear();
		// }
		// switchScreen(getScreen());
	}

	protected void gameStart(final BattleMap map, final int entranceX,
			final int entranceY) {
		// while (true) {
		// Thing hero = Game.hero();
		// Game.enterMap(map, hero.x, hero.y);
		//
		// // run the game
		// try {
		// getScreen().mainLoop();
		// } catch (final EndBattleException e) {
		// /*
		// * TODO not working. might as well kill the game and reload the
		// * whole state
		// */
		// final WorldScreen s = new WorldScreen(Javelin.app);
		// switchScreen(s);
		// hero = WorldScreen.worldhero;
		// Game.instance().hero = hero;
		// hero.place = map;
		// }
		// }
	}

	public void preparebattlemap() {
		Game.setQuestapp(this);
		final Thing h = Game.hero();
		if (h == null) {
			throw new Error("Hero not created");
		}
		Game.instance().initialize(h);

		final BattleMap world = Game.instance().createWorld();

		Quest.addQuest(h, Quest.createVisitMapQuest("Vist a town", "town"));

		final Thing port = world.find("tutorial inn");
		final BattleMap tm = Portal.getTargetMap(port);
		gameStart(tm, tm.getEntrance().x, tm.getEntrance().y);
	}

	private String getDeathString(final Thing h) {
		if (h.getStat("HPS") <= 0) {
			final Thing t = h.getThing("Killer");
			if (t == null) {
				return "Killed by divine power";
			}
			t.remove();

			String killer = t.getAName();
			if (t.getFlag("IsEffect")) {
				killer = t.name();
			}

			if (killer.equals("you")) {
				killer = "stupidity";
			}
			return "Killed by " + killer;
		}

		return "Defeated The Tyrant";
	}

	public void gameOver() {
		Wish.makeWish("identification", 100);
		Game.messageTyrant("");

		final Thing h = Game.hero();

		final String outcome = getDeathString(h);

		String story = null;

		getScreen().getMappanel().repaint();

		String hresult = "No high score available in debug mode";

		final int sc = h.getStat("Score");
		final String score = Integer.toString(sc);
		final String level = Integer.toString(h.getLevel());
		final String seed = Integer.toString(h.getStat("Seed"));
		final String name = h.getString("HeroName");
		final String profession = h.getString("Profession");
		final String race = h.getString("Race");

		try {
			final String urldeath =
					URLEncoder.encode(outcome, QuestApp.fileEncoding);
			final String urlname =
					URLEncoder.encode(name, QuestApp.fileEncoding);

			final String check = Integer.toString(
					sc + name.length() * profession.length() * race.length()
							^ 12345678);
			final String st = "&name=" + urlname + "&race=" + race
					+ "&profession=" + profession + "&level=" + level
					+ "&score=" + score + "&check=" + check + "&version="
					+ Game.VERSION + "&seed=" + seed + "&death=" + urldeath;

			final String url =
					"http://tyrant.sourceforge.net/logscore.php?client=tyrant"
							+ st;

			Game.warn((Game.isDebug() ? "NOT " : "") + "Sending data:");
			Game.warn(st);

			if (!Game.isDebug()) {
				final URL u = new URL(url);
				final InputStream s = u.openStream();

				String returnstring = "";
				int b = s.read();
				while (b >= 0) {
					returnstring = returnstring + (char) b;
					b = s.read();
				}

				final int ok = returnstring.indexOf("OK:");
				if (ok >= 0) {
					hresult = "High score logged.\n";
					hresult += "You are in position "
							+ returnstring.substring(ok + 3).trim();
				} else {
					hresult = "Failed to log high score";
					Game.warn(returnstring);
				}
			}
		} catch (final Exception e) {
			Game.warn(e.getMessage());
			hresult = "High score feature not available";
		}

		if (!h.isDead()) {
			story = "You have defeated The Tyrant!\n" + "\n"
					+ "Having saved the world from such malevolent evil, you are crowned as the new Emperor of Daedor, greatly beloved by all the people of the Earth.\n"
					+ "\n"
					+ "You rule an Empire of peace and prosperity, and enjoy a long and happy life.\n"
					+ "\n" + "Hurrah for Emperor " + h.getString("HeroName")
					+ "!!\n";

			if (Game.isDebug()) {
				story = "You have defeated The Tyrant in Debug Mode.\n" + "\n"
						+ "Now go and do it the hard way....\n";
			}

		} else {
			story = "\n" + "It's all over...... " + outcome + "\n" + "\n"
					+ "You have failed in your adventures and died a hideous death.\n"
					+ "\n" + "You reached level " + level + "\n"
					+ "Your score is " + score + "\n" + "\n" + hresult + "\n";
		}

		Game.messageTyrant("GAME OVER - " + outcome);

		Game.messageTyrant(
				"Would you like to see your final posessions? (y/n)");

		final char c = Game.getOption("yn");

		if (c == 'y') {
			Game.selectItem("Your final posessions:", h);
		}

		// display the final story
		Game.scrollTextScreen(story);

		// display the final story
		final String killData = Hero.reportKillData();
		Game.scrollTextScreen(killData);

		Game.over = true;

		Lib.clear();

		// recreate lib in background
		Game.asynchronousCreateLib();
	}

	public KeyAdapter keyhandler = null;

	// All keypresses get directed here.....
	public final KeyAdapter keyadapter = new KeyAdapter() {
		@Override
		public void keyPressed(final KeyEvent e) {
			// Game.warn("Focus owned by:
			// "+KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
			// Game.warn(""+e.getKeyChar());

			// call the currently registered keyhandler
			if (keyhandler != null) {
				keyhandler.keyPressed(e);
			} else {
				Game.getUserinterface().go(e);
			}
		}
	};

	@Override
	public void destroy() {
		removeAll();
	}

	// loads an image from wherever possible
	// ideally, from the .jar resource bundle
	// not sure if all of this is necessary
	// but try to cover all possible environments
	public static Image getImage(final String filename) {
		final URL imageURL = QuestApp.class.getResource(filename);
		if (imageURL == null) {
			return null;
		}
		return Toolkit.getDefaultToolkit().getImage(imageURL);

	}

	public static Image maketransparent(float alpha, Image image) {
		BufferedImage transparent =
				new BufferedImage(32, 32, Transparency.TRANSLUCENT);
		Graphics2D g = transparent.createGraphics();
		g.setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return transparent;
	}

	static final GraphicsConfiguration configuration =
			GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getDefaultScreenDevice().getDefaultConfiguration();

	/**
	 * @param screen
	 *            The screen to set.
	 */
	public void setScreen(final BattleScreen screen) {
		this.screen = screen;
	}

	public BattleScreen getScreen() {
		return screen;
	}

	public static void setInstance(final QuestApp instance) {
		QuestApp.instance = instance;
	}

	public static QuestApp getInstance() {
		if (QuestApp.instance == null) {
			QuestApp.instance = new QuestApp();
		}
		return QuestApp.instance;
	}

	static {
		final Applet applet = new Applet();

		QuestApp.tiles = QuestApp.getImage("/images/tiles32.png");
		QuestApp.scenery = QuestApp.getImage("/images/scenery32.png");
		QuestApp.creatures = QuestApp.getImage("/images/creature32.png");
		QuestApp.items = QuestApp.getImage("/images/items32.png");
		QuestApp.effects = QuestApp.getImage("/images/effects32.png");
		QuestApp.title = QuestApp.getImage("/images/title.png");
		QuestApp.penalized = QuestApp.getImage("/images/spiralbig.png");
		QuestApp.crafting = QuestApp.getImage("/images/crafting.png");
		QuestApp.upgrading = QuestApp.getImage("/images/upgrading.png");
		QuestApp.banner = QuestApp.getImage("/images/banner.png");
		QuestApp.dead = QuestApp.getImage("/images/dead.png");
		QuestApp.crystal = QuestApp.getImage("/images/meld.png");

		// store images in source hashtable
		QuestApp.images.put("Tiles", QuestApp.tiles);
		QuestApp.images.put("Scenery", QuestApp.scenery);
		QuestApp.images.put("Creatures", QuestApp.creatures);
		QuestApp.images.put("Items", QuestApp.items);
		QuestApp.images.put("Effects", QuestApp.effects);

		// Create mediatracker for the images
		final MediaTracker mediaTracker = new MediaTracker(applet);
		mediaTracker.addImage(QuestApp.tiles, 1);
		mediaTracker.addImage(QuestApp.scenery, 1);
		mediaTracker.addImage(QuestApp.creatures, 1);
		mediaTracker.addImage(QuestApp.items, 1);
		mediaTracker.addImage(QuestApp.effects, 1);
		mediaTracker.addImage(QuestApp.title, 1);
		mediaTracker.addImage(QuestApp.penalized, 1);
		mediaTracker.addImage(QuestApp.crafting, 1);
		mediaTracker.addImage(QuestApp.upgrading, 1);
		mediaTracker.addImage(QuestApp.banner, 1);
		mediaTracker.addImage(QuestApp.dead, 1);
		mediaTracker.addImage(QuestApp.crystal, 1);
		mediaTracker.addImage(QuestApp.DEFAULTTEXTURE, 1);
		mediaTracker.addImage(LairScreen.DUNGEONTEXTURE, 1);
		mediaTracker.addImage(IncursionFight.INCURSIONTEXTURE, 1);

		// create grey-filtered background tiles
		final ImageFilter imf = new GreyFilter();
		QuestApp.greytiles = applet.createImage(
				new FilteredImageSource(QuestApp.tiles.getSource(), imf));

		// Wait for images to load
		try {
			mediaTracker.waitForID(1);
			QuestApp.penalized =
					QuestApp.maketransparent(2 / 3f, QuestApp.penalized);
		} catch (final Exception e) {
			System.out.println("Error loading images.");
			e.printStackTrace();
		}

	}

}