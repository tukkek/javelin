package javelin.view.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.action.Action;
import javelin.controller.action.Dig;
import javelin.controller.action.world.WorldAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.ThreadManager;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.old.MessagePanel;
import javelin.controller.terrain.map.Map;
import javelin.controller.walker.Walker;
import javelin.model.condition.Dominated;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.view.StatusPanel;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import tyrant.mikera.tyrant.GameHandler;
import tyrant.mikera.tyrant.IActionHandler;
import tyrant.mikera.tyrant.LevelMapPanel;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Screen;

/**
 * Screen context during battle.
 * 
 * TODO it has become a hierarchy that behaves how different types of
 * {@link Fight}s should behave. The ideal would be for all this type of
 * controller behavior to move to {@link Fight}. For example: {@link LairScreen}
 * .
 * 
 * TODO the 2.0 interface should absolutely not be redrawn every time, only when
 * an update is needed and even then the redraw should be on a tile-by-tile
 * basis, not the entire screen.
 * 
 * TODO many things this is actually handling should be moved to {@link Fight}
 * controllers instead.
 * 
 * @author alex
 */
public class BattleScreen extends Screen {

	private static final long serialVersionUID = 3907207143852421428L;

	public static List<Combatant> originalredteam;
	public static List<Combatant> originalblueteam;
	static public Combatant lastlooked = null;

	public MapPanel mappanel;
	public MessagePanel messagepanel;
	public StatusPanel statuspanel;
	public GameHandler gameHandler = new GameHandler();

	List<IActionHandler> actionHandlers;
	ArrayList<String> lastMessages = new ArrayList<String>();
	boolean shownLastMessages = false;
	boolean overridefeedback;
	Combatant lastwascomputermove;
	boolean firstvision;
	boolean allvisible;

	public ArrayList<Combatant> fleeing = new ArrayList<Combatant>();
	/**
	 * Allows the human player to use up to .5AP of movement before ending turn.
	 */
	public float spentap = 0;
	public LevelMapPanel levelMap = null;

	static Runnable callback = null;

	public BattleScreen(Image texture) {
		super();
		Javelin.settexture(texture);
	}

	public void addActionHandler(final IActionHandler actionHandler) {
		if (actionHandlers == null) {
			actionHandlers = new LinkedList<IActionHandler>();
		}
		actionHandlers.add(actionHandler);
	}

	public void setGameHandler(final GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}

	public GameHandler getGameHandler() {
		return gameHandler;
	}

	public static BattleScreen active;

	public BattleScreen(boolean addsidebar) {
		BattleScreen.active = this;
		setForeground(Color.white);
		setBackground(Color.black);
		setLayout(new BorderLayout());
		messagepanel = new MessagePanel();
		add(messagepanel, "South");
		mappanel = getmappanel();
		mappanel.init();
		add(mappanel, "Center");
		final Panel cp = new Panel();
		cp.setLayout(new BorderLayout());
		add("East", cp);
		statuspanel = new StatusPanel();
		if (addsidebar) {
			cp.add("Center", statuspanel);
			// if (addminimap()) {
			// levelMap = new LevelMapPanel();
			// }
		}
		setFont(QuestApp.mainfont);
		// Game.enterMap(map, Game.hero().x, Game.hero().y);
		Javelin.app.switchScreen(this);
		BattleScreen.active = this;
		Game.delayblock = false;
		initmap();
		mappanel.repaint();
	}

	protected MapPanel getmappanel() {
		return new BattlePanel(Fight.state);
	}

	/** Initializes this screen instance. */
	protected void initmap() {
		// map.makeAllInvisible();
	}

	/**
	 * this is the main game loop. it catches any exceptions for stability and
	 * lets the game continue <br>
	 * very important that endTurn() gets called after the player moves, this
	 * ensures that the rest of the map stays up to date <br>
	 */
	public void mainLoop() {
		mappanel.setVisible(false);
		javelin.controller.Point t = Javelin.app.context.getherolocation();
		mappanel.setPosition(t.x, t.y);
		Game.redraw();
		mappanel.center(t.x, t.y, true);
		mappanel.setVisible(true);
		while (true) {
			step();
		}
	}

	public void step() {
		try {
			checkEndBattle();
		} catch (EndBattle e) {
			checkblock();
			throw e;
		}
		humanTurn();
	}

