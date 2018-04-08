package javelin.view.mappanel.battle.overlay;

import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.action.Movement;
import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.state.Meld;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;

/**
 * TODO could probably use the hierarchy structure better instead of declaring
 * our own {@link #steps}, etc.
 * 
 * @author alex
 */
public class BattleWalker extends Walker {
	/**
	 * Note that AP cost has a different meaning depending on context. For
	 * battle is literal AP, for world and dungeons is chance of encounter,
	 * unrelated to time.
	 * 
	 * @author alex
	 */
	public class BattleStep extends Step {
		public final float apcost;
		public boolean engaged;
		public boolean safe = false;
		public float totalcost;

		public BattleStep(final int x, final int y, final float apcost,
				final float totalcost, final boolean engaged) {
			super(x, y);
			this.apcost = apcost;
			this.totalcost = totalcost;
			this.engaged = engaged;
		}
	}

	Combatant current;
	public ArrayList<BattleStep> steps = new ArrayList<BattleStep>(1);

	public BattleWalker(Point from, Point to, Combatant current,
			BattleState state) {
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
			walk.add(new javelin.controller.walker.Step(tox, toy));
		}
		final boolean engaged = isengaged();
		float totalcost = BattleScreen.partialmove;
		for (final javelin.controller.walker.Step s : walk) {
			float stepcost = getcost(engaged, s);
			totalcost += stepcost;
			steps.add(new BattleStep(s.x, s.y, stepcost, totalcost, engaged));
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
		Meld m = state.getmeld(tox, toy);
		if (m != null && current.ap >= m.meldsat) {
			return true;
		}
		return valid(tox, toy, state);
	}

	@Override
	protected boolean valid(final int x, final int y,
			final BattleState state2) {
		if (current.source.fly == 0 && state.map[x][y].blocked) {
			return false;
		}
		if (state.getcombatant(x, y) != null || state.getmeld(x, y) != null) {
			return false;
		}
		try {
			return (((BattlePanel) BattleScreen.active.mappanel).daylight
					|| state.haslineofsight(current,
							new Point(x, y)) != Vision.BLOCKED);
		} catch (NullPointerException e) {
			return false;
		} catch (ClassCastException e) {
			return false;
		}
	}

	boolean engaged(final int xp, final int yp) {
		if (current.burrowed) {
			return false;
		}
		final int maxx = Math.min(xp + 1, state.map.length);
		final int maxy = Math.min(yp + 1, state.map[0].length);
		for (int x = Math.max(xp - 1, 0); x <= maxx; x++) {
			for (int y = Math.max(yp - 1, 0); y <= maxy; y++) {
				final Combatant c = state.getcombatant(x, y);
				if (c != null && !c.isally(current, state)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected ArrayList<Step> getsteplist() {
		return new ArrayList<Step>();
	}

	public String drawtext(float apcost) {
		return Float.toString(apcost).substring(0, 3);
	}

	@Override
	public void reset() {
		super.reset();
		steps.clear();
	}

	public Point resetlocation() {
		return null;
	}
}