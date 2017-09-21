package javelin.controller.action.ai;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.Action;
import javelin.controller.action.ActionCost;
import javelin.controller.ai.AiThread;
import javelin.controller.ai.ChanceNode;
import javelin.controller.ai.Node;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.state.Meld;
import javelin.model.unit.attack.Combatant;
import javelin.view.Images;
import javelin.view.mappanel.battle.overlay.AiOverlay;
import javelin.view.mappanel.battle.overlay.BattleMover;
import javelin.view.mappanel.battle.overlay.BattleMover.BatteStep;

/**
 * Attempst to offer a more fluid experience than having {@link AiMovement}
 * simple do long step-by-step movement. Tries to move at most .5AP (move
 * action).
 * 
 * @author alex
 */
public class AiMovement extends Action implements AiAction {
	/**
	 * Target value for number of outcome nodes.We want to limit the nu,ber of
	 * node outcomes so that the AI doesn't become to slow.
	 */
	static final int MOVES = 8;
	public static final AiMovement SINGLETON = new AiMovement();
	public static final Image MOVEOVERLAY = Images.getImage("overlaymove");

	public class LongMove extends ChanceNode {
		public LongMove(Combatant c, BatteStep s, BattleMover mover, Meld m,
				Node n) {
			super(n, 1, getmessage(c, m, s),
					m == null ? Delay.WAIT : Delay.BLOCK);
			AiOverlay o = new AiOverlay(
					mover.steps.subList(0, mover.steps.indexOf(s)));
			o.affected.add(new Point(mover.sourcex, mover.sourcey));
			o.image = AiMovement.MOVEOVERLAY;
			overlay = o;
		}
	}

	private AiMovement() {
		super("Long move");
		allowburrowed = true;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant c, BattleState s) {
		HashSet<Point> destinations = getdestinations(c, s);
		ArrayList<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>(
				Math.min(destinations.size(), MOVES));
		Point from = new Point(c.location[0], c.location[1]);
		for (Point interest : getpointsofinterest(c, s, destinations)) {
			add(move(c, from, interest, s), outcomes);
		}
		if (outcomes.size() >= MOVES) {
			return outcomes;
		}
		Random r = AiThread.getrandom();
		ArrayList<Point> pool = new ArrayList<Point>(destinations);
		while (outcomes.size() < MOVES && !pool.isEmpty()) {
			Point to = pool.remove(r.nextInt(pool.size()));
			add(move(c, from, to, s), outcomes);
		}
		return outcomes;
	}

	ArrayList<Point> getpointsofinterest(Combatant c, BattleState s,
			HashSet<Point> destinations) {
		final ArrayList<Combatant> enemies = s.getteam(c) == s.blueTeam
				? s.redTeam : s.blueTeam;
		ArrayList<Point> interesting = new ArrayList<Point>();
		for (Combatant enemy : enemies) {
			interesting.add(new Point(enemy.location[0], enemy.location[1]));
		}
		for (Meld m : s.meld) {
			interesting.add(new Point(m.x, m.y));
		}
		for (Point p : new ArrayList<Point>(interesting)) {
			if (destinations.contains(p)) {
				destinations.remove(p);
			} else {
				/* not interesting, can't reach this turn */
				interesting.remove(p);
			}
		}
		return interesting;
	}

	void add(ChanceNode n, ArrayList<List<ChanceNode>> outcomes) {
		if (n != null) {
			ArrayList<ChanceNode> chances = new ArrayList<ChanceNode>();
			chances.add(n);
			outcomes.add(chances);
		}
	}

	LongMove move(Combatant c, Point from, Point to, BattleState s) {
		BattleMover mover = new BattleMover(from, to, c, s);
		mover.walk();
		if (mover.steps == null || mover.steps.isEmpty()) {
			return null;
		}
		BatteStep step = choosestep(mover.steps);
		s = s.clone();
		c = s.clone(c);
		c.ap += step.totalcost;
		c.location[0] = step.x;
		c.location[1] = step.y;
		Meld m = s.getmeld(step.x, step.y);
		if (m != null) {
			c.meld();
			s.meld.remove(m);
		}
		return new LongMove(c, step, mover, m, s);
	}

	BatteStep choosestep(ArrayList<BatteStep> steps) {
		for (BatteStep step : steps) {
			if (step.engaged || step.totalcost >= ActionCost.MOVE) {
				return step;
			}
		}
		return steps.get(steps.size() - 1);
	}

	HashSet<Point> getdestinations(Combatant c, BattleState s) {
		HashSet<Point> visible = new HashSet<Point>();
		final Fight f = Javelin.app.fight;
		int range = 1;
		if (!s.isengaged(c)) {
			range = Math.min(c.view(f.period), c.source.gettopspeed() / 5);
		}
		for (int x = -range; x <= +range; x++) {
			for (int y = -range; y <= +range; y++) {
				Point p = new Point(c.location[0] + x, c.location[1] + y);
				if (!p.validate(0, 0, s.map.length, s.map[0].length)) {
					continue;
				}
				if (s.map[p.x][p.y].blocked && c.source.fly == 0) {
					continue;
				}
				if (s.getcombatant(p.x, p.y) != null) {
					continue;
				}
				Meld m = s.getmeld(p.x, p.y);
				if (m != null && !m.crystalize(s)) {
					continue;
				}
				if (s.haslineofsight(c, p) == Vision.BLOCKED) {
					continue;
				}
				visible.add(p);
			}
		}
		return visible;
	}

	@Override
	public boolean perform(Combatant active) {
		throw new UnsupportedOperationException();
	}

	static String getmessage(Combatant c, Meld m, BatteStep s) {
		if (m != null) {
			return c + " powers up!";
		}
		if (s.engaged) {
			return c + " disengages...";
		}
		return c + " moves...";
	}
}