	protected void removehero() {
		// Game.hero().remove();
	}

	protected void humanTurn() {
		checkblock();
		Fight.state.checkwhoisnext();
		final Combatant next = Fight.state.next;
		// final Thing h = setHero(next);
		if (Fight.state.redTeam.contains(next) || next.automatic) {
			computerTurn();
			return;
		}
		for (Combatant c : Fight.state.getCombatants()) {
			c.refresh();
		}
		while (true) {
			try {
				updatescreen();
				Game.messagepanel.clear();
				endturn();
				BattlePanel.current = next;
				mappanel.refresh();
				Game.userinterface.waiting = true;
				final KeyEvent updatableUserAction = getUserInput();
				if (MapPanel.overlay != null) {
					MapPanel.overlay.clear();
				}
				if (updatableUserAction == null) {
					callback.run();
					callback = null;
				} else {
					tryTick(convertEventToAction(updatableUserAction),
							updatableUserAction.isShiftDown());
				}
				spendap(next, false);
				break;
			} catch (final RepeatTurn e) {
				updateMessages(lastMessages);
				shownLastMessages = true;
				if (next.automatic) {
					return;// set by Automate
				} else {
					continue;
				}
			}
		}
		checkEndBattle();
		computerTurn();
	}

	/**
	 * Use this to break the input loop.
	 * 
	 * @param r
	 *            This will be run instead of an {@link Action} or
	 *            {@link WorldAction}.
	 * @see Mouse
	 */
	static public void perform(Runnable r) {
		callback = r;
		Game.userinterface.go(null);
	}

	void listen(final Combatant next) {
		if (Fight.state.redTeam.contains(next)) {
			return;
		}
		int listen = next.source.perceive(false);
		for (Combatant c : Fight.state.redTeam) {
			if (listen >= c.movesilently() + (Walker.distance(next, c) - 1)) {
				// map.seeTile(c.location[0], c.location[1]);
				// map.setVisible(c.location[0], c.location[1]);
				mappanel.tiles[c.location[0]][c.location[1]].discovered = true;
			}
		}
	}

	public void checkblock() {
		if (Game.delayblock) {
			Game.delayblock = false;
			Game.getInput();
			messagepanel.clear();
		}
	}

	protected void updatescreen() {
		int x = Fight.state.next.location[0];
		int y = Fight.state.next.location[1];
		centerscreen(x, y);
		view(x, y);
		if (shownLastMessages) {
			shownLastMessages = false;
		} else {
		}
		statuspanel.repaint();
		Game.redraw();
		if (levelMap != null) {
			levelMap.repaint();
		}
	}

	public void centerscreen(int x, int y) {
		centerscreen(x, y, false);
	}

	public void view(int x, int y) {
		if (Javelin.app.fight.period == Javelin.PERIODEVENING
				|| Javelin.app.fight.period == Javelin.PERIODNIGHT) {
			if (firstvision) {
				firstvision = false;
				return;
			}
			initmap();
			// h.calculateVision();
			listen(Fight.state.next);
		} else if (!allvisible) {
			for (javelin.view.mappanel.Tile[] ts : mappanel.tiles) {
				for (javelin.view.mappanel.Tile t : ts) {
					t.discovered = true;
				}
			}
			allvisible = true;
		}
	}

	public void centerscreen(int x, int y, boolean force) {
		if (mappanel.center(x, y, force)) {
			mappanel.viewPosition(x, y);
			return;
		}
	}

	public void checkEndBattle() {
		Javelin.app.fight.checkEndBattle(this);
	}

	public boolean checkforenemies() {
		for (Combatant c : Fight.state.redTeam) {
			if (c.hascondition(Dominated.class) == null) {
				return true;
			}
		}
		return false;
	}

	public void afterend() {
		return;
	}

	private void updateMessages(final ArrayList<String> messageList) {
		for (final String s : messageList) {
			Game.messagepanel.add(s + "\n");
		}
		lastMessages = new ArrayList<String>(messageList);
		messageList.clear();
		Game.messagepanel.getPanel().repaint();
	}

