package javelin.controller.action;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.action.ai.attack.MeleeAttack;
import javelin.controller.ai.ChanceNode;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game.Delay;
import javelin.controller.walker.ChargePath;
import javelin.controller.walker.Step;
import javelin.controller.walker.Walker;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Charging;
import javelin.model.unit.condition.Fatigued;
import javelin.model.unit.feat.attack.BullRush;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.TargetOverlay;

/**
 * Charging is a special full-round action that allows you to move up to twice
 * your speed and attack. You must move at least 10 feet (2 squares) and may
 * move up to double your speed directly toward the designated opponent. You
 * must have a clear path toward the opponent. After moving, you may make a
 * single melee attack. You get a +2 bonus on the attack roll and take a -2
 * penalty to your AC until the start of your next turn.
 */
public class Charge extends Fire implements AiAction {
	class ChargeOverlay extends Overlay {
		Point target;

		public ChargeOverlay(List<Point> steps, Point target) {
			this.target = target;
			affected.add(target);
			affected.addAll(steps);
		}

		@Override
		public void overlay(Tile t, Graphics g) {
			Point p = new Point(t.x, t.y);
			if (p.equals(target)) {
				draw(t, g, TargetOverlay.TARGET);
			} else if (affected.contains(p)) {
				draw(t, g, AiMovement.MOVEOVERLAY);
			}
		}
	}

	/** Constructor. */
	public Charge() {
		super("Charge", "c", 'c');
	}

	@Override
	protected void attack(final Combatant combatant,
			final Combatant targetCombatant, final BattleState s) {
		Action.outcome(charge(Fight.state, combatant, targetCombatant).get(0));
	}

	ArrayList<List<ChanceNode>> charge(BattleState state, Combatant me,
			Combatant target) {
		final ArrayList<List<ChanceNode>> chances = new ArrayList<List<ChanceNode>>();
		if (me.source.melee.isEmpty()
				|| me.hascondition(Fatigued.class) != null) {
			return chances;
		}
		Point from = new Point(me.location[0], me.location[1]);
		state = state.clone();
		me = state.clone(me);
		target = state.clone(target);
		final ArrayList<Step> walk = walk(me, target, state);
		final Step destination = walk.get(walk.size() - 1);
		if (state.getmeld(destination.x, destination.y) != null) {
			return new ArrayList<List<ChanceNode>>();
		}
		me.location[0] = destination.x;
		me.location[1] = destination.y;
		charge(me);
		final List<ChanceNode> move = MeleeAttack.SINGLETON.attack(me, target,
				usedefaultattack(me), 2, 0, 1, state);
		final boolean bullrush = me.source.hasfeat(BullRush.SINGLETON);
		List<Point> steps = new ArrayList<Point>(
				walk.subList(0, walk.size() - 1));
		steps.add(from);
		final Overlay o = new ChargeOverlay(steps, new Point(target));
		for (ChanceNode node : move) {
			node.action = me + " charges!\n" + node.action;
			node.delay = Delay.BLOCK;
			node.overlay = o;
			if (bullrush) {
				final BattleState post = (BattleState) node.n;
				final Combatant posttarget = post.clone(target);
				if (posttarget != null && posttarget.hp < target.hp) {
					final int pushx = Charge.push(me, posttarget, 0);
					final int pushy = Charge.push(me, posttarget, 1);
					if (!Charge.outoufbounds(post, pushx, pushy)
							&& state.getcombatant(pushx, pushy) == null
							&& state.getmeld(pushx, pushy) == null) {
						posttarget.location[0] = pushx;
						posttarget.location[1] = pushy;
					}
				}
			}
		}
		chances.add(move);
		return chances;
	}

	void charge(Combatant me) {
		me.addcondition(new Charging(me.ap + ActionCost.FULL, me));
		me.acmodifier -= 2;
	}

	static boolean outoufbounds(final BattleState s, final int x, final int y) {
		return x < 0 || y < 0 || x >= s.map.length || y >= s.map[0].length;
	}

	static int push(final Combatant me, final Combatant target, final int i) {
		return target.location[i] + target.location[i] - me.location[i];
	}

	Attack usedefaultattack(Combatant me) {
		return me.source.melee.get(0).get(0);
	}

	@Override
	protected int calculatehitdc(Combatant active, Combatant target,
			BattleState s) {
		return target.ac() - (2 + usedefaultattack(active).bonus);
	}

	@Override
	protected void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		super.filtertargets(combatant, targets, s);
		for (Combatant target : new ArrayList<Combatant>(targets)) {
			final ArrayList<Step> steps = walk(combatant, target, s);
			if (steps == null) {
				targets.remove(target);
			} else {
				final double distance = steps.size();
				if (distance < 2 || distance > 2 * combatant.gettopspeed(s) / 5
						|| distance > combatant.view(s.period)) {
					targets.remove(target);
				}
			}
		}
	}

	ArrayList<Step> walk(final Combatant me, Combatant target,
			final BattleState state) {
		final Walker walk = new ChargePath(
				new Point(me.location[0], me.location[1]),
				new Point(target.location[0], target.location[1]), state,
				me.source.swim() > 0);
		walk.walk();
		if (walk.solution == null) {
			return null;
		}
		final ArrayList<Step> threatened = (ArrayList<Step>) walk.solution
				.clone();
		threatened.remove(threatened.size() - 1);
		final ArrayList<Combatant> opponents = state.blueTeam == state
				.getteam(me) ? state.redTeam : state.blueTeam;
		for (Step s : threatened) {
			for (int deltax : Walker.DELTAS) {
				for (int deltay : Walker.DELTAS) {
					for (Combatant neighbor : opponents) {
						if (s.x + deltax == neighbor.location[0]
								&& s.y + deltay == neighbor.location[1]) {
							return null;
						}
					}
				}
			}
		}
		return walk.solution;
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(Combatant combatant,
			BattleState gameState) {
		ArrayList<List<ChanceNode>> outcomes = new ArrayList<List<ChanceNode>>();
		if (gameState.isengaged(combatant)) {
			return outcomes;
		}
		List<Combatant> targets = gameState.gettargets(combatant);
		filtertargets(combatant, targets, gameState);
		for (Combatant target : targets) {
			outcomes.addAll(charge(gameState, combatant, target));
		}
		return outcomes;
	}

	@Override
	protected void checkhero(Combatant hero) {
		return;
	}
}
