package javelin.view.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Panel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.controller.Movement;
import javelin.controller.action.Action;
import javelin.controller.action.PassItem;
import javelin.controller.action.UseItem;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.ThreadManager;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.exception.UnbalancedTeamsException;
import javelin.controller.exception.battle.BattleEvent;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.walker.Walker;
import javelin.model.BattleMap;
import javelin.model.condition.Condition;
import javelin.model.condition.Dominated;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.place.town.Transport;
import javelin.view.MapPanel;
import javelin.view.StatusPanel;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.GameHandler;
import tyrant.mikera.tyrant.IActionHandler;
import tyrant.mikera.tyrant.LevelMapPanel;
import tyrant.mikera.tyrant.MessagePanel;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Screen;
import tyrant.mikera.tyrant.Tile;

/**
 * Screen context during battle.
 * 
 * TODO it has become a hierarchy that behaves how different types of
 * {@link Fight}s should behave. The ideal would be for all this type of
 * controller behavior to move to {@link Fight}. For example: {@link LairScreen}
 * .
 * 
 * @author alex
 */
public class BattleScreen extends Screen {
	public class DescendingLevelComparator implements Comparator<Combatant> {
		@Override
		public int compare(Combatant arg0, Combatant arg1) {
			return new Integer(sumcrandxp(arg0)).compareTo(sumcrandxp(arg1));
		}

		public int sumcrandxp(Combatant arg0) {
			return -Math.round(100 * (arg0.xp.floatValue()
					+ ChallengeRatingCalculator.calculateCr(arg0.source)));
		}
	}

	String[] ERRORQUOTES = new String[] { "A wild error appears!",
			"You were eaten by a grue.", "So again it has come to pass..." };
	private static final long serialVersionUID = 3907207143852421428L;

	public static List<Combatant> originalredteam;
	public static List<Combatant> originalblueteam;

	public MapPanel mappanel;
	public MessagePanel messagepanel;
	public StatusPanel statuspanel;
	public GameHandler gameHandler = new GameHandler();
	public BattleMap map;

	List<IActionHandler> actionHandlers;
	ArrayList<String> lastMessages = new ArrayList<String>();
	boolean shownLastMessages = false;
	boolean overridefeedback;
	Combatant lastwascomputermove;
	boolean firstvision;
	boolean allvisible;
	/**
	 * Allows the human player to use up to .5AP of movement before ending turn.
	 */
	public float spentap = 0;
	public LevelMapPanel levelMap = null;
	static public Combatant lastlooked = null;

	public BattleScreen() {
		super(Javelin.app);
		Javelin.settexture(QuestApp.DEFAULTTEXTURE);
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

	public BattleScreen(final QuestApp q, final BattleMap mapp,
			boolean addsidebar) {
		super(q);
		map = mapp;
		if (q == null) {
			return;
		}
		BattleScreen.active = this;
		q.setScreen(this);
		setForeground(Color.white);
		setBackground(Color.black);
		setLayout(new BorderLayout());
		messagepanel = new MessagePanel(q);
		add(messagepanel, "South");
		mappanel = new MapPanel(this);
		add(mappanel, "Center");
		final Panel cp = new Panel();
		cp.setLayout(new BorderLayout());
		add("East", cp);
		statuspanel = new StatusPanel();
		if (addsidebar) {
			cp.add("Center", statuspanel);
			if (addminimap()) {
				levelMap = new LevelMapPanel();
				cp.add(levelMap, "South");
			}
		}
		setFont(QuestApp.mainfont);
		Game.enterMap(map, Game.hero().x, Game.hero().y);
		q.switchScreen(this);
		BattleScreen.active = this;
		// endTurn();
		BattleScreen.originalblueteam =
				new ArrayList<Combatant>(BattleMap.blueTeam);
		BattleScreen.originalredteam =
				new ArrayList<Combatant>(BattleMap.redTeam);
		Game.delayblock = false;
		initmap();
		mappanel.repaint();
	}

	/**
	 * TODO
	 */
	protected void initmap() {
		map.makeAllInvisible();
	}

	/**
	 * this is the main game loop. it catches any exceptions for stability and
	 * lets the game continue <br>
	 * very important that endTurn() gets called after the player moves, this
	 * ensures that the rest of the map stays up to date <br>
	 */
	public void mainLoop() {
		mappanel.setVisible(false);
		Thing hero = map.getState().next.visual;
		mappanel.setPosition(map, hero.x, hero.y);
		Game.redraw();
		mappanel.autozoom(BattleMap.combatants, hero.x, hero.y);
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
		if (Javelin.DEBUG) {
			humanTurn();
			return;
		}
		try {
			humanTurn();
		} catch (BattleEvent e) {
			throw e;
		} catch (RuntimeException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Javelin.app,
					RPG.pick(ERRORQUOTES) + "\n\n"
							+ "Please forward us a screenshot of the console output so we\n"
							+ "can get this error fixed as soon as possible.");
			System.exit(1);
		}
	}