	private void computerTurn() {
		overridefeedback = false;
		spentap = 0;
		while (!Fight.state.blueTeam.isEmpty()) {
			checkblock();
			endturn();
			checkEndBattle();
			Fight.state.checkwhoisnext();
			final Combatant active = Fight.state.next;
			if (Fight.state.blueTeam.contains(active) && !active.automatic) {
				lastwascomputermove = null;
				return;
			}
			if (lastwascomputermove == null
					|| !lastwascomputermove.equals(active)) {
				BattlePanel.current = active;
			}
			lastwascomputermove = active;
			if (overridefeedback) {
				overridefeedback = false;
			} else {
				computerfeedback("Thinking...\n", Delay.NONE);
			}
			if (Javelin.DEBUG) {
				Action.outcome(ThreadManager.think(Fight.state), true);
			} else {
				try {
					Action.outcome(ThreadManager.think(Fight.state), true);
				} catch (final RuntimeException e) {
					Game.message("Fatal error: " + e.getMessage(), Delay.NONE);
					messagepanel.repaint();
					throw e;
				}
			}
		}
	}

	/**
	 * Called after a computer or human move.
	 */
	protected void endturn() {
		if (Javelin.app.fight.friendly) {
			BattleState s = Fight.state;
			int blue = s.blueTeam.size();
			int red = s.redTeam.size();
			cleanwounded(s.blueTeam, s);
			cleanwounded(s.redTeam, s);
			if (s.blueTeam.size() != blue || s.redTeam.size() != red) {
				Fight.state = s;
				Game.redraw();
			}
		}
	}

	static void cleanwounded(ArrayList<Combatant> team, BattleState s) {
		for (Combatant c : (List<Combatant>) team.clone()) {
			if (c.getNumericStatus() <= 2) {
				if (team == s.blueTeam) {
					BattleScreen.active.fleeing.add(c);
				}
				team.remove(c);
				if (s.next == c) {
					s.checkwhoisnext();
				}
				s.addmeld(c.location[0], c.location[1]);
				Game.message(
						c + " is removed from the battlefield!\nPress ENTER to continue...",
						Delay.NONE);
				while (Game.getInput().getKeyChar() != '\n') {
					// wait for enter
				}
				Game.messagepanel.clear();
			}
		}
	}

	public void computerfeedback(String s, Delay delay) {
		messagepanel.clear();
		if (lastwascomputermove != null) {
			updatescreen();
		}
		Game.message(s, delay);
	}

	public void updatescreen(final ChanceNode state, boolean enableoverrun) {
		final BattleState s = (BattleState) state.n;
		Fight.state = s;
		if (lastwascomputermove == null) {
			Game.redraw();
		}
		Delay delay = state.delay;
		if (enableoverrun && delay == Delay.WAIT && s.redTeam.contains(s.next)
				|| s.next.automatic) {
			delay = Delay.NONE;
			overridefeedback = true;
		}
		computerfeedback(state.action, delay);
	}

	/**
	 * @param thing
	 *            See {@link #performAction(Thing, Action, boolean)}.
	 * @param action
	 *            See {@link #performAction(Thing, Action, boolean)}.
	 * @param isShiftDown
	 *            See {@link #performAction(Thing, Action, boolean)}.
	 */
	public void tryTick(final Action action, final boolean isShiftDown) {
		if (action == null) {
			System.out.println("Null action");
			return;
		}
		performAction(action, isShiftDown);
	}

	public Action convertEventToAction(final KeyEvent keyEvent) {
		if (rejectEvent(keyEvent)) {
			throw new RepeatTurn();
		}
		return gameHandler.actionFor(keyEvent);
	}

	/**
	 * @return User-input.
	 */
	public KeyEvent getUserInput() {
		Game.instance().clearMessageList();
		return Game.getInput();
	}

	protected boolean addminimap() {
		return true;
	}

	/**
	 * @param thing
	 *            Visual representation of current unit.
	 * @param action
	 *            What is being performed.
	 * @param isShiftDown
	 *            Ignored.
	 */
	public void performAction(final Action action, final boolean isShiftDown) {
		if (actionHandlers != null) {
			for (final IActionHandler iActionHandler : actionHandlers) {
				if (iActionHandler.handleAction(action, isShiftDown)) {
					return;
				}
			}
		}
		Combatant combatant = Fight.state.next;
		try {
			if (combatant.burrowed && !action.allowwhileburrowed) {
				Dig.refuse();
			}
			if (!action.perform(combatant)) {
				throw new RepeatTurn();
			}
		} catch (EndBattle e) {
			throw e;
		} catch (Exception e) {
			// TODO throw on debug?
			if (!(e instanceof RepeatTurn)) {
				e.printStackTrace();
			}
			throw new RepeatTurn();
		}
	}

