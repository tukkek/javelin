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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.controller.Movement;
import javelin.controller.action.Action;
import javelin.controller.action.ActionDescription;
import javelin.controller.action.ActionMapping;
import javelin.controller.action.GiveItem;
import javelin.controller.action.UseItem;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.ThreadManager;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.BattleEvent;
import javelin.controller.exception.EndBattle;
import javelin.controller.exception.RepeatTurnException;
import javelin.controller.exception.UnbalancedTeamsException;
import javelin.model.BattleMap;
import javelin.model.condition.Condition;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.MapPanel;
import javelin.view.StatusPanel;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Being;
import tyrant.mikera.tyrant.Door;
import tyrant.mikera.tyrant.Event;
import tyrant.mikera.tyrant.Food;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;
import tyrant.mikera.tyrant.GameHandler;
import tyrant.mikera.tyrant.IActionHandler;
import tyrant.mikera.tyrant.InfoScreen;
import tyrant.mikera.tyrant.InventoryScreen;
import tyrant.mikera.tyrant.LevelMapPanel;
import tyrant.mikera.tyrant.MessagePanel;
import tyrant.mikera.tyrant.QuestApp;
import tyrant.mikera.tyrant.Screen;
import tyrant.mikera.tyrant.Spell;
import tyrant.mikera.tyrant.Tile;

public class BattleScreen extends Screen {
	class DescendingLevelComparator implements Comparator<Combatant> {
		@Override
		public int compare(Combatant arg0, Combatant arg1) {
			return new Integer(sumcrandxp(arg0)).compareTo(sumcrandxp(arg1));
		}

		public int sumcrandxp(Combatant arg0) {
			return -Math
					.round(100 * (arg0.xp.floatValue() + ChallengeRatingCalculator
							.calculateCr(arg0.source)));
		}
	}

	private static final Preferences PREFERENCES = Preferences
			.userNodeForPackage(BattleScreen.class);
	String[] ERRORQUOTES = new String[] { "A wild error appears!",
			"You were eaten by a grue.", "So again it has come to pass..." };
	private static final long serialVersionUID = 3907207143852421428L;

	public MapPanel mappanel;
	public MessagePanel messagepanel;
	public StatusPanel statuspanel;
	public GameHandler gameHandler = new GameHandler();
	public BattleMap map;
	private InventoryScreen inventoryScreen;
	private List<IActionHandler> actionHandlers;
	private ArrayList<String> lastMessages = new ArrayList<String>();
	private boolean shownLastMessages = false;
	private boolean overridefeedback;
	private Combatant lastcomputermove;

	public static List<Combatant> originalredteam;
	public static List<Combatant> originalblueteam;

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

	/**
	 * this is the main game loop. it catches any exceptions for stability and
	 * lets the game continue <br>
	 * very important that endTurn() gets called after the player moves, this
	 * ensures that the rest of the map stays up to date <br>
	 */
	public void mainLoop() {
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
		} else {
			try {
				humanTurn();
			} catch (BattleEvent e) {
				throw e;
			} catch (RuntimeException e) {
				e.printStackTrace();
				JOptionPane
						.showMessageDialog(
								this,
								RPG.pick(ERRORQUOTES)
										+ "\n\n"
										+ "Please forward us a screenshot of the console output so we\n"
										+ "can get this error fixed as soon as possible.");
				System.exit(1);
			}
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
				final KeyEvent updatableUserAction = getUserInput();
				tryTick(h, convertEventToAction(updatableUserAction),
						updatableUserAction.isShiftDown());
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
		if (map.period == Javelin.PERIOD_EVENING
				|| map.period == Javelin.PERIOD_NIGHT) {
			map.makeAllInvisible();
			h.calculateVision();
		} else {
			map.setAllVisible();
		}
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
		mappanel.viewPosition(map, x, y);
	}

	private void updateMessages() {
		updateMessages(Game.instance().getMessageList());
	}

