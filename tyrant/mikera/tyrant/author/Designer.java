package tyrant.mikera.tyrant.author;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.UIManager;

import javelin.controller.action.Action;
import javelin.controller.action.ActionDescription;
import javelin.controller.action.ActionMapping;
import javelin.model.BattleMap;
import javelin.view.MapPanel;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.BaseObject;
import tyrant.mikera.engine.Lib;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.GameHandler;
import tyrant.mikera.tyrant.Hero;
import tyrant.mikera.tyrant.IActionHandler;
import tyrant.mikera.tyrant.ImageGadget;
import tyrant.mikera.tyrant.InventoryScreen;
import tyrant.mikera.tyrant.Item;
import tyrant.mikera.tyrant.LevelMap;
import tyrant.mikera.tyrant.LevelMapPanel;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.TPanel;
import tyrant.mikera.tyrant.Tile;

public class Designer {
	public interface IMapUpdater {
		void set(BattleMap map, int x, int y, Thing toAdd);
	}

	private class StartBlock implements Runnable {
		@Override
		public void run() {
			Game.instance().setHero(
					Hero.createHero("Designer", "human", "fighter"));
			Game.instance().setDesignerMode(true);
			questApp.setupScreen();
			gameScreen = questApp.getScreen();
			mapPanel = gameScreen.getMappanel();
			loadImages();
			levelMapPanel = gameScreen.getLevelMap();

			questApp.keyhandler = new KeyAdapter() {
				@Override
				public void keyPressed(final KeyEvent k) {
					actionHandler
							.handleAction(null,
									gameScreen.convertEventToAction(k),
									k.isShiftDown());
				}
			};

			mapPanel.addMouseListener(new MyMouseListener());
			mapPanel.addMouseMotionListener(new MyMouseMotionListener());
			gameScreen.addActionHandler(actionHandler);
			gameScreen.setGameHandler(new GameHandler() {
				@Override
				public void calculateVision(final Thing thing) {
					if (Game.instance().lineOfSightDisabled()) {
						return;
					}
					thing.calculateVision();
				}
			});
			gameScreen.getGameHandler().setActionMapping(createActions());
			map = createEmptyMap();
			createStatusBar();
			createTilePalette();
			createThingPalette();
			frame.add(statusBar, BorderLayout.SOUTH);
			statusBar.setVisible(true);
			tilePalette.setVisible(true);
			thingPalette.setVisible(true);
			frame.invalidate();
			frame.validate();
			map.setAllVisible();

			// view the map
			levelMapPanel.setMap(map);
			mapPanel.render();
			mapPanel.viewPosition(map, map.getWidth() / 2, map.getHeight() / 2);
		}
	}

	private class MyActionHandler implements IActionHandler {
		@Override
		public boolean handleAction(final Thing actor, final Action action,
				final boolean isShiftDown) {
			lastAction = action;
			if (action.isMovementKey()) {
				final Point direction = GameHandler
						.convertActionToDirection(action);
				mapPanel.scroll(direction.x, direction.y);
			} else if (action == POINT) {
				updateMode(POINT);
			}
			if (action == LINE) {
				updateMode(LINE);
			} else if (action == RECTANGLE) {
				updateMode(RECTANGLE);
			} else if (action == CIRCLE) {
				updateMode(CIRCLE);
			} else if (action == Action.SAVE_GAME) {
				saveMap();
			} else if (action == Action.LOAD_GAME) {
				loadMap();
			} else if (action == SWITCH_TO_TILE_PALETTE) {
				tilePalette.setVisible(true);
				tilesScreen.getInventoryPanel().requestFocus();
			} else if (action == ADD_THING) {
				updateMode("Add things");
				mode = POINT;
				thingPalette.setVisible(true);
				thingsScreen.getInventoryPanel().requestFocus();
				currentMapAdder = addThing;
			} else if (action == ADD_TILE) {
				updateMode("Add tiles");
				mode = POINT;
				tilePalette.setVisible(true);
				tilesScreen.getInventoryPanel().requestFocus();
				currentMapAdder = addTile;
			} else if (action == UNDO) {
				Game.messageTyrant("Undo");
				if (currentMapAdder == addThing) {
					map.setObjects(copy(oldThings));
				} else {
					map.setTiles(copy(oldTiles));
				}
				makeAllVisible();
			} else if (action == ERASE) {
				updateMode(ERASE);
			} else if (action == DELETE) {
				updateMode(DELETE);
				currentMapAdder = deleteThing;
			} else if (action == FILL) {
				Game.messageTyrant("Fill mode, currently only tiles can used for filling");
				updateMode(FILL);
			} else if (action == Action.EXIT) {
				exit();
			} else if (action == RESIZE) {
				textLabel.setText("Enter new size (current size is "
						+ map.getWidth() + "x" + map.getHeight());
				textLabel.getParent().invalidate();
				textLabel.getParent().validate();
				statusBar.setVisible(true);
				((CardLayout) statusBar.getLayout()).show(statusBar, "input");
				textField.requestFocus();
				statusBar.getParent().invalidate();
				statusBar.getParent().validate();
			} else if (action == SELECT) {
				if (thingEditor == null) {
					openThingEditor(null);
				}
				updateMode(SELECT);
			}

			mapPanel.render();
			mapPanel.repaint();
			return true;
		}
	}

