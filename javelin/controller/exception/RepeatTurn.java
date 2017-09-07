package javelin.controller.exception;

import javelin.controller.action.Action;
import javelin.model.unit.attack.Combatant;

/**
 * Let's an {@link Action} finish execution at any point without causing the
 * game to advance with the current {@link Combatant} turn.
 * 
 * @author alex
 */
public class RepeatTurn extends RuntimeException {
	private static final long serialVersionUID = 1L;

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
