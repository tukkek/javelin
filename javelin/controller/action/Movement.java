package javelin.controller.action;

import javelin.Javelin;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Point;

/**
 * @see AiMovement
 * @author alex
 */
public class Movement extends Action {
	/** Cost to disengage. */
	public static final float DISENGAGE = .25f;
	private final String descriptivekeys;
	/** TODO hack */
	public static boolean lastmovewasattack = false;

	/**
	 * @see Action#Action(String, String[]).
	 * @param descriptivekeys
	 *            Used in printed description to user.
	 */
	public Movement(final String name, final String[] keys,
			final String descriptivekeys) {
		super(name, keys);
		this.descriptivekeys = descriptivekeys;
		this.allowwhileburrowed = true;
	}

	/**
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @return How much it costs to move to the specified square.
	 */
	public float cost(final Combatant c, final BattleState state, int x,
			int y) {
		float speed;
		if (c.burrowed) {
			speed = c.source.burrow;
		} else if (state.isengaged(c)) {
			return disengage(c);
		} else {
			speed = c.source.gettopspeed();
			if (state.map[x][y].flooded && c.source.fly == 0) {
				speed = c.source.swim() > 0 ? c.source.swim : speed / 2f;
			}
		}
		return converttoap(speed);
	}

	/**
	 * @param speed
	 *            Speed in feet (5 feet = 1 square).
	 * @return Cost of 1 square of movement in action points.
	 */
	public static float converttoap(float speed) {
		return .5f / (speed / 5f);
	}

	/**
	 * @param c
	 *            Checks {@link Skills#acrobatics}.
	 * @return Action points to disengage.
	 */
	static public float disengage(Combatant c) {
		return Math.max(.1f, DISENGAGE - (.01f * c.source.skills.acrobatics));
	}

	@Override
	public String[] getDescriptiveKeys() {
		return new String[] { descriptivekeys };
	}

	@Override
	public boolean perform(Combatant hero) {
		try {
			final BattleState state = Fight.state;
			boolean disengaging = state.isengaged(hero);
			final Point to = doDirection(this, state);
			if (to == null) {
				return false;
			}
			Meld meld = Fight.state.getmeld(to.x, to.y);
			if (!Movement.lastmovewasattack) {
				BattleScreen.active.spentap += cost(hero, state, to.x, to.y);
			}
			final boolean finishmove =
					meld != null || disengaging || Movement.lastmovewasattack
							|| BattleScreen.active.spentap >= .5f;
			if (!finishmove) {
				throw new RepeatTurn();
			}
			if (!Movement.lastmovewasattack) {
				if (meld == null) {
					Game.message(hero + " "
							+ (disengaging ? "disengages" : "moves") + "...",
							Delay.WAIT);
				} else {
					Game.message(hero + " powers up!", Delay.BLOCK);
					Javelin.getCombatant(hero.id).meld();
					Fight.state.meld.remove(meld);
				}
			}
			BattleScreen.active.spendap(hero, true);
			return true;
		} catch (RuntimeException e) {
			throw e;
		} finally {
			Movement.lastmovewasattack = false;
		}
	}

	public Point doDirection(final ActionDescription action,
			final BattleState state) {
		final Point direction = convertActionToDirection(action);
		final int tox = state.next.location[0] + direction.x;
		final int toy = state.next.location[1] + direction.y;
		return Movement.tryMove(tox, toy, state) ? new Point(tox, toy) : null;
	}

	public static Point
			convertActionToDirection(final ActionDescription action) {
		final Point direction = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		if (action == Action.MOVE_N) {
			direction.x = 0;
			direction.y = -1;
		}
		if (action == Action.MOVE_S) {
			direction.x = 0;
			direction.y = 1;
		}
		if (action == Action.MOVE_W) {
			direction.x = -1;
			direction.y = 0;
		}
		if (action == Action.MOVE_E) {
			direction.x = 1;
			direction.y = 0;
		}
		if (action == Action.MOVE_NW) {
			direction.x = -1;
			direction.y = -1;
		}
		if (action == Action.MOVE_NE) {
			direction.x = 1;
			direction.y = -1;
		}
		if (action == Action.MOVE_SW) {
			direction.x = -1;
			direction.y = 1;
		}
		if (action == Action.MOVE_SE) {
			direction.x = 1;
			direction.y = 1;
		}
		return direction.x == Integer.MIN_VALUE ? null : direction;
	}

	/**
	 * try to move
	 * 
	 * @param state
	 *            TODO use {@link Fight#state} directly.
	 * 
	 * @return <code>true</code> if some action taken
	 */
	public static boolean tryMove(int tx, int ty, BattleState state) {
		Meld m = state.getmeld(tx, ty);
		if (m != null && !m.crystalize(state)) {
			throw new RepeatTurn();
		}
		Combatant c = state.getCombatant(tx, ty);
		Combatant hero = state.next;
		if (c != null) {
			if (!c.isAlly(hero, state)) {
				if (c.burrowed) {
					Dig.refuse();
				}
				javelin.controller.action.Movement.lastmovewasattack = true;
				hero.meleeAttacks(c, state);
				return true;
			}
			throw new RepeatTurn();
		}
		hero.location[0] = tx;
		hero.location[1] = ty;
		return true;
	}

}
