package javelin.controller.action;

import javelin.Javelin;
import javelin.controller.action.ai.AiMovement;
import javelin.controller.exception.RepeatTurnException;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.Point;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * @see AiMovement
 * @author alex
 */
public class Movement extends Action {
	private final String descriptivekeys;
	public static boolean lastmovewasattack = false;

	public Movement(final String name, final String[] keys,
			final String descriptivekeys) {
		super(name, keys);
		this.descriptivekeys = descriptivekeys;
	}

	public float cost(final Combatant c, final BattleState state, int x,
			int y) {
		return isDisengaging(c, state)
				? Math.max(.1f, .25f - (.01f * c.source.skills.acrobatics))
				: (.5f / Movement.move(c, state, x, y));
	}

	/**
	 * @return The number of squares the {@link Combatant} can move in a move
	 *         action in this type of terrain.
	 */
	static float move(final Combatant c, final BattleState state, final int x,
			final int y) {
		float speed = c.source.gettopspeed();
		if (state.map[x][y].flooded && c.source.fly == 0) {
			speed = c.source.swim() ? c.source.swim : speed / 2f;
		}
		return speed / 5f;
	}

	/**
	 * To avoid having to implement attacks-of-opporutnity gonna simply prohibit
	 * that anything that would cause an aoo is simply prohibited. since the
	 * game is more fluid with movement/turns now this shouldn't be a problem.
	 * 
	 * Disengaging is simply forcing a 5-foot step to avoid aoo as per the core
	 * rules.
	 */
	public boolean isDisengaging(final Combatant c, final BattleState s) {
		for (final Combatant nearby : s.getSurroundings(c)) {
			if (!c.isAlly(nearby, s)) {
				return true;
			}
		}
		return false;
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
			boolean disengaging = isDisengaging(thing.combatant, state);
			Meld meld = BattleMap.checkformeld(to.x, to.y);
			if (!Movement.lastmovewasattack) {
				BattleScreen.active.spentap +=
						cost(thing.combatant, state, to.x, to.y);
			}
			boolean finishmove =
					meld != null || disengaging || Movement.lastmovewasattack
							|| BattleScreen.active.spentap >= .5f;
			if (!finishmove) {
				BattleMap.visioncache.remove(thing.combatant.id);
				thing.calculateVision();
				throw new RepeatTurnException();
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
			BattleScreen.active.spendap(hero);
			return true;
		} catch (RuntimeException e) {
			throw e;
		} finally {
			Movement.lastmovewasattack = false;
		}
	}
}
