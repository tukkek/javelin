/*
 * Created on 12-Jun-2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package javelin.controller;

import javelin.controller.action.Dig;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;

/**
 * Player movement, taken from Mike's Tyrant.
 * 
 * @author Mike
 * 
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class Movement {
	/**
	 * try to move
	 * 
	 * @param state
	 * 
	 * @return <code>true</code> if some action taken
	 */
	public static boolean tryMove(int tx, int ty, BattleState state) {
		Meld m = Fight.state.getmeld(tx, ty);
		if (m != null && !m.crystalize(state)) {
			throw new RepeatTurn();
		}
		Combatant c = Fight.state.getCombatant(tx, ty);
		if (c == null) {
		} else {
			if (!c.isAlly(c, state)) {
				if (c.burrowed) {
					Dig.refuse();
				}
				javelin.controller.action.Movement.lastmovewasattack = true;
				c.meleeAttacks(c, state);
				return true;
			}
			throw new RepeatTurn();
		}
		state.next.location[0] = tx;
		state.next.location[1] = ty;
		return true;
	}

	private static final double rt2 = Math.sqrt(2);

}