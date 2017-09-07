package javelin.view.mappanel.battle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.Examine;
import javelin.controller.action.Fire;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.model.state.Meld;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.MoveOverlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.BattleMover;
import javelin.view.mappanel.battle.overlay.BattleMover.Step;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Handles mouse events for {@link BattleScreen}.
 * 
 * @author alex
 */
public class BattleMouse extends Mouse {
	enum Action {
		MELEE, RANGED, MOVE
	}

	/**
	 * TODO there is an edge case here for the future: if you're not engaged
	 * with an opponent but could either attack with a ranged weapon or a reach
	 * weapon
	 */
	static Action getaction(final Combatant current, final Combatant target,
			final BattleState s) {
		if (target == null) {
			return Action.MOVE;
		}
		if (target.isally(current, s)) {
			return null;
		}
		if (current.isadjacent(target)) {
			return current.source.melee.isEmpty() ? null : Action.MELEE;
		}
		return current.source.ranged.isEmpty() || s.isengaged(current)
				|| s.haslineofsight(current, target) == Vision.BLOCKED ? null
						: Action.RANGED;
	}

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
		final Tile t = (Tile) e.getSource();
		final BattleState s = Fight.state;
		final Combatant target = s.getcombatant(t.x, t.y);
		final int button = e.getButton();
		if (button == MouseEvent.BUTTON3 && target != null) {
			BattleScreen.perform(new Runnable() {
				@Override
				public void run() {
					new StatisticsScreen(target);
				}
			});
			return;
		}
		if (button == MouseEvent.BUTTON1) {
			final Combatant current = BattlePanel.current;
			final Action a = getaction(current, target, s);
			if (a == Action.MOVE) {
				if (MapPanel.overlay instanceof MoveOverlay) {
					final MoveOverlay walk = (MoveOverlay) MapPanel.overlay;
					if (!walk.path.steps.isEmpty()) {
						walk.clear();
						BattleScreen.perform(new Runnable() {
							@Override
							public void run() {
								final Step to = walk.path.steps
										.get(walk.path.steps.size() - 1);
								BattleState move = Fight.state;
								Combatant c = move.clone(current);
								c.location[0] = to.x;
								c.location[1] = to.y;
								c.ap += to.apcost + BattleScreen.active.spentap;
								BattleScreen.active.spentap = 0;
								Meld m = move.getmeld(to.x, to.y);
								if (m != null && c.ap >= m.meldsat) {
									Javelin.app.fight.meld(c, m);
								}
							}
						});
					}
				}
				return;
			} else if (a == Action.MELEE) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						current.meleeattacks(target, s);
					}
				});
			} else if (a == Action.RANGED) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						current.rangedattacks(target, s);
					}
				});
			}
			if (MapPanel.overlay != null) {
				MapPanel.overlay.clear();
			}
			return;
		}
		super.mouseClicked(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
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
			final Tile t = (Tile) e.getSource();
			final BattleState s = Fight.state;
			final Combatant current = s.clone(BattlePanel.current);
			final Combatant target = s.getcombatant(t.x, t.y);
			final Action a = getaction(current, target, s);
			if (a == Action.MOVE) {
				MoveOverlay.schedule(new MoveOverlay(new BattleMover(
						new Point(current.location[0], current.location[1]),
						new Point(t.x, t.y), current, s)));
				return;
			} else if (a == Action.MELEE) {
				final List<Attack> attack = current.currentmelee.next == null
						|| current.currentmelee.next.isEmpty()
								? current.source.melee.get(0)
								: current.currentmelee.next;
				final String chance = MeleeAttack.SINGLETON.getchance(current,
						target, attack.get(0), s);
				status(target + " (" + target.getstatus() + ", " + chance
						+ " to hit)", target);
			} else if (a == Action.RANGED) {
				status(Fire.SINGLETON.describehitchance(current, target, s),
						target);
			}
			if (target != null) {
				Examine.lastlooked = target;
				BattleScreen.active.statuspanel.repaint();
			}
		} finally {
			Game.messagepanel.getPanel().repaint();
		}
	}

	void status(String s, Combatant target) {
		MapPanel.overlay = new TargetOverlay(target.location[0],
				target.location[1]);
		Game.message(s, Delay.NONE);
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