	/**
	 * Normalize {@link #spentap} with the canonical {@link Combatant} instance.
	 * 
	 * @param force
	 *            If <code>false</code> will check if an action has been
	 *            performed first by comparing actual {@link Combatant#ap}.
	 */
	public void spendap(Combatant combatant, boolean force) {
		for (Combatant c : Fight.state.getCombatants()) {
			if (c.id == combatant.id && (c.ap != combatant.ap || force)) {
				c.ap += spentap;
				break;
			}
		}
		spentap = 0;
	}

	/*
	 * BUG Fix for
	 * http://sourceforge.net/tracker/index.php?func=detail&aid=1088187
	 * &group_id=16696&atid=116696 Ignore Alt keypresses, we may need to add
	 * more of these for other platforms.
	 */
	protected boolean rejectEvent(final KeyEvent keyEvent) {
		return (keyEvent.getModifiers() | InputEvent.ALT_DOWN_MASK) > 0
				&& keyEvent.getKeyCode() == 18;
	}

	// get location, initially place crosshairs at start
	public Point getTargetLocation(Point start) {
		if (start == null) {
			start = JavelinApp.context.getherolocation();
		}
		setCursor(start.x, start.y);
		mappanel.viewPosition(start.x, start.y);
		// initial look
		doLookPoint(getcursor());
		// get interesting stuff to see
		// Note that the hero is incidentally also seen
		// So there should be no worries of an empty list
		// final List stuff = map.findStuff(Game.hero(), BattleMap.FILTER_ITEM
		// + BattleMap.FILTER_MONSTER);
		// int stuffIndex = 0;

		// repaint the status panel
		statuspanel.repaint();
		// TODO : get 'x' and 'l' working
		try {
			while (true) {
				final KeyEvent e = Game.getInput();
				if (e == null) {
					continue;
				}
				int dx = 0;
				int dy = 0;
				switch (BattleScreen.convertkey(e)) {
				case '8':
					dx = 0;
					dy = -1;
					break;
				case '2':
					dx = 0;
					dy = 1;
					break;
				case '4':
					dx = -1;
					dy = 0;
					break;
				case '6':
					dx = 1;
					dy = 0;
					break;
				case '7':
					dx = -1;
					dy = -1;
					break;
				case '9':
					dx = 1;
					dy = -1;
					break;
				case '1':
					dx = -1;
					dy = 1;
					break;
				case '3':
					dx = 1;
					dy = 1;
					break;
				case 'v':
					Point cursor = getcursor();
					for (Combatant c : Fight.state.getCombatants()) {
						if (c.location[0] == cursor.x
								&& c.location[1] == cursor.y) {
							new StatisticsScreen(c);
							break;
						}
					}
					break;
				case 'q':
					clearCursor();
					return null;
				default:
					Point getcursor = getcursor();
					clearCursor();
					return getcursor;
				}
				Point cursor = getcursor();
				int x = BattleScreen.checkbounds(cursor.x + dx,
						Fight.state.map.length);
				int y = BattleScreen.checkbounds(cursor.y + dy,
						Fight.state.map[0].length);
				setCursor(x, y);
				mappanel.viewPosition(x, y);
				doLookPoint(getcursor());
			}
		} finally {
			lastlooked = null;
		}
	}

	private void clearCursor() {
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
	}

	private Point getcursor() {
		TargetOverlay cursor = (TargetOverlay) MapPanel.overlay;
		return new Point(cursor.x, cursor.y);
	}

	private void setCursor(int x, int y) {
		clearCursor();
		MapPanel.overlay = new TargetOverlay(x, y);
		mappanel.refresh();
	}

