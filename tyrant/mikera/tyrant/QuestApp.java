package tyrant.mikera.tyrant;

// This is the main Applet class for Tyrant

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.net.URL;
import java.util.Hashtable;

import javelin.controller.fight.IncursionFight;
import javelin.controller.old.Game;
import javelin.controller.old.Interface;
import javelin.model.unit.Squad;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.WorldScreen;

/**
 * The main Tyrant applet class, also used as the root UI component when run as
 * an application
 * 
 * TODO: move game-related code out of QuestApp.
 */
public abstract class QuestApp extends Applet implements Runnable {
	private static final long serialVersionUID = 3257569503247284020L;

	public static final Image DEFAULTTEXTURE = QuestApp.getImage("/images/texture3.png");

	public static Image tiles;
	public static Image greytiles;
	public static Image scenery;
	public static Image creatures;
	public static Image items;
	public static Image effects;
	public static Image title;
	public static Image paneltexture = QuestApp.DEFAULTTEXTURE;

	static final GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice().getDefaultConfiguration();

	public static Hashtable images = new Hashtable();

	public static Font mainfont = new Font("Monospaced", Font.BOLD, 15);

	public static int charsize;

	public static final Color TEXTCOLOUR = new Color(192, 192, 192);

	public static final Color BACKCOLOUR = new Color(0, 0, 0);

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
			return rgb & 0xff000000
					| 0x10101 * (((rgb & 0xff0000) >> 18) + ((rgb & 0xff00) >> 10) + ((rgb & 0xff) >> 2));
		}
	}

	public void init(final Runnable runnable) {
		// recreate lib in background

		QuestApp.setInstance(this);

		super.init();
		setLayout(new BorderLayout());
		setBackground(Color.black);
		setFont(QuestApp.mainfont);
		// "+KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());

		// set game in action
		Game.userinterface = new Interface();
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
		if (s instanceof WorldScreen && Squad.active != null) {
			// ((WorldScreen) s).mappanel.center(Squad.active.x, Squad.active.y,
			// false);
			((WorldScreen) s).firstdraw = true;
		}
		setVisible(true);
		/*
		 * CBG This is needed to give the focus to the contained screen.
		 * RequestFocusInWindow is preferable to requestFocus.
		 */
		s.requestFocus();
		mainComponent = s;
	}

	public void setupScreen() {
	}

	public KeyAdapter keyhandler = null;

	// All keypresses get directed here.....
	public final KeyAdapter keyadapter = new KeyAdapter() {
		@Override
		public void keyPressed(final KeyEvent e) {
			// call the currently registered keyhandler
			if (keyhandler != null) {
				keyhandler.keyPressed(e);
			} else {
				Game.userinterface.go(e);
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

	static {
		final Applet applet = new Applet();

		QuestApp.tiles = QuestApp.getImage("/images/tiles32.png");
		QuestApp.scenery = QuestApp.getImage("/images/scenery32.png");
		QuestApp.creatures = QuestApp.getImage("/images/creature32.png");
		QuestApp.items = QuestApp.getImage("/images/items32.png");
		QuestApp.effects = QuestApp.getImage("/images/effects32.png");
		QuestApp.title = QuestApp.getImage("/images/title.png");

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
		mediaTracker.addImage(QuestApp.DEFAULTTEXTURE, 1);
		// mediaTracker.addImage(LairFight.DUNGEONTEXTURE, 1);
		mediaTracker.addImage(IncursionFight.INCURSIONTEXTURE, 1);

		// create grey-filtered background tiles
		final ImageFilter imf = new GreyFilter();
		QuestApp.greytiles = applet.createImage(new FilteredImageSource(QuestApp.tiles.getSource(), imf));

		// Wait for images to load
		try {
			mediaTracker.waitForID(1);
		} catch (final Exception e) {
			System.out.println("Error loading images.");
			e.printStackTrace();
		}

	}

}