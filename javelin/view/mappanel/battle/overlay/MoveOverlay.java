package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.BorderFactory;

import javelin.controller.Point;
import javelin.controller.action.Movement;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.BattleTile;
import javelin.view.screen.BattleScreen;

public class MoveOverlay extends Overlay {
	static final javax.swing.border.Border GREENBORDER =
			BorderFactory.createLineBorder(Color.GREEN, 3);
	static final javax.swing.border.Border REDBORDER =
			BorderFactory.createLineBorder(Color.RED, 3);

	public class Step {
		public final int x, y;
		public final float apcost;
		private boolean engaged;

		public Step(final int x2, final int y2, final float apcost2,
				final boolean engaged) {
			x = x2;
			y = y2;
			apcost = apcost2;
			this.engaged = engaged;
		}
	}

	public class Walk extends Walker {
		Combatant current;
		public ArrayList<Step> steps = new ArrayList<Step>(1);

		public Walk(Combatant current, Point to, BattleState state) {
			super(new Point(current.location[0], current.location[1]), to,
					state);
			this.current = current;
		}

		@Override
		public ArrayList<javelin.controller.walker.Step> walk() {
			ArrayList<javelin.controller.walker.Step> walk = super.walk();
			if (walk == null) {
				if (partial != null) {
					walk = partial;
				} else {
					return null;
				}
			} else if (valid(targetx, targety, state)) {
				walk.add(new javelin.controller.walker.Step(targetx, targety));
			}
			float totalcost = 0;
			final boolean engaged =
					engaged(current.location[0], current.location[1]);
			for (final javelin.controller.walker.Step s : walk) {
				final float apcost;
				if (engaged) {
					apcost = Movement.disengage(current);
				} else if (current.burrowed) {
					apcost = javelin.controller.action.Movement
							.converttoap(current.source.burrow);
				} else if (state.map[s.x][s.y].flooded) {
					int swim = current.source.swim();
					apcost = javelin.controller.action.Movement.converttoap(
							swim > 0 ? swim : current.source.walk / 2);
				} else {
					apcost = javelin.controller.action.Movement
							.converttoap(current.source.gettopspeed());
				}
				totalcost += apcost;
				steps.add(new Step(s.x, s.y, totalcost, engaged));
				if (engaged || engaged(s.x, s.y) || totalcost >= 1
						|| state.getmeld(s.x, s.y) != null) {
					break;
				}
			}
			return walk;
		}

		@Override
		protected boolean valid(final int x, final int y,
				final BattleState state2) {
			if (current.source.fly == 0 && state.map[x][y].blocked) {
				return false;
			}
			return state.getCombatant(x, y) == null
					&& (((BattlePanel) BattleScreen.active.mappanel).daylight
							|| state.hasLineOfSight(current,
									new Point(x, y)) != Vision.BLOCKED);
		}

		boolean engaged(final int xp, final int yp) {
			if (current.burrowed) {
				return false;
			}
			final int maxx = Math.min(xp + 1, state.map.length);
			final int maxy = Math.min(yp + 1, state.map[0].length);
			for (int x = Math.max(xp - 1, 0); x <= maxx; x++) {
				for (int y = Math.max(yp - 1, 0); y <= maxy; y++) {
					final Combatant c = state.getCombatant(x, y);
					if (c != null && !c.isAlly(current, state)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public Walk path;

	public MoveOverlay(Combatant current, Point to, BattleState s) {
		path = new Walk(current, to, s);
		path.walk();
		for (Step step : path.steps) {
			affected.add(new Point(step.x, step.y));
			((BattlePanel) BattleScreen.active.mappanel).tiles[step.x][step.y]
					.repaint();
		}
	}

	@Override
	public void overlay(BattleTile t, Graphics g) {
		for (Step s : path.steps) {
			if (t.x == s.x && t.y == s.y) {
				final boolean partial = !s.engaged && s.apcost <= .5f;
				(partial ? GREENBORDER : REDBORDER).paintBorder(t, g, 0, 0,
						BattlePanel.tilesize, BattlePanel.tilesize);
				g.setColor(partial ? Color.GREEN : Color.RED);
				g.drawString(Float.toString(s.apcost).substring(0, 3), 5,
						BattlePanel.tilesize - 5);
			}
		}
	}
}