	private class MyMouseMotionListener extends MouseMotionAdapter {
		private int scrollCount;

		@Override
		public void mouseDragged(final MouseEvent e) {
			final Point mapPoint = mapPanel.convertUICoordinatesToMap(e);
			inDrag = true;
			final Rectangle place = questApp.getScreen().getMappanel()
					.getBounds();
			if (!place.contains(e.getPoint())) {
				if (scrollCount++ < 5) {
					return;
				}
				scrollCount = 0;
				int scrollX = 0;
				int scrollY = 0;
				System.out.println("outside");
				// NW N NE
				// W . E
				// SW S SE
				if (e.getY() < 0) {
					// North half
					if (e.getX() < 0) {
						// System.out.println("NW");
						scrollX = -1;
						scrollY = -1;
					} else if (e.getX() > place.getWidth()) {
						// System.out.println("NE");
						scrollX = 1;
						scrollY = -1;
					} else {
						// System.out.println("N");
						scrollY = -1;
					}
				} else if (e.getY() > place.getHeight()) {
					// South half
					if (e.getX() < 0) {
						// System.out.println("SW");
						scrollX = -1;
						scrollY = 1;
					} else if (e.getX() > place.getWidth()) {
						// System.out.println("SE");
						scrollX = 1;
						scrollY = 1;
					} else {
						// System.out.println("S");
						scrollY = 1;
					}
				} else if (e.getX() < 0) {
					// System.out.println("W");
					scrollX = -1;
				} else if (e.getX() > place.getWidth()) {
					// System.out.println("E");
					scrollX = 1;
				}
				mapPanel.scroll(scrollX, scrollY);
			}
			if (mode == POINT) {
				doPoint(mapPoint);
			} else if (mode == LINE) {
				doLine(mapPoint);
			} else if (mode == RECTANGLE) {
				doRectangle(mapPoint, e.isShiftDown());
			} else if (mode == CIRCLE) {
				doCircleMidpoint(mapPoint);
			} else if (mode == ERASE) {
				// TODO
			}
			mapPanel.render();
			mapPanel.repaint();
		}
	}

	private class MyMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(final MouseEvent e) {
			inDrag = false;
			final Point mapPoint = mapPanel.convertUICoordinatesToMap(e);
			currentPoint = mapPoint;
			if (mode == SELECT) {
				openThingEditor(mapPoint);
				return;
			}
			final int[] tiles = map.getTiles();
			oldTiles = new int[tiles.length];
			final Thing[] objects = map.getObjects();
			oldThings = new Thing[objects.length];
			// mapPanel.viewPosition(hero.x, hero.y);
			System.arraycopy(tiles, 0, oldTiles, 0, tiles.length);
			for (int i = 0; i < objects.length; i++) {
				final Thing thing = objects[i];
				if (thing == null) {
					continue;
				}
				if (thing == Game.hero()) {
					oldThings[i] = Game.hero();
					continue;
				}
				oldThings[i] = thing.cloneType();
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				doPoint(mapPoint);
			}

