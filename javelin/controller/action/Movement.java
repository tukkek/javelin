package javelin.controller.action;

import javelin.Javelin;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Skills;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.Thing;

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
	public boolean perform(Combatant hero, BattleMap map, Thing thing) {
		try {
			final BattleState state = map.getState();
			final Point to = BattleScreen.active.gameHandler.doDirection(thing,
					this, state);
			if (to == null) {
				return false;
			}
			boolean disengaging = state.isengaged(thing.combatant);
			Meld meld = BattleMap.checkformeld(to.x, to.y);
			if (!Movement.lastmovewasattack) {
				BattleScreen.active.spentap +=
						cost(thing.combatant, state, to.x, to.y);
			}
			final boolean finishmove =
					meld != null || disengaging || Movement.lastmovewasattack
							|| BattleScreen.active.spentap >= .5f;
			if (!finishmove) {
				// BattleMap.visioncache.remove(thing.combatant.id);
				// thing.calculateVision();
				throw new RepeatTurn();
			}
			if (!Movement.lastmovewasattack) {
				if (meld == null) {
					Game.message(thing.combatant + " "
							+ (disengaging ? "disengages" : "moves") + "...",
							null, Delay.WAIT);
				} else {
					Game.message(thing.combatant + " powers up!", null,
							Delay.BLOCK);
					Javelin.getCombatant(thing.combatant.id).meld();
					BattleScreen.active.map.meld.remove(meld);
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
}
