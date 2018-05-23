package javelin.controller.action;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.skill.Acrobatics;
import javelin.model.unit.skill.Skill;
import javelin.old.Game;
import javelin.old.Game.Delay;
import javelin.view.mappanel.battle.action.BattleMouseAction;
import javelin.view.screen.BattleScreen;

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
		allowburrowed = true;
	}

	/**
	 * @param x
	 *            Location.
	 * @param y
	 *            Location.
	 * @param disengaging
	 * @return How much it costs to move to the specified square.
	 */
	static public float cost(final Combatant c, final BattleState s, int x,
			int y, boolean disengaging) {
		float speed;
		if (c.burrowed) {
			speed = c.source.burrow;
		} else if (disengaging) {
			return disengage(c);
		} else {
			speed = c.gettopspeed(s);
			if (s.map[x][y].flooded && c.source.fly == 0) {
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
	 *            Checks {@link Acrobatics}.
	 * @return Action points to disengage.
	 */
	static public float disengage(Combatant c) {
		return Math.max(.01f, DISENGAGE - .01f * Skill.ACROBATICS.getbonus(c));
	}

	@Override
	public String[] getDescriptiveKeys() {
		return new String[] { descriptivekeys };
	}

	@Override
	public boolean perform(Combatant c) {
		try {
			boolean disengaging = Fight.state.isengaged(c);
			final Point to = doDirection(this, Fight.state, c);
			if (to == null) {
				return false;
			}
			Meld meld = Fight.state.getmeld(to.x, to.y);
			if (!Movement.lastmovewasattack) {
				float ap = cost(c, Fight.state, to.x, to.y, disengaging);
				BattleScreen.partialmove += ap;
				c.ap += ap;
			}
			final boolean finishmove = meld != null || disengaging
					|| Movement.lastmovewasattack
					|| BattleScreen.partialmove >= .5f;
			if (!finishmove) {
				throw new RepeatTurn();
			}
			BattleScreen.partialmove = 0;
			if (!Movement.lastmovewasattack) {
				if (meld == null || meld.meldsat > c.ap) {
					String action = disengaging ? "disengages" : "moves";
					Game.message(c + " " + action + "...", Delay.WAIT);
				} else {
					Javelin.app.fight.meld(c, meld);
				}
			}
			return true;
		} catch (RuntimeException e) {
			throw e;
		} finally {
			Movement.lastmovewasattack = false;
		}
	}

	Point doDirection(final ActionDescription action, final BattleState state,
			Combatant hero) {
		final Point direction = convertActionToDirection(action);
		final int tox = hero.location[0] + direction.x;
		final int toy = hero.location[1] + direction.y;
		return Movement.tryMove(tox, toy, state, hero) ? new Point(tox, toy)
				: null;
	}

	static Point convertActionToDirection(final ActionDescription action) {
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
	 * @param hero2
	 *
	 * @return <code>true</code> if some action taken
	 */
	public static boolean tryMove(int x, int y, BattleState state,
			Combatant hero) {
		Combatant c = state.getcombatant(x, y);
		if (c == null && state.map[x][y].blocked
				&& (hero.source.fly == 0 || !Javelin.app.fight.map.flying)) {
			return false;
		}
		Meld m = state.getmeld(x, y);
		if (m != null && !m.crystalize(state)) {
			throw new RepeatTurn();
		}
		if (c == null) {
			hero.location[0] = x;
			hero.location[1] = y;
			return true;
		}
		if (c.isally(hero, state)) {
			BattleMouseAction action = c.getmouseaction();
			if (action != null && action.validate(hero, c, state)) {
				BattleScreen.perform(action.act(hero, c, state));
			}
			throw new RepeatTurn();
		}
		if (c.burrowed) {
			Dig.refuse();
		}
		javelin.controller.action.Movement.lastmovewasattack = true;
		hero.meleeattacks(c, state);
		return true;
	}

}
