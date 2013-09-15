package javelin.controller.action;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

public class Movement extends Action {
	private final String descriptivekeys;

	public Movement(final String name, final String[] keys,
			final String descriptivekeys) {
		super(name, keys);
		this.descriptivekeys = descriptivekeys;
	}

	public float cost(final Combatant c, final BattleState state, int x, int y) {
		int speed = c.source.gettopspeed();
		if (state.map[x][y].flooded && c.source.fly == 0) {
			speed = c.source.swim() ? c.source.swim : speed / 2;
		}
		/* TODO use the skill Balance */
		return isDisengaging(c, state) ? .25f : .5f / (speed / 5f);
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
		for (final Combatant nearby : s.getSurroudings(c)) {
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
}
