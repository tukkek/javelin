package javelin.controller.exception;

import javelin.controller.upgrade.Spell;

/**
 * @see Spell#castpeacefully(javelin.model.unit.Combatant,
 *      javelin.model.unit.Combatant)
 * @author alex
 */
public class NotPeaceful extends Throwable {
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}
