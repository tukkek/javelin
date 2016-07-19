package javelin.view.mappanel;

import java.awt.event.MouseEvent;
import java.util.List;

import javelin.controller.action.Fire;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Attack;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

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
		if (target.isAlly(current, s)) {
			return null;
		}
		if (current.isadjacent(target)) {
			return current.source.melee.isEmpty() ? null : Action.MELEE;
		}
		return current.source.ranged.isEmpty()
				|| s.hasLineOfSight(current, target) == Vision.BLOCKED ? null
						: Action.RANGED;
	}

	public BattleMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (overrideinput()) {
			return;
		}
		if (!Game.getUserinterface().waiting) {
			return;
		}
		final Tile t = (Tile) e.getSource();
		final BattleState s = BattlePanel.state;
		final Combatant target = s.getCombatant(t.x, t.y);
		final int button = e.getButton();
		if (button == MouseEvent.BUTTON3 && target != null) {
			BattleScreen.perform(new Runnable() {
				@Override
				public void run() {
					new StatisticsScreen(target);
				}
			});
			return;
		} else if (button == MouseEvent.BUTTON1) {
			final Combatant current = Game.hero().combatant;
			final Action a = getaction(current, target, s);
			if (a == Action.MOVE) {
				spend(current);
				// move
			} else if (a == Action.MELEE) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						spend(current).meleeAttacks(target, s);
					}
				});
			} else if (a == Action.RANGED) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						spend(current).rangedattacks(target, s);
					}
				});
			}
			return;
		}
		super.mouseClicked(e);
	}

	static Combatant spend(final Combatant c) {
		// BattleScreen.active.spendap(c);
		return c;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (BattleScreen.lastlooked != null) {
			BattleScreen.lastlooked = null;
			BattleScreen.active.statuspanel.repaint();
		}
		if (!Game.getUserinterface().waiting) {
			return;
		}
		BattleScreen.active.messagepanel.clear();
		final Combatant current = Game.hero().combatant;
		final Tile t = (Tile) e.getSource();
		final BattleState s = BattlePanel.state;
		final Combatant target = s.getCombatant(t.x, t.y);
		final Action a = getaction(current, target, s);
		if (a == Action.MOVE) {
			// move
			return;
		} else if (a == Action.MELEE) {
			final List<Attack> attack = current.currentmelee.next == null
					|| current.currentmelee.next.isEmpty()
							? current.source.melee.get(0)
							: current.currentmelee.next;
			final String chance = MeleeAttack.SINGLETON.getchance(current,
					target, attack.get(0), s);
			status(target + " (" + target.getStatus() + ", " + chance
					+ " to hit)");
		} else if (a == Action.RANGED) {
			status(Fire.SINGLETON.describehitchance(current, target, s));
		} else if (target != null) {
			BattleScreen.lastlooked = target;
			BattleScreen.active.statuspanel.repaint();
		} else {
			return;
		}
		Game.messagepanel.getPanel().repaint();
	}

	void status(String s) {
		Game.message(s, null, Delay.NONE);
		BattleScreen.active.updateMessages();
	}
}