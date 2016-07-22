package javelin.view.mappanel.battle.overlay;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.action.Movement;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;

public class Mover extends Walker {
	public class Step {
		public final int x, y;
		public final float apcost;
		boolean engaged;

		public Step(final int x2, final int y2, final float apcost2,
				final boolean engaged) {
			x = x2;
			y = y2;
			apcost = apcost2;
			this.engaged = engaged;
		}
	}

	Combatant current;
	public ArrayList<Step> steps = new ArrayList<Step>(1);

	public Mover(Point from, Point to, Combatant current, BattleState state) {
		super(from, to, state);
		this.current = current;
	}

	@Override
	public ArrayList<javelin.controller.walker.Step> walk() {
		ArrayList<javelin.controller.walker.Step> walk = super.walk();
		if (walk == null) {
			if (partial == null) {
				return null;
			}
			walk = partial;
		} else if (validatefinal()) {
			walk.add(new javelin.controller.walker.Step(targetx, targety));
		}
		float totalcost = 0;
		final boolean engaged = isengaged();
		for (final javelin.controller.walker.Step s : walk) {
			totalcost += getcost(engaged, s);
			steps.add(new Step(s.x, s.y, totalcost, engaged));
			if (end(totalcost, engaged, s)) {
				break;
			}
		}
		return walk;
	}

	protected boolean end(float totalcost, final boolean engaged,
			final javelin.controller.walker.Step s) {
		return engaged || engaged(s.x, s.y) || totalcost >= 1
				|| state.getmeld(s.x, s.y) != null;
	}

	protected float getcost(final boolean engaged,
			final javelin.controller.walker.Step s) {
		final float apcost;
		if (engaged) {
			apcost = Movement.disengage(current);
		} else if (current.burrowed) {
			apcost = javelin.controller.action.Movement
					.converttoap(current.source.burrow);
		} else if (state.map[s.x][s.y].flooded) {
			int swim = current.source.swim();
			apcost = javelin.controller.action.Movement
					.converttoap(swim > 0 ? swim : current.source.walk / 2);
		} else {
			apcost = javelin.controller.action.Movement
					.converttoap(current.source.gettopspeed());
		}
		return apcost;
	}

	protected boolean isengaged() {
		return engaged(current.location[0], current.location[1]);
	}

	protected boolean validatefinal() {
		return valid(targetx, targety, state);
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

	@Override
	protected ArrayList<javelin.controller.walker.Step> getsteplist() {
		// return new NextMove(targetx, targety);
		return new ArrayList<javelin.controller.walker.Step>();
	}

	public String drawtext(float apcost) {
		return Float.toString(apcost).substring(0, 3);
	}
}