	protected void removehero() {
		Game.hero().remove();
	}

	protected void humanTurn() {
		checkblock();
		final Combatant next = map.getState().next;
		final Thing h = setHero(next);
		if (BattleMap.redTeam.contains(next)) {
			computerTurn();
			return;
		}
		for (Combatant c : BattleMap.combatants) {
			c.refresh();
		}
		while (true) {
			try {
				updatescreen(h);
				Game.messagepanel.clear();
				endturn();
				final KeyEvent updatableUserAction = getUserInput();
				tryTick(h, convertEventToAction(updatableUserAction),
						updatableUserAction.isShiftDown());
				// endturn();
				break;
			} catch (final RepeatTurnException e) {
				updateMessages(lastMessages);
				shownLastMessages = true;
				continue;
			}
		}
		checkEndBattle();
		computerTurn();
	}

	void listen(final Combatant next) {
		if (map.redTeam.contains(next)) {
			return;
		}
		int listen = next.listen();
		for (Combatant c : BattleMap.redTeam) {
			/* TODO currently the AI always has full map info */
			if (listen >= c.movesilently() + (Walker.distance(next, c) - 1)) {
				map.seeTile(c.location[0], c.location[1]);
				map.setVisible(c.location[0], c.location[1]);
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

	private Thing setHero(final Combatant lowestAp) {
		final Thing hero = lowestAp.visual;
		Game.instance().hero = hero;
		return hero;
	}

	protected void updatescreen(final Thing h) {
		centerscreen(h.x, h.y);
		view(h);
		if (shownLastMessages) {
			shownLastMessages = false;
		} else {
			updateMessages();
		}
		statuspanel.repaint();
		Game.redraw();
		levelMap.repaint();
	}

	public void centerscreen(int x, int y) {
		centerscreen(x, y, false);
	}

	public void view(final Thing h) {
		if (map.period == Javelin.PERIOD_EVENING
				|| map.period == Javelin.PERIOD_NIGHT) {
			if (firstvision) {
				firstvision = false;
				return;
			}
			initmap();
			h.calculateVision();
			listen(h.combatant);
		} else if (!allvisible) {
			map.setAllVisible();
			allvisible = true;
		}
	}

	public void centerscreen(int x, int y, boolean force) {
		if (force || center(x, y)) {
			mappanel.viewPosition(map, x, y);
		}
	}

	protected boolean center(int x, int y) {
		return !(2 + mappanel.startx <= x && x <= mappanel.endx - 2
				&& 2 + mappanel.starty <= y && y <= mappanel.endy - 2);
	}

	private void updateMessages() {
		updateMessages(Game.instance().getMessageList());
	}

	public void checkEndBattle() {
		if (BattleMap.redTeam.isEmpty() || BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
		if (!checkforenemies()) {
			throw new EndBattle();
		}
	}

	public boolean checkforenemies() {
		for (Combatant c : BattleMap.redTeam) {
			if (!c.hascondition(Dominated.class)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * First thing to happen after an {@link EndBattle}
	 */
	public void onEnd() {
		BattleMap.victory = BattleMap.redTeam.isEmpty() || !checkforenemies();
		BattleState s = map.getState();
		terminateconditions(s);
		map.setState(s);
		for (Combatant c : fleeing) {
			BattleMap.blueTeam.add(c);
			BattleMap.combatants.add(c);
		}
		Game.messagepanel.clear();
		String combatresult;
		if (BattleMap.victory) {
			combatresult = dealreward();
		} else if (fleeing.isEmpty()) {
			combatresult = "You lost!";
			Squad.active.disband();
			return;
		} else if (Javelin.app.fight.friendly()) {
			combatresult = "You lost!";
		} else {
			combatresult = "Fled from combat. No awards received.";
			if (!BattleMap.victory
					&& fleeing.size() != originalblueteam.size()) {
				combatresult += "\nFallen allies left behind are lost!";
				for (Combatant abandoned : BattleMap.dead) {
					abandoned.hp = Combatant.DEADATHP;
				}
			}
			if (Squad.active.transport != Transport.NONE) {
				combatresult += " Vehicle lost!";
				Squad.active.transport = Transport.NONE;
				Squad.active.updateavatar();
			}
		}
		singleMessage(combatresult + "\nPress any key to continue...",
				Delay.BLOCK);
		getUserInput();
	}

	public void terminateconditions(BattleState s) {
		checkblock();
		for (Combatant c : BattleMap.combatants) {
			for (Condition co : c.conditions) {
				co.finish(s);
				updateMessages();
				checkblock();
			}
		}
	}

	public void afterend() {
		return;
	}

	/**
	 * Only called on victory.
	 * 
	 * @return Reward description.
	 */
	public String dealreward() {
		/* should at least serve as food for 1 day */
		final int bonus = Math.round(Math.max(Squad.active.size() / 2,
				RewardCalculator.receivegold(BattleScreen.originalredteam)));
		Squad.active.members = BattleMap.blueTeam;
		String gold = "";
		if (Javelin.app.fight.rewardgold()) {
			Squad.active.gold += bonus;
			gold = " Party receives $" + SelectScreen.formatcost(bonus) + "!\n";
		}
		return "Congratulations! " + rewardxp() + gold;
	}

	public String rewardxp() {
		int eldifference;
		try {
			eldifference = Math.round(ChallengeRatingCalculator
					.calculateEl(BattleScreen.originalredteam)
					- ChallengeRatingCalculator
							.calculateEl(BattleScreen.originalblueteam));
		} catch (final UnbalancedTeamsException e) {
			throw new RuntimeException(e);
		}
		double partycr = RewardCalculator.getpartycr(eldifference,
				BattleMap.blueTeam.size());
		List<Combatant> survivors =
				(List<Combatant>) BattleMap.blueTeam.clone();
		Collections.sort(survivors, new DescendingLevelComparator());
		float segments = 0;
		for (int i = 1; i <= survivors.size(); i++) {
			segments += i;
		}
		for (int i = 1; i <= survivors.size(); i++) {
			final Combatant survivor = survivors.get(i - 1);
			survivor.xp = survivor.xp.add(new BigDecimal(partycr * i / segments)
					.setScale(2, RoundingMode.HALF_UP));
		}
		return "Party wins "
				+ new BigDecimal(100 * partycr).setScale(0, RoundingMode.UP)
				+ "XP!";
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
		while (!BattleMap.blueTeam.isEmpty()) {
			checkblock();
			endturn();
			checkEndBattle();
			final BattleState state = map.getState();
			final Combatant active = state.next;
			if (state.blueTeam.contains(active)) {
				lastwascomputermove = null;
				return;
			}
			lastwascomputermove = active;
			if (overridefeedback) {
				overridefeedback = false;
			} else {
				computerfeedback("Thinking...\n", Delay.NONE);
			}
			if (Javelin.DEBUG) {
				Action.outcome(ThreadManager.think(state), true);
			} else {
				try {
					Action.outcome(ThreadManager.think(state), true);
				} catch (final RuntimeException e) {
					singleMessage("Fatal error: " + e.getMessage(), Delay.NONE);
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
		// do nothing
	}

	public void computerfeedback(String s, Delay delay) {
		messagepanel.clear();
		if (lastwascomputermove != null) {
			updatescreen(setHero(lastwascomputermove));
		}
		singleMessage(s, delay);
	}

	public void updatescreen(final ChanceNode state, boolean enableoverrun) {
		BattleScreen.active.map.setState(state.n);
		if (lastwascomputermove == null) {
			Game.redraw();
		}
		final BattleState s = (BattleState) state.n;
		Delay delay = state.delay;
		if (enableoverrun && delay == Delay.WAIT
				&& s.redTeam.contains(s.next)) {
			delay = Delay.NONE;
			overridefeedback = true;
		}
		computerfeedback(state.action, delay);
	}

	protected void singleMessage(final String s, final Delay d) {
		Game.message(s, null, d);
	}

	/**
	 * @param thing
	 *            See {@link #performAction(Thing, Action, boolean)}.
	 * @param action
	 *            See {@link #performAction(Thing, Action, boolean)}.
	 * @param isShiftDown
	 *            See {@link #performAction(Thing, Action, boolean)}.
	 */
	public void tryTick(final Thing thing, final Action action,
			final boolean isShiftDown) {
		if (action == null) {
			System.out.println("Null action");
			return;
		}
		performAction(thing, action, isShiftDown);
	}

	public Action convertEventToAction(final KeyEvent keyEvent) {
		if (rejectEvent(keyEvent)) {
			throw new RepeatTurnException();
		}
		return gameHandler.actionFor(keyEvent);
	}

	protected KeyEvent getUserInput() {
		Game.instance().clearMessageList();
		return Game.getInput();
	}

	protected boolean addminimap() {
		return true;
	}

	public void performAction(final Thing thing, final Action action,
			final boolean isShiftDown) {
		Game.actor = thing;
		if (actionHandlers != null) {
			for (final IActionHandler iActionHandler : actionHandlers) {
				if (iActionHandler.handleAction(thing, action, isShiftDown)) {
					return;
				}
			}
		}
		Combatant combatant = Game.hero().combatant;
		float originalap = combatant.ap;
		try {
			if (action.perform(combatant, map, thing)) {
				return;
			}
			if (action == Action.WAIT) {
				thing.combatant.await();
			} else if (action == Action.ZOOM_OUT) {
				mappanel.zoom(-1, true, thing.x, thing.y);
				throw new RepeatTurnException();
			} else if (action == Action.ZOOM_IN) {
				mappanel.zoom(+1, true, thing.x, thing.y);
				throw new RepeatTurnException();
			} else if (action == Action.WITHDRAW) {
				withdraw(combatant);
			} else if (action == Action.LOOK) {
				doLook();
			} else if (action == UseItem.SINGLETON) {
				UseItem.use();
			} else if (action == PassItem.SINGLETON) {
				PassItem.give();
			} else {
				throw new RepeatTurnException();
			}
		} catch (EndBattle e) {
			throw e;
		} catch (Exception e) {
			// TODO throw on debug?
			if (!(e instanceof RepeatTurnException)) {
				e.printStackTrace();
			}
			throw new RepeatTurnException();
		} finally {
			if (originalap != combatant.ap) {
				spendap(combatant);
			}
		}
	}

	public void spendap(Combatant combatant) {
		for (Combatant c : BattleMap.combatants) {
			if (c.id == combatant.id) {
				c.ap += spentap;
				break;
			}
		}
		spentap = 0;
	}

	protected void withdraw(Combatant combatant) {
		if (map.getState().isEngaged(combatant)) {
			if (Javelin.DEBUG) {
				Game.message("Press w to cancel battle (debug feature)", null,
						Delay.NONE);
				if (Game.getInput().getKeyChar() == 'w') {
					for (Combatant c : new ArrayList<Combatant>(
							BattleMap.blueTeam)) {
						escape(c);
					}
					throw new EndBattle();
				}
			}
			Game.message("Disengage first!", null, Delay.BLOCK);
			InfoScreen.feedback();
			throw new RepeatTurnException();
		}
		Game.message(
				"Are you sure you want to escape? Press ENTER to confirm...\n",
				null, Delay.NONE);
		if (Game.getInput().getKeyChar() != '\n') {
			throw new RepeatTurnException();
		}
		escape(combatant);
		Game.message("Escapes!", null, Delay.WAIT);
		if (BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
	}

	void escape(Combatant combatant) {
		fleeing.add(combatant);
		BattleMap.blueTeam.remove(combatant);
		BattleMap.combatants.remove(combatant);
		combatant.visual.remove();
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

	public Point getTargetLocation() {
		return getTargetLocation(Game.hero());
	}

	public Point getTargetLocation(Thing a) {
		if (a == null) {
			a = Game.hero();
		}
		return getTargetLocation(a.getMap(), new Point(a.x, a.y));
	}

	// get location, initially place crosshairs at start
	public Point getTargetLocation(final BattleMap m, final Point start) {
		if (start == null) {
			return getTargetLocation();
		}
		mappanel.setCursor(start.x, start.y);
		mappanel.viewPosition(m, start.x, start.y);
		// initial look
		doLookPoint(new Point(mappanel.curx, mappanel.cury));
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
					for (Combatant c : BattleMap.combatants) {
						if (c.location[0] == mappanel.curx
								&& c.location[1] == mappanel.cury) {
							new StatisticsScreen(c);
							break;
						}
					}
					break;
				case 'q':
					mappanel.clearCursor();
					return null;
				default:
					mappanel.clearCursor();
					return new Point(mappanel.curx, mappanel.cury);
				}
				mappanel.setCursor(
						BattleScreen.checkbounds(mappanel.curx + dx, map.width),
						BattleScreen.checkbounds(mappanel.cury + dy,
								map.height));
				mappanel.viewPosition(m, mappanel.curx, mappanel.cury);
				doLookPoint(new Point(mappanel.curx, mappanel.cury));
			}
		} finally {
			lastlooked = null;
		}
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

	private void doLook() {
		doLookPoint(getTargetLocation());
		throw new RepeatTurnException();
	}

	private void doLookPoint(final Point p) {
		if (p == null) {
			return;
		}
		if (!map.isVisible(p.x, p.y)) {
			lookmessage("Can't see");
			return;
		}
		lookmessage("");
		lastlooked = null;
		for (Thing t = map.getObjects(p.x, p.y); t != null
				&& t.isVisible(Game.hero()); t = t.next) {
			final Combatant combatant = Javelin.getCombatant(t);
			if (combatant != null) {
				lookmessage(describestatus(combatant, map.getState()));
				lastlooked = combatant;
			} else if (!Movement.canMove(t, map, p.x, p.y)) {
				lookmessage("Blocked");
			} else if (map.getTile(p.x, p.y) == Tile.POOL) {
				lookmessage("Flooded");
			}
		}
		if (Tile.isSolid(map, p.x, p.y)) {
			lookmessage("Blocked");
		}
		BattleScreen.active.statuspanel.repaint();
	}

	public String describestatus(final Combatant combatant,
			final BattleState state) {
		String description = combatant + " (" + combatant.getStatus();
		final ArrayList<String> statuslist = combatant.liststatus(state);
		for (final String status : statuslist) {
			description += ", " + status;
		}
		if (Javelin.DEBUG) {
			description += ", " + (combatant.ap) + "ap";
		}
		return description + ")\n";
	}

	private void lookmessage(final String string) {
		messagepanel.clear();
		Game.message(
				"Examine: move the cursor over another combatent, press v to view character sheet.\n\n"
						+ string,
				null, Delay.NONE);
		updateMessages();
		Game.redraw();
	}

	public ArrayList<Combatant> fleeing = new ArrayList<Combatant>();

	/**
	 * @param mappanel
	 *            The mappanel to set.
	 */
	public void setMappanel(final MapPanel mappanel) {
		this.mappanel = mappanel;
	}

	/**
	 * @return Returns the mappanel.
	 */
	public MapPanel getMappanel() {
		return mappanel;
	}

	public LevelMapPanel getLevelMap() {
		return levelMap;
	}

	public void setposition() {
		getMappanel().setPosition(Game.hero().getMap(), Game.hero().x,
				Game.hero().y);
	}

	public boolean drawbackground() {
		return true;
	}

	public boolean scale() {
		return true;
	}

}