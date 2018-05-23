package javelin.view.mappanel.battle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javelin.controller.Point;
import javelin.controller.action.Examine;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.old.Game;
import javelin.old.Game.Delay;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.MoveOverlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.mappanel.battle.action.MeleeMouseAction;
import javelin.view.mappanel.battle.action.MoveMouseAction;
import javelin.view.mappanel.battle.action.RangedMouseAction;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Handles mouse events for {@link BattleScreen}.
 *
 * @author alex
 */
public class BattleMouse extends Mouse {
	static final BattleMouseAction[] ACTIONS = new BattleMouseAction[] {
			new MoveMouseAction(), new MeleeMouseAction(),
			new RangedMouseAction() };

	/** Constructor. */
	public BattleMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (overrideinput()) {
			return;
		}
		if (!Game.userinterface.waiting) {
			return;
		}
		final Tile t = gettile(e);
		final BattleState s = Fight.state;
		final Combatant target = s.getcombatant(t.x, t.y);
		final int button = e.getButton();
		if (button == MouseEvent.BUTTON3 && target != null) {
			BattleScreen.perform(new Runnable() {
				@Override
				public void run() {
					if (!target.source.passive) {
						new StatisticsScreen(target);
					}
				}
			});
			return;
		}
		if (button == MouseEvent.BUTTON1) {
			click(target, s);
			return;
		}
		super.mouseClicked(e);
	}

	void click(final Combatant target, final BattleState s) {
		final Combatant current = BattlePanel.current;
		BattleMouseAction action = getaction(s, target, current);
		Runnable outcome = action == null ? null
				: action.act(current, target, s);
		if (outcome != null) {
			BattleScreen.perform(outcome);
		}
		if (MapPanel.overlay != null
				&& (action == null || action.clearoverlay)) {
			MapPanel.overlay.clear();
		}
	}

	public BattleMouseAction getaction(final BattleState s,
			final Combatant target, final Combatant current) {
		BattleMouseAction custom = target == null ? null
				: target.getmouseaction();
		if (custom != null && custom.validate(current, target, s)) {
			return custom;
		}
		for (BattleMouseAction a : ACTIONS) {
			if (a.validate(current, target, s)) {
				return a;
			}
		}
		return null;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		MoveOverlay.cancel();
		if (Examine.lastlooked != null) {
			Examine.lastlooked = null;
			BattleScreen.active.statuspanel.repaint();
		}
		if (!Game.userinterface.waiting) {
			return;
		}
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
		BattleScreen.active.messagepanel.clear();
		try {
			final Tile t = gettile(e);
			final BattleState s = Fight.state;
			final Combatant current = s.clone(BattlePanel.current);
			final Combatant target = s.getcombatant(t.x, t.y);
			BattleMouseAction action = getaction(s, target, current);
			if (action == null) {
				final String status = target + " (" + target.getstatus() + ")";
				showstatus(status, target, false);
			} else {
				action.onenter(current, target, t, s);
			}
			if (target != null && (action == null || action.clearoverlay)) {
				Examine.lastlooked = target;
				BattleScreen.active.statuspanel.repaint();
			}
		} finally {
			Game.messagepanel.getPanel().repaint();
		}
	}

	public static void showstatus(String status, Combatant c, boolean target) {
		if (target) {
			Point p = c.getlocation();
			MapPanel.overlay = new TargetOverlay(p.x, p.y);
			Tile t = BattleScreen.active.mappanel.tiles[p.x][p.y];
			MapPanel.overlay.overlay(t);
		}
		status += "\n\nConditions: ";
		String list = c.printstatus(Fight.state);
		status += list.isEmpty() ? "none" : list;
		Game.message(status += ".", Delay.NONE);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!Game.userinterface.waiting) {
			return;
		}
		super.mouseWheelMoved(e);
		BattleScreen.active.mappanel.center(BattlePanel.current.location[0],
				BattlePanel.current.location[1], true);
	}
}