	public void checkEndBattle() {
		if (BattleMap.redTeam.isEmpty() || BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
	}

	/**
	 * First thing to happen after an {@link EndBattle}
	 */
	public void onEnd() {
		for (Combatant c : fleeing) {
			BattleMap.blueTeam.add(c);
			BattleMap.combatants.add(c);
		}
		Game.messagepanel.clear();
		String combatresult;
		if (BattleMap.redTeam.isEmpty()) {
			combatresult = dealreward();
		} else if (fleeing.isEmpty()) {
			combatresult = "You lost!";
			Squad.active.disband();
			return;
		} else {
			combatresult = "Fled from combat. No awards received.";
			if (!BattleMap.redTeam.isEmpty() && !BattleMap.dead.isEmpty()) {
				combatresult += "\nFallen allies left behind are lost!";
				BattleMap.dead.clear();
			}
		}
		singleMessage(combatresult + "\nPress any key to continue...",
				Delay.BLOCK);
		getUserInput();
	}

	public void afterend() {
		return;
	}

	public String dealreward() {
		final int bonus = RewardCalculator.receivegold(originalredteam);
		Squad.active.gold += bonus;
		Squad.active.members = BattleMap.blueTeam;
		final String gold = " Party receives $"
				+ SelectScreen.formatcost(bonus) + "!\n";
		return "Congratulations! " + rewardxp() + gold;
	}

	public String rewardxp() {
		int eldifference;
		try {
			eldifference = Math.round(ChallengeRatingCalculator
					.calculateEl(ChallengeRatingCalculator
							.convertlist(originalredteam))
					- ChallengeRatingCalculator
							.calculateEl(ChallengeRatingCalculator
									.convertlist(originalblueteam)));
		} catch (final UnbalancedTeamsException e) {
			throw new RuntimeException(e);
		}
		double partycr = RewardCalculator.getpartycr(eldifference,
				BattleMap.blueTeam.size());
		List<Combatant> survivors = (List<Combatant>) BattleMap.blueTeam
				.clone();
		Collections.sort(survivors, new DescendingLevelComparator());
		float segments = 0;
		for (int i = 1; i <= survivors.size(); i++) {
			segments += i;
		}
		for (int i = 1; i <= survivors.size(); i++) {
			final Combatant survivor = survivors.get(i - 1);
			survivor.xp = survivor.xp
					.add(new BigDecimal(partycr * i / segments).setScale(2,
							RoundingMode.HALF_UP));
		}
		return "Party wins "
				+ new BigDecimal(100 * partycr).setScale(0, RoundingMode.UP)
				+ "XP!";
	}

	private void updateMessages(final ArrayList<String> messageList) {
		// Game.messagepanel.clear();
		for (final String s : messageList) {
			Game.messagepanel.add(s + "\n");
		}
		lastMessages = new ArrayList<String>(messageList);
		messageList.clear();
		Game.messagepanel.getPanel().repaint();
	}

	private void computerTurn() {
		overridefeedback = false;
		while (!BattleMap.blueTeam.isEmpty()) {
			checkblock();
			final BattleState state = map.getState();
			final Combatant active = state.next;
			if (state.blueTeam.contains(active)) {
				lastcomputermove = null;
				return;
			}
			lastcomputermove = active;
			if (overridefeedback) {
				overridefeedback = false;
			} else {
				computerfeedback("Thinking...\n", Delay.NONE);
			}
			try {
				Action.outcome(ThreadManager.think(state), true);
			} catch (final RuntimeException e) {
				singleMessage("Fatal error: " + e.getMessage(), Delay.NONE);
				messagepanel.repaint();
				throw e;
			}
		}
	}

	public void computerfeedback(String s, Delay delay) {
		messagepanel.clear();
		if (lastcomputermove != null) {
			updatescreen(setHero(lastcomputermove));
		}
		singleMessage(s, delay);
	}

	public void updatescreen(final ChanceNode state, boolean enableoverrun) {
		BattleScreen.active.map.setState(state.n);
		Game.redraw();
		final BattleState s = (BattleState) state.n;
		Delay delay = state.delay;
		if (enableoverrun && delay == Delay.WAIT && s.redTeam.contains(s.next)) {
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
		endTurn();
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

	public BattleScreen(final QuestApp q, final BattleMap mapp) {
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
		if (addsidebar()) {
			cp.add("Center", statuspanel);

			if (addminimap()) {
				levelMap = new LevelMapPanel();
				cp.add(levelMap, "South");
			}
		}
		setFont(QuestApp.mainfont);
		q.switchScreen(this);
		Game.enterMap(map, Game.hero().x, Game.hero().y);
		endTurn();
		originalblueteam = new ArrayList<Combatant>(BattleMap.blueTeam);
		originalredteam = new ArrayList<Combatant>(BattleMap.redTeam);
		Game.delayblock = false;
		map.setAllVisible();
		mappanel.zoomfactor = PREFERENCES.getInt("zoom", 130);
		mappanel.repaint();
	}

	public void debugclearpreferences() {
		try {
			PREFERENCES.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	protected boolean addsidebar() {
		return true;
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
		if (action.perform(combatant, map)) {
			return;
		}
		if (action == Action.WAIT) {
			thing.combatant.await();
		} else if (action.isMovementKey()) {
			move(thing, action);
		} else if (action == Action.HELP) {
			help(ActionMapping.actions);
		} else if (action == Action.MESSAGES) {
			doMessages();
		} else if (action == Action.ZOOM_OUT) {
			doZoom(25);
			throw new RepeatTurnException();
		} else if (action == Action.ZOOM_IN) {
			doZoom(-25);
			throw new RepeatTurnException();
		} else if (action == Action.FLEE) {
			flee(combatant);
		} else if (action == Action.LOOK) {
			doLook();
		} else if (action == UseItem.SINGLETON) {
			UseItem.use();
		} else if (action == GiveItem.SINGLETON) {
			GiveItem.give();
		} else {
			throw new RepeatTurnException();
		}
	}

	public void move(final Thing thing, final Action action) {
		final javelin.controller.action.Movement moveaction = (javelin.controller.action.Movement) action;
		final Combatant c = Javelin.getCombatant(thing);
		final BattleState state = map.getState();
		try {
			Point to = gameHandler.doDirection(thing, action);
			if (to != null && !lastmovewasattack) {
				c.ap += moveaction.cost(c, state, to.x, to.y);
				String description;
				Delay d;
				if (moveaction.isDisengaging(c, state)) {
					description = "disengages";
					d = Delay.WAIT;
				} else if (Javelin.getLowestAp().equals(c)) {
					description = null;
					d = Delay.NONE;
				} else {
					description = "moves";
					d = Delay.WAIT;
				}
				if (d != Delay.NONE) {
					Game.message(thing.combatant + " " + description + "...",
							null, d);
				}
			}
		} finally {
			lastmovewasattack = false;
		}
	}

	protected void flee(Combatant combatant) {
		if (map.getState().isEngaged(combatant)) {
			Game.message("Disengage first!", null, Delay.BLOCK);
			IntroScreen.feedback();
			throw new RepeatTurnException();
		}
		fleeing.add(combatant);
		BattleMap.blueTeam.remove(combatant);
		BattleMap.combatants.remove(combatant);
		combatant.visual.remove();
		Game.message("Escapes!", null, Delay.WAIT);
		if (BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
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

	public InventoryScreen getInventoryScreen() {
		if (inventoryScreen == null) {
			inventoryScreen = new InventoryScreen();
		}
		return inventoryScreen;
	}

	public Point getSpellTargetLocation(final Thing h, final Thing s) {
		final BattleMap map = h.getMap();
		Thing f = s.getStat("SpellUsage") == Spell.SPELL_OFFENCE ? map
				.findNearestFoe(h) : null;
		if (f != null && !map.isVisible(f.x, f.y)) {
			f = null;
		}

		if (f == null) {
			if (s.getStat("SpellUsage") == Spell.SPELL_OFFENCE) {
				// aim at square in front of caster
				final Point sp = new Point(h.x + h.getStat("DirectionX"), h.y
						+ h.getStat("DirectionY"));
				return getTargetLocation(map, sp);
			}
			return getTargetLocation(h);
		}
		return getTargetLocation(f);
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

		getMappanel().setCursor(start.x, start.y);
		getMappanel().viewPosition(m, start.x, start.y);

		// initial look
		doLookPoint(new Point(getMappanel().curx, getMappanel().cury));

		// get interesting stuff to see
		// Note that the hero is incidentally also seen
		// So there should be no worries of an empty list
		final List stuff = map.findStuff(Game.hero(), BattleMap.FILTER_ITEM
				+ BattleMap.FILTER_MONSTER);
		int stuffIndex = 0;

		// repaint the status panel
		statuspanel.repaint();
		// TODO : get 'x' and 'l' working
		while (true) {
			// looking = true;
			final KeyEvent e = Game.getInput();
			if (e == null) {
				continue;
			}

			char k = Character.toLowerCase(e.getKeyChar());

			final int i = e.getKeyCode();

			// handle key conversions
			switch (i) {
			case KeyEvent.VK_UP:
				k = '8';
				break;
			case KeyEvent.VK_DOWN:
				k = '2';
				break;
			case KeyEvent.VK_LEFT:
				k = '4';
				break;
			case KeyEvent.VK_RIGHT:
				k = '6';
				break;
			case KeyEvent.VK_HOME:
				k = '7';
				break;
			case KeyEvent.VK_END:
				k = '1';
				break;
			case KeyEvent.VK_PAGE_UP:
				k = '9';
				break;
			case KeyEvent.VK_PAGE_DOWN:
				k = '3';
				break;
			case KeyEvent.VK_ESCAPE:
				k = 'q';
				break;
			}

			int dx = 0;
			int dy = 0;
			switch (k) {
			case '*':
				// case 'x':
			case 'l':
				if (stuffIndex >= stuff.size()) {
					stuffIndex = 0;
				}
				Point p = (Point) stuff.get(stuffIndex);
				stuffIndex++;
				if (p == null) {
					p = start;
				}
				dx = p.x - getMappanel().curx;
				dy = p.y - getMappanel().cury;
				break;
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
					if (c.location[0] == getMappanel().curx
							&& c.location[1] == getMappanel().cury) {
						new StatisticsScreen(c);
						break;
					}
				}
				break;
			case 'q':
				getMappanel().clearCursor();
				return null;
			default:
				getMappanel().clearCursor();
				return new Point(getMappanel().curx, getMappanel().cury);
			}
			int x = checkbounds(getMappanel().curx + dx, map.width);
			int y = checkbounds(getMappanel().cury + dy, map.height);
			getMappanel().setCursor(x, y);
			getMappanel().viewPosition(m, getMappanel().curx,
					getMappanel().cury);
			doLookPoint(new Point(getMappanel().curx, getMappanel().cury));
		}
	}

	private int checkbounds(int i, int upperbound) {
		if (i < 0) {
			return 0;
		}
		if (i >= upperbound) {
			return upperbound - 1;
		}
		return i;
	}

	public void castSpell(final Thing h, final Thing s) {
		if (s == null) {
			return;
		}

		final BattleMap map = h.getMap();

		switch (s.getStat("SpellTarget")) {
		case Spell.TARGET_SELF:
			Spell.castAtSelf(s, h);
			break;
		case Spell.TARGET_DIRECTION: {
			Game.messageTyrant("Select Direction:");
			final Point p = Game.getDirection();
			if (p != null) {
				Spell.castInDirection(s, h, p.x, p.y);
			}
			break;
		}
		case Spell.TARGET_LOCATION:
			Thing f = s.getStat("SpellUsage") == Spell.SPELL_OFFENCE ? map
					.findNearestFoe(h) : null;
			if (f != null && !map.isVisible(f.x, f.y)) {
				f = null;
			}
			final Point p = getSpellTargetLocation(h, s);
			// Do not confuse player with possible false info
			Game.messageTyrant("");
			if (p != null) {
				// don't fire offensive spell at self by accident
				if (p.x == h.x && p.y == h.y
						&& s.getStat("SpellUsage") == Spell.SPELL_OFFENCE) {
					Game.messageTyrant("Are you sure you want to target yourself? (y/n)");
					final char opt = Game.getOption("yn");
					if (opt == 'n') {
						break;
					}
				}

				if (map.isVisible(p.x, p.y)) {
					Spell.castAtLocation(s, h, map, p.x, p.y);
				} else {
					Game.messageTyrant("You cannot see to focus your power");
				}
			}
			break;
		case Spell.TARGET_ITEM:
			final Thing t = Game.selectItem("Select an item:", h.getItems());
			questapp.switchScreen(this);
			if (t != null) {
				Spell.castAtObject(s, h, t);
			}
			break;
		}
	}

	public void endTurn() {
		// make sure hero is not responsible for these actions
		Game.actor = null;
		final Thing h = Game.hero();
		h.set("IsFrozen", 0);
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

	public void doZoom(final int factor) {
		getMappanel().zoomfactor += factor;
		if (getMappanel().zoomfactor < 25) {
			getMappanel().zoomfactor = 25;
		}
		if (getMappanel().zoomfactor > 800) {
			getMappanel().zoomfactor = 800;
		}
		Game.warn("Zooming.... (" + getMappanel().zoomfactor + "%)");
		getMappanel().repaint();
		PREFERENCES.putInt("zoom", getMappanel().zoomfactor);
	}

	public LevelMapPanel levelMap = null;

	public void doIntroduction(final Thing person) {
		final String intro = (String) person.get("Introduction");
		if (intro != null) {
			Game.messageTyrant(intro);
			person.set("Introduction", null);
		}
	}

	public void doChat(final Thing h) {
		Thing person = null;
		if (map.countNearby("IsIntelligent", h.x, h.y, 1) > 2) {
			Game.messageTyrant("Chat: select direction");
			final Point p = Game.getDirection();
			person = map
					.getFlaggedObject(h.x + p.x, h.y + p.y, "IsIntelligent");
		} else {
			try {
				person = map.getNearby("IsIntelligent", h.x, h.y, 1);
			} catch (final Exception anyex) {
				anyex.printStackTrace();
			}
		}

		if (person == null || person.equals(Game.hero())) {
			if (!doOpen(h)) {
				Game.messageTyrant("You mumble to yourself");
			}
			return;
		}

		if (person.handles("OnChat")) {
			doIntroduction(person);
			final Event e = new Event("Chat");
			e.set("Target", h);
			person.handle(e);
		} else if (person.equals(Game.hero())) {
			Game.messageTyrant("You mumble to yourself");
		} else if (person.getFlag("IsIntelligent")) {
			doIntroduction(person);
			Game.messageTyrant("You chat with " + person.getTheName()
					+ " for some time");

		} else {
			Game.messageTyrant("You can't talk to " + person.getTheName());
		}
		h.incStat("APS", -200);

	}

	public void doDrop(final Thing h, final boolean ext) {
		if (ext) {
			Thing o = Game.selectItem("Select item to drop:", h.getItems());
			while (o != null) {
				Being.tryDrop(h, o);
				final Thing[] its = h.getItems();
				if (its.length == 0) {
					break;
				}
				o = Game.selectItem("Select item to drop:", its, true);
			}
		} else {
			final Thing o = Game.selectItem("Select item to drop:",
					h.getItems());
			if (o != null) {
				Being.tryDrop(h, o);
			}
		}
	}

	public void doEat(final Thing h) {
		final Thing o = Game.selectItem("Select item to eat:",
				h.getFlaggedContents("IsEdible"));
		questapp.switchScreen(this);
		if (o != null) {
			Food.eat(h, o);
		}
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
		for (Thing t = map.getObjects(p.x, p.y); t != null
				&& t.isVisible(Game.hero()); t = t.next) {
			final Combatant combatant = Javelin.getCombatant(t);
			if (combatant != null) {
				lookmessage(describestatus(t, combatant, map));
			} else if (!Movement.canMove(t, map, p.x, p.y)) {
				lookmessage("Blocked");
			} else if (map.getTile(p.x, p.y) == Tile.POOL) {
				lookmessage("Flooded");
			}
		}
		if (Tile.isSolid(map, p.x, p.y)) {
			lookmessage("Blocked");
		}
	}

	static public String describestatus(final Thing t,
			final Combatant combatant, BattleMap map) {
		String description = combatant + " (" + combatant.getStatus();
		final ArrayList<String> statuslist = new ArrayList<String>();
		BattleState state = map.getState();
		if (state.isEngaged(combatant)) {
			statuslist.add("engaged");
			for (Combatant c : BattleMap.blueTeam.contains(combatant) ? BattleMap.redTeam
					: BattleMap.blueTeam) {
				if (state.isflanked(state.translatecombatant(combatant),
						state.translatecombatant(c))) {
					statuslist.add("flanked");
					break;
				}
			}
		}
		if (combatant.surprise() != 0) {
			statuslist.add("flat-footed");
		}
		Vision v = state.hasLineOfSight(state.next, combatant);
		if (v == Vision.COVERED) {
			statuslist.add("covered");
		} else if (v == Vision.BLOCKED) {
			statuslist.add("blocked");
		}
		if (combatant.source.fly == 0
				&& state.map[combatant.location[0]][combatant.location[1]].flooded) {
			statuslist.add("knee-deep");
		}
		for (Condition c : combatant.conditions) {
			statuslist.add(c.describe());
		}
		for (final String status : statuslist) {
			description += ", " + status;
		}
		return description + ")\n";
	}

	private void lookmessage(final String string) {
		messagepanel.clear();
		Game.message(
				"Examine: move the cursor over another combatent, press v to view character sheet\n\n"
						+ string, null, Delay.NONE);
		updateMessages();
		Game.redraw();
	}

	private void doMessages() {
		String text = "";
		for (final String s : lastMessages) {
			text += s + "\n";
		}
		switchText(text);
	}

	private boolean doOpen(final Thing h) {
		Thing t = null;
		if (map.countNearby("IsOpenable", h.x, h.y, 1) > 2) {
			Game.messageTyrant("Select direction");
			final Point p = Game.getDirection();
			if (p != null && (p.x != 0 || p.y != 0)) {
				messagepanel.clear();
				t = map.getFlaggedObject(h.x + p.x, h.y + p.y, "IsOpenable");
			}
		} else {
			try {
				t = map.getNearby("IsOpenable", h.x, h.y, 1);
			} catch (final Exception anyex) {
				anyex.printStackTrace();
			}
		}
		if (t != null) {
			Door.useDoor(h, t);
			h.incStat("APS", -Being.actionCost(h));
			return true;
		}
		return false;
	}

	public static boolean lastmovewasattack = false;

	private ArrayList<Combatant> fleeing = new ArrayList<Combatant>();

	public void help(final ActionDescription[] actions) {
		String text = "Movement is also used to attack adjacent enemies.\n\nExample:\nKey or keys: command name.\n\n";
		for (final ActionDescription a : actions) {
			final String[] keys = a.getDescriptiveKeys();
			if (keys.length == 0) {
				continue;
			}
			boolean first = true;
			for (final String key : keys) {
				if (key.contains("arrow")) {
					continue;
				}
				if (first) {
					first = false;
				} else {
					text += " or ";
				}
				text += key;
			}
			text += ": " + a.getDescriptiveName() + "\n";
		}
		switchText(text);

	}

	private void switchText(final String text) {
		questapp.switchScreen(new InfoScreen(Game.getQuestapp(), text));
		Game.getInput();
		questapp.switchScreen(this);
	}

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

}