			mapPanel.render();
			mapPanel.repaint();
		}
	}

	public static final Action POINT = new Action("Point");
	public static final Action LINE = new Action("Line");
	public static final Action CIRCLE = new Action("Circle");
	public static final Action RECTANGLE = new Action("Rectangle");
	public static final ActionDescription SWITCH_TO_TILE_PALETTE = new Action(
			"switch to tiles");
	public static final ActionDescription SWITCH_TO_THING_PALETTE = new Action(
			"switch to things");
	public static final Action ADD_THING = new Action("Add thing");
	public static final Action ADD_TILE = new Action("Add tile");
	public static final Action UNDO = new Action("undo");
	public static final ActionDescription REDO = new Action("redo");
	public static final ActionDescription RESIZE = new Action("Resize");
	public static final ActionDescription ERASE = new Action("erase");
	public static final ActionDescription DELETE = new Action("delete");
	public static final Action FILL = new Action("Fill");
	public static final Action SELECT = new Action("Select");

	protected ActionDescription mode = POINT;

	protected MapPanel mapPanel;

	// is designer run in embedded mode?
	private boolean embedded = false;

	private Point currentPoint;
	private boolean inDrag;
	private int[] oldTiles;
	private Thing[] oldThings;
	protected BattleMap map;
	private InventoryScreen tilesScreen;
	private InventoryScreen thingsScreen;
	private Thing currentThing = (Thing) Lib.get("cave wall");
	private Frame tilePalette;
	private Frame thingPalette;
	private final boolean switchPalette = false;
	private Frame frame;
	private IMapUpdater addTile;
	private IMapUpdater addThing;
	private IMapUpdater deleteThing;
	private IMapUpdater currentMapAdder;
	private final IActionHandler actionHandler = new MyActionHandler();
	private TPanel statusBar;
	private ActionListener textListener;
	private TextField textField;
	private ImageGadget textLabel;
	private ActionDescription lastAction;
	private ThingEditor thingEditor;
	private ImageGadget statusGadget;
	private TPanel statusInfo;
	private Container statusInput;
	private QuestApp questApp;
	private LevelMapPanel levelMapPanel;
	private BattleScreen gameScreen;

	private static java.util.Map isThings;
	private static Image overlayImage;
	private static Image plusImage;

	public static void main(final String[] args) {
		try {
			UIManager
					.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
		} catch (final Exception e) {
			// Likely PlasticXP is not in the class path; ignore.
		}

		final Designer des = new Designer();
		des.go();

		if (args.length >= 1 && args[0].equals("embedded")) {
			des.embedded = true;
		}

		if (des.embedded) {
			try {
				synchronized (des) {
					des.wait();
				}
			} catch (final InterruptedException e) {
				// do nothing
			}
		}
		Game.warn("Designer main thread completed.");
	}

	private void go() {
		createMapUpdaters();
		Game.loadVersionNumber();
		createFrame();
		frame.requestFocus();
	}

	private void loadImages() {
		final MediaTracker mediatracker = new MediaTracker(mapPanel);
		overlayImage = QuestApp.getImage("/images/isOverlay.gif");
		plusImage = QuestApp.getImage("/images/plus.gif");
		mediatracker.addImage(overlayImage, 12);
		mediatracker.addImage(plusImage, 12);
		try {
			mediatracker.waitForID(12);
		} catch (final InterruptedException ioe) {
			System.out.println("Error reading image data");
			ioe.printStackTrace();
		}
	}

	private boolean shouldNotAdd(final Thing toAdd, final int x, final int y) {
		if (!inDrag) {
			return false;
		}
		Thing current = map.getObjects(x, y);
		while (current != null) {
			if (current.name().equals(toAdd.name())) {
				return true;
			}
			current = current.next;
		}
		return false;
		// if(mode == ERASE) {
		// doErase(map, x, y);
		// return false;
		// }
	}

	private void createMapUpdaters() {
		addTile = new IMapUpdater() {
			@Override
			public void set(final BattleMap map, final int x, final int y,
					final Thing toAdd) {
				if (shouldNotAdd(toAdd, x, y)) {
					return;
				}
				map.setTile(x, y, toAdd.getStat("TileValue"));
			}
		};

		addThing = new IMapUpdater() {
			@Override
			public void set(final BattleMap map, final int x, final int y,
					final Thing toAdd) {
				if (shouldNotAdd(toAdd, x, y)) {
					return;
				}
				map.addThing(toAdd.cloneType(), x, y);
			}
		};

		deleteThing = new IMapUpdater() {
			@Override
			public void set(final BattleMap map, final int x, final int y,
					final Thing toAdd) {
				final Thing[] ts = map.getThings(x, y);
				for (final Thing element : ts) {
					element.remove();
				}
			}
		};

		currentMapAdder = addTile;
	}

	protected void doErase(final BattleMap map2, final int x, final int y) {
		System.out.println("Doing erase!");
		map.setTile(x, y, Tile.NOTHING);
		map.getObjects()[x * map2.getWidth() + y] = null;
	}

	private void createFrame() {
		questApp = new QuestApp();
		QuestApp.isapplet = false;
		Game.setDebug(true);
		final Designer designer = this;
		frame = new Frame("Tyrant - Designer - v" + Game.VERSION);
		frame.setBackground(Color.black);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				designer.exit();
			}
		});
		frame.setLayout(new BorderLayout());
		frame.add(questApp, BorderLayout.CENTER);
		frame.setSize(questApp.getPreferredSize().width,
				questApp.getPreferredSize().height);
		frame.addKeyListener(questApp.keyadapter);
		frame.setMenuBar(createMenuBar());
		frame.setVisible(true);
		questApp.init(new StartBlock());
	}

	private void exit() {
		if (embedded) {
			synchronized (this) {
				notify();
			}
			Game.warn("Disposing designer windows...");
			frame.dispose();
			thingPalette.dispose();
			tilePalette.dispose();
		} else {
			Game.warn("Exiting via System.exit(0)...");
			System.exit(0);
		}
	}

	private void createStatusBar() {
		statusBar = new TPanel(null);
		statusBar.setLayout(new CardLayout());

		statusInfo = new TPanel(null);
		statusInfo.setName("info");
		statusBar.add(statusInfo, "info");

		statusInfo.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		gridBagConstraints.weightx = 1.0;
		textLabel = ImageGadget.noImage(null);
		textLabel.setBackgroundImage(QuestApp.paneltexture);
		statusInfo.add(textLabel, gridBagConstraints);

		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 0;
		gridBagConstraints.insets = new Insets(2, 2, 2, 4);
		statusGadget = ImageGadget.noImage(null);
		statusGadget.setBackgroundImage(QuestApp.paneltexture);
		statusInfo.add(statusGadget, gridBagConstraints);

		// //////////////////
		// Input
		// //////////////////
		statusInput = new TPanel(null);
		statusInput.setName("input");
		statusBar.add(statusInput, "input");
		statusInput.setLayout(new GridBagLayout());
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		gridBagConstraints.weightx = 1.0;
		textLabel = ImageGadget.noImage(null);
		textLabel.setBackgroundImage(QuestApp.paneltexture);
		statusInput.add(textLabel, gridBagConstraints);

		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = .5;
		gridBagConstraints.insets = new Insets(2, 2, 2, 2);
		textField = new TextField(30);
		statusInput.add(textField, gridBagConstraints);

		textListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				doTextPerformed();
			}
		};
		textField.addActionListener(textListener);

		((CardLayout) statusBar.getLayout()).show(statusBar, "info");
	}

	protected void doTextPerformed() {
		if (lastAction == RESIZE) {
			final String line = textField.getText();
			if (line == null || line.length() == 0) {
				return;
			}
			final String[] splits = line.split("x");
			final int width = Integer.parseInt(splits[0].trim());
			final int height = Integer.parseInt(splits[1].trim());
			doResize(width, height);
			((CardLayout) statusBar.getLayout()).show(statusBar, "info");
			statusBar.getParent().invalidate();
			statusBar.getParent().validate();
		}
	}

	private void doResize(final int width, final int height) {
		// this is a bit lame at the moment in terms of copying data
		final BattleMap newMap = new BattleMap(width, height);
		newMap.copyArea(0, 0, map, 0, 0, map.getWidth(), map.getHeight());
		mapPanel.map = newMap;
		gameScreen.map = newMap;
		map = newMap;
		// map.addThing(hero, width / 2, height / 2);
		LevelMap.reveal(newMap);
		levelMapPanel.getParent().invalidate();
		levelMapPanel.getParent().validate();
		makeAllVisible();
		mapPanel.setPosition(newMap, width / 2, height / 2);
		mapPanel.render();
		mapPanel.repaint();
		oldTiles = null;
		oldThings = null;
	}

	private MenuBar createMenuBar() {
		final MenuBar menuBar = new MenuBar();
		menuBar.add(createMenu("File", new String[] { "Open Map", "Save Map",
				"Exit" }, new ActionDescription[] { Action.LOAD_GAME, Action.SAVE_GAME,
				Action.EXIT }, new boolean[] { true, true }));
		menuBar.add(createMenu("Edit",
				new String[] { "Undo", "Redo", "Resize" }, new ActionDescription[] { UNDO,
						REDO, RESIZE }, new boolean[] { false, true }));
		menuBar.add(createMenu("Mode", new String[] { "Paint", "Erase", "Fill",
				"Select", "Delete" }, new ActionDescription[] { POINT, ERASE, FILL,
				SELECT, DELETE }, new boolean[] { false, false, true }));
		menuBar.add(createMenu("Window", new String[] { "Show Thing editor" },
				new ActionDescription[] { SELECT }, new boolean[] { false }));

		return menuBar;
	}

	private Menu createMenu(final String menuName, final String[] names,
			final ActionDescription[] actions, final boolean[] separatorsBetweenItems) {
		final java.util.Map menuToAction = new HashMap();
		associateNamesAndActions(menuToAction, names, actions);
		final Menu menu = new Menu(menuName);
		for (int i = 0; i < names.length; i++) {
			final String name = names[i];
			menu.add(name);
			if (i < separatorsBetweenItems.length) {
				if (separatorsBetweenItems[i]) {
					menu.addSeparator();
				}
			}
		}
		menu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Action action = (Action) menuToAction.get(e
						.getActionCommand());
				if (action == null) {
					return;
				}
				actionHandler.handleAction(null, action, false);
			}
		});
		return menu;
	}

	private void associateNamesAndActions(final java.util.Map map,
			final String[] names, final ActionDescription[] actions) {
		for (int i = 0; i < actions.length; i++) {
			map.put(names[i], actions[i]);
		}
	}

	private void createTilePalette() {
		tilePalette = new Frame("Tiles");
		tilePalette.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				tilePalette.setVisible(false);
			}
			// public void windowActivated(WindowEvent e) {
			// tilesScreen.getInventoryPanel().requestFocus();
			// }
		});
		tilePalette.setLayout(new BorderLayout());
		tilesScreen = new InventoryScreen(false, true);
		tilePalette.add(tilesScreen, BorderLayout.CENTER);
		tilesScreen.setUp("Tiles", null, tilesAsThings());
		tilesScreen.getInventoryPanel().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				currentThing = (Thing) e.getItem();
				currentMapAdder = addTile;
				System.out.println("current thing is " + currentThing);
				if (switchPalette) {
					frame.requestFocus();
				}
			}
		});
		final Rectangle parent = frame.getBounds();
		tilePalette.setBounds(parent.x + parent.width, parent.y, 300,
				parent.height);
	}

	private void createThingPalette() {
		thingPalette = new Frame("Things");
		thingPalette.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				thingPalette.setVisible(false);
			}
			// public void windowActivated(WindowEvent e) {
			// thingsScreen.getInventoryPanel().requestFocus();
			// }
		});
		thingPalette.setLayout(new BorderLayout());
		thingsScreen = new InventoryScreen(false, true);
		thingPalette.add(thingsScreen, BorderLayout.CENTER);
		thingsScreen.setUp("Things", null, getThings());
		thingsScreen.getInventoryPanel().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				currentThing = (Thing) e.getItem();
				currentMapAdder = addThing;
				System.out.println("current thing is " + currentThing);
				if (switchPalette) {
					frame.requestFocus();
				}
			}
		});
		final Rectangle parent = tilePalette.getBounds();
		thingPalette.setBounds(parent.x + parent.width, parent.y, 300,
				parent.height);
	}

	private Thing[] getThings() {
		final List all = Lib.instance().getAll();
		final List toShow = new ArrayList();
		addIsThings(toShow);
		for (final Iterator iter = all.iterator(); iter.hasNext();) {
			final Thing thing = (Thing) iter.next();
			Item.fullIdentify(thing);
			if (thing.name().startsWith("base ")) {
				continue;
			}
			toShow.add(thing);
		}
		return (Thing[]) toShow.toArray(new Thing[toShow.size()]);
	}

	private void addIsThings(final List toShow) {
		toShow.add(getFlagged("IsFood"));
		toShow.add(getFlagged("IsBeast"));
		toShow.add(getFlagged("IsHerb"));
		toShow.add(getFlagged("IsScenery"));
		toShow.add(getFlagged("IsPotion"));
		toShow.add(getFlagged("IsFruitTree"));

		toShow.add(getFlagged("IsHostile"));
		toShow.add(getFlagged("IsWell"));
		toShow.add(getFlagged("IsBandit"));
		toShow.add(getFlagged("IsDoor"));
		toShow.add(getFlagged("IsMonster"));
		toShow.add(getFlagged("IsGoblinoid"));
		toShow.add(getFlagged("IsUndead"));
		toShow.add(getFlagged("IsDemonic"));
		toShow.add(getFlagged("IsUndead"));
		toShow.add(getFlagged("IsGravestone"));
		toShow.add(getFlagged("IsEquipment"));
		toShow.add(getFlagged("IsArmour"));
		toShow.add(getFlagged("IsWeapon"));
	}

	public static Thing getFlagged(final String type) {
		if (isThings == null) {
			createAllIs();
		}
		return (Thing) isThings.get(type);
	}

	private static void createAllIs() {
		isThings = new HashMap();
		for (final Iterator iter = Lib.instance().getAllPropertyNames()
				.iterator(); iter.hasNext();) {
			final String ifAttribute = (String) iter.next();
			if (!ifAttribute.startsWith("Is")) {
				continue;
			}
			final Thing parent = new Thing();
			final Thing isThing = new Thing(parent);

			Thing prototype = null;

			// might fail if no things have the property set
			try {
				prototype = Lib.createType(ifAttribute, 1);
			} catch (final Throwable t) {
				// nothing
			}
			if (prototype == null) {
				prototype = Lib.create("strange rock");
			}
			isThing.set("Name", ifAttribute + ThingMaker.FLAG_ENDING);
			// set these in the parent so they aren't saved
			parent.set("AuthorIsTyped", true);
			parent.set("LevelMin", 1);
			parent.set("ImageSource", prototype.getString("ImageSource"));
			parent.set("Image", prototype.getStat("Image"));
			Lib.add(isThing);
			pushLocalUp(isThing);
			isThings.put(ifAttribute, isThing);
		}
	}

	private static void pushLocalUp(final Thing isThing) {
		final BaseObject parent = isThing.getInherited();
		if (parent == null) {
			return;
		}
		for (final Iterator iter = isThing.getLocal().entrySet().iterator(); iter
				.hasNext();) {
			final java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
			final String key = (String) entry.getKey();
			final Object value = entry.getValue();
			if (key.equals("Name")) {
				continue;
			}
			parent.set(key, value);
			iter.remove();
		}
	}

	private Thing[] tilesAsThings() {
		return (Thing[]) Lib.instance().getTiles()
				.toArray(new Thing[Lib.instance().getTiles().size()]);
	}

	protected ActionMapping createActions() {
		final ActionMapping actionMapping = new ActionMapping();
		actionMapping.addDefaultMovementActions();
		actionMapping.map('l', LINE);
		actionMapping.map('r', RECTANGLE);
		actionMapping.map('c', CIRCLE);
		actionMapping.map('-', Action.SAVE_GAME);
		actionMapping.map('+', Action.LOAD_GAME);
		actionMapping.map('=', Action.LOAD_GAME);
		actionMapping.map('t', ADD_TILE);
		actionMapping.map('h', ADD_THING);
		actionMapping.map('z', UNDO);
		// actionMapping.map('e', ERASE);
		actionMapping.map('f', FILL);
		actionMapping.map('p', POINT);
		actionMapping.map('s', SELECT);
		return actionMapping;
	}

	private void doRectangle(final Point mapPoint, final boolean isShiftDown) {
		map.setTiles(copy(oldTiles));
		map.setObjects(copy(oldThings));
		final int westX = Math.min(mapPoint.x, currentPoint.x);
		int eastX = Math.max(mapPoint.x, currentPoint.x);
		final int northY = Math.min(mapPoint.y, currentPoint.y);
		int southY = Math.max(mapPoint.y, currentPoint.y);
		if (isShiftDown) {
			final int square = Math.max(eastX - westX, southY - northY);
			eastX = westX + square;
			southY = northY + square;
		}
		fastDrawLine(westX, northY, eastX, northY);
		fastDrawLine(eastX, northY, eastX, southY);
		fastDrawLine(eastX, southY, westX, southY);
		fastDrawLine(westX, southY, westX, northY);
		makeAllVisible();
	}

	public void doCircleMidpoint(final Point mapPoint) {
		map.setTiles(copy(oldTiles));
		map.setObjects(copy(oldThings));

		final int xCenter = currentPoint.x;
		final int yCenter = currentPoint.y;
		final int xDiff = xCenter - mapPoint.x;
		final int yDiff = yCenter - mapPoint.y;
		final int radius = (int) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		int x = 0;
		int y = radius;
		int p = (5 - radius * 4) / 4;

		circlePoints(xCenter, yCenter, x, y);
		while (x < y) {
			x++;
			if (p < 0) {
				p += 2 * x + 1;
			} else {
				y--;
				p += 2 * (x - y) + 1;
			}
			circlePoints(xCenter, yCenter, x, y);
		}
		makeAllVisible();

	}

	private void doPoint(final Point mapPoint) {
		if (mode == FILL) {
			doFill(mapPoint);
			return;
		}
		currentMapAdder.set(map, mapPoint.x, mapPoint.y, currentThing);
		makeAllVisible();
	}

	private void doFillWith(final List queue, final int tileToChange) {
		if (currentMapAdder == addThing) {
			Game.messageTyrant("Currently you can only fill with tiles.");
			return;
		}
		// check orthogonial directions only
		Point[] orthoginalTiles;
		while (!queue.isEmpty()) {
			final Point inQuestion = (Point) queue.remove(queue.size() - 1);
			orthoginalTiles = orthoginalTiles(inQuestion);
			for (final Point point : orthoginalTiles) {
				if (map.getTile(point.x, point.y) == tileToChange) {
					currentMapAdder.set(map, point.x, point.y, currentThing);
					queue.add(point);
				}
			}
			currentMapAdder.set(map, inQuestion.x, inQuestion.y, currentThing);
		}
	}

	private void doFill(final Point mapPoint) {
		final int tileToChange = map.getTile(mapPoint.x, mapPoint.y);
		final List queue = new ArrayList();
		queue.add(mapPoint);
		doFillWith(queue, tileToChange);
		makeAllVisible();
		mapPanel.render();
		mapPanel.repaint();
	}

	private Point[] orthoginalTiles(final Point mapPoint) {
		final List points = new ArrayList();
		// NESW
		if (mapPoint.y > 0) {
			points.add(new Point(mapPoint.x, mapPoint.y - 1));
		}
		if (mapPoint.x < map.getWidth() - 1) {
			points.add(new Point(mapPoint.x + 1, mapPoint.y));
		}
		if (mapPoint.y < map.getHeight() - 1) {
			points.add(new Point(mapPoint.x, mapPoint.y + 1));
		}
		if (mapPoint.x > 0) {
			points.add(new Point(mapPoint.x - 1, mapPoint.y));
		}
		return (Point[]) points.toArray(new Point[points.size()]);
	}

	private void doLine(final Point mapPoint) {
		map.setTiles(copy(oldTiles));
		map.setObjects(copy(oldThings));
		fastDrawLine(currentPoint.x, currentPoint.y, mapPoint.x, mapPoint.y);
		makeAllVisible();

	}

	private int[] copy(final int[] tiles) {
		final int[] copy = new int[tiles.length];
		System.arraycopy(tiles, 0, copy, 0, tiles.length);
		return copy;
	}

	private Thing[] copy(final Thing[] objects) {
		final Thing[] copy = new Thing[objects.length];
		System.arraycopy(objects, 0, copy, 0, objects.length);
		return copy;
	}

	private void makeAllVisible() {
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				map.setVisible(x, y);
			}
		}
		levelMapPanel.setMap(map);
		LevelMap.reveal(map);
		levelMapPanel.repaint();
	}

	/**
	 * This is a standard Bresenham line drawing algorithm.
	 */
	private void fastDrawLine(int x0, int y0, final int x1, final int y1) {
		int dy = y1 - y0;
		int dx = x1 - x0;
		int stepx, stepy;

		if (dy < 0) {
			dy = -dy;
			stepy = -1;
		} else {
			stepy = 1;
		}
		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}
		dy <<= 1; // dy is now 2*dy
		dx <<= 1; // dx is now 2*dx

		currentMapAdder.set(map, x0, y0, currentThing);
		if (dx > dy) {
			int fraction = dy - (dx >> 1); // same as 2*dy - dx
			while (x0 != x1) {
				if (fraction >= 0) {
					y0 += stepy;
					fraction -= dx; // same as fraction -= 2*dx
				}
				x0 += stepx;
				fraction += dy; // same as fraction -= 2*dy
				currentMapAdder.set(map, x0, y0, currentThing);
			}
		} else {
			int fraction = dx - (dy >> 1);
			while (y0 != y1) {
				if (fraction >= 0) {
					x0 += stepx;
					fraction -= dy;
				}
				y0 += stepy;
				fraction += dx;
				currentMapAdder.set(map, x0, y0, currentThing);
			}
		}

	}

	private BattleMap createEmptyMap() {
		final BattleMap map = new BattleMap(20, 20);
		map.set("Description", "design");
		map.fillArea(0, 0, map.getWidth(), map.getHeight(), Tile.GRASS);
		return map;
	}

	private final void circlePoints(final int cx, final int cy, final int x,
			final int y) {
		if (x == 0) {
			currentMapAdder.set(map, cx, cy + y, currentThing);
			currentMapAdder.set(map, cx, cy - y, currentThing);
			currentMapAdder.set(map, cx + y, cy, currentThing);
			currentMapAdder.set(map, cx - y, cy, currentThing);
		} else if (x == y) {
			currentMapAdder.set(map, cx + x, cy + y, currentThing);
			currentMapAdder.set(map, cx - x, cy + y, currentThing);
			currentMapAdder.set(map, cx + x, cy - y, currentThing);
			currentMapAdder.set(map, cx - x, cy - y, currentThing);
		} else if (x < y) {
			currentMapAdder.set(map, cx + x, cy + y, currentThing);
			currentMapAdder.set(map, cx - x, cy + y, currentThing);
			currentMapAdder.set(map, cx + x, cy - y, currentThing);
			currentMapAdder.set(map, cx - x, cy - y, currentThing);
			currentMapAdder.set(map, cx + y, cy + x, currentThing);
			currentMapAdder.set(map, cx - y, cy + x, currentThing);
			currentMapAdder.set(map, cx + y, cy - x, currentThing);
			currentMapAdder.set(map, cx - y, cy - x, currentThing);
		}
	}

	private void loadMap() {
		final BattleMap loadedMap = new MapMaker().promptAndLoad();
		if (loadedMap != null) {
			reloadMap(loadedMap);
		}
	}

	public void reloadMap(final BattleMap loadedMap) {
		map = loadedMap;
		mapPanel.map = map;
		makeAllVisible();

		mapPanel.getParent().invalidate();
		mapPanel.getParent().validate();

		mapPanel.setPosition(loadedMap, 0, 0);
		mapPanel.render();
		mapPanel.repaint();
		oldTiles = null;
		oldThings = null;
	}

	private void saveMap() {
		String filename = "map.txt";
		final FileDialog fileDialog = new FileDialog(new Frame(), "Save map",
				FileDialog.SAVE);
		fileDialog.setFile(filename);
		fileDialog.setVisible(true);

		if (fileDialog.getFile() == null) {
			return;
		}
		filename = fileDialog.getDirectory() + fileDialog.getFile();

		try {
			final FileOutputStream f = new FileOutputStream(filename);
			final String mapAsText = new MapMaker().store(map);
			f.write(mapAsText.getBytes());
		} catch (final Exception e) {
			e.printStackTrace();
			Game.messageTyrant("Error while saving map, check console");
			return;
		}
		Game.messageTyrant("Map saved - " + filename);
	}

	private void openThingEditor(final Point mapPoint) {
		if (thingEditor == null) {
			thingEditor = new ThingEditor();
			thingEditor.setDesigner(this);
			thingEditor.buildUI();
			thingEditor.getFrame().setLocation(
					frame.getLocation().x + frame.getSize().width,
					frame.getLocation().y);
			thingEditor.getFrame().setVisible(true);
		}
		thingEditor.inspect(mapPoint == null ? null : map.getThings(mapPoint.x,
				mapPoint.y));
		thingEditor.setVisible(true);
	}

	private void updateMode(final String mode) {
		statusGadget.setText("Mode: " + mode);
		statusGadget.getParent().invalidate();
		statusGadget.getParent().validate();
	}

	private void updateMode(final ActionDescription mode) {
		this.mode = mode;
		updateMode(mode.toString());
	}

	public static Image getOverlayImage() {
		return overlayImage;
	}

	public static Image getPlusImage() {
		return plusImage;
	}
}