	static private char convertkey(final KeyEvent e) {
		// handle key conversions
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			return '8';
		case KeyEvent.VK_DOWN:
			return '2';
		case KeyEvent.VK_LEFT:
			return '4';
		case KeyEvent.VK_RIGHT:
			return '6';
		case KeyEvent.VK_HOME:
			return '7';
		case KeyEvent.VK_END:
			return '1';
		case KeyEvent.VK_PAGE_UP:
			return '9';
		case KeyEvent.VK_PAGE_DOWN:
			return '3';
		case KeyEvent.VK_ESCAPE:
			return 'q';
		default:
			return Character.toLowerCase(e.getKeyChar());
		}
	}

	static private int checkbounds(int i, int upperbound) {
		if (i < 0) {
			return 0;
		}
		return i < upperbound ? i : upperbound - 1;
	}

	public static char getKey(final KeyEvent e) {
		// handle key conversions
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP) {
			return '8';
		}
		if (keyCode == KeyEvent.VK_DOWN) {
			return '2';
		}
		if (keyCode == KeyEvent.VK_LEFT) {
			return '4';
		}
		if (keyCode == KeyEvent.VK_RIGHT) {
			return '6';
		}
		if (keyCode == KeyEvent.VK_HOME) {
			return '7';
		}
		if (keyCode == KeyEvent.VK_END) {
			return '1';
		}
		if (keyCode == KeyEvent.VK_PAGE_UP) {
			return '9';
		}
		if (keyCode == KeyEvent.VK_PAGE_DOWN) {
			return '3';
		}
		if (keyCode == KeyEvent.VK_ESCAPE) {
			return 'Q';
		}
		if (keyCode == KeyEvent.VK_ENTER) {
			return 'Q';
		}
		if (keyCode == KeyEvent.VK_F1) {
			return '?';
		}
		if (keyCode == KeyEvent.VK_F2) {
			return '(';
		}
		if (keyCode == KeyEvent.VK_F3) {
			return ')';
		}
		if (keyCode == KeyEvent.VK_F4) {
			return '*';
		}
		if (keyCode == KeyEvent.VK_F5) {
			return ':';
		}
		if (keyCode == KeyEvent.VK_TAB) {
			return '\t';
		}
		return Character.toLowerCase(e.getKeyChar());
	}

	public void doLook() {
		doLookPoint(getTargetLocation(null));
	}

	private void doLookPoint(final Point p) {
		if (p == null) {
			return;
		}
		if (!mappanel.tiles[p.x][p.y].discovered) {
			lookmessage("Can't see");
			return;
		}
		lookmessage("");
		lastlooked = null;
		final BattleState state = Fight.state;
		final Combatant combatant = Fight.state.getCombatant(p.x, p.y);
		if (combatant != null) {
			lookmessage(describestatus(combatant, state));
			lastlooked = combatant;
		} else if (state.map[p.x][p.y].flooded) {
			lookmessage("Flooded");
		} else if (Javelin.app.fight.map.map[p.x][p.y].blocked) {
			lookmessage("Blocked");
		}
		BattleScreen.active.statuspanel.repaint();
	}

	static public String describestatus(final Combatant combatant,
			final BattleState state) {
		final ArrayList<String> statuslist = combatant.liststatus(state);
		if (statuslist.isEmpty()) {
			return combatant.toString();
		}
		String description = combatant + " (";
		for (final String status : statuslist) {
			description += status + ", ";
		}
		return description.substring(0, description.length() - 2) + ")\n";
	}

	private void lookmessage(final String string) {
		messagepanel.clear();
		Game.message(
				"Examine: move the cursor over another combatant, press v to view character sheet.\n\n"
						+ string,
				Delay.NONE);
		Game.redraw();
	}

	/**
	 * @param mappanel
	 *            The mappanel to set.
	 */
	public void setMappanel(final MapPanel mappanel) {
		this.mappanel = mappanel;
	}

	public LevelMapPanel getLevelMap() {
		return levelMap;
	}

	public void setposition() {
		// Point hero = JavelinApp.context.getherolocation();

		mappanel.setPosition(Fight.state.next.location[0],
				Fight.state.next.location[1]);
	}

	public boolean drawbackground() {
		return true;
	}

	public Square getsquare(int x, int y) {
		return Javelin.app.fight.map.map[x][y];
	}

	/**
	 * TODO with the {@link MapPanel} hierarchy now this is probably not needed
	 * anymore
	 */
	public Image gettile(int x, int y) {
		Map m = Javelin.app.fight.map;
		Square s = m.map[x][y];
		if (s.blocked) {
			return m.getblockedtile(x, y);
		}
		return m.floor;
	}

}