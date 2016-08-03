package javelin.view.frame.arena;

import javelin.model.unit.Combatant;
import javelin.model.world.location.unique.Arena;

/**
 * Alows to recruit temporary allies for the {@link Arena}.
 * 
 * @author alex
 */
public class AllyScreen extends HireScreen {
	ArenaSetup parent;

	/** Constructor. */
	public AllyScreen(ArenaSetup parent) {
		this.parent = parent;
		costmultiplier = 1;
	}

	@Override
	protected void select(Combatant c) {
		c.automatic = true;
		parent.allies.add(c);
		parent.show();
	}
}
