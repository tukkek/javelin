package javelin.view.frame.arena;

import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;

/**
 * Allows to recruit temporary allies for the {@link Arena}.
 * 
 * @author alex
 */
public class HireAllyScreen extends HireGladiatorScreen {
	ArenaSetup parent;

	/** Constructor. */
	public HireAllyScreen(ArenaSetup parent) {
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
