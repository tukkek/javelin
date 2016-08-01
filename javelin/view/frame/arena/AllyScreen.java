package javelin.view.frame.arena;

import javelin.model.unit.Combatant;

public class AllyScreen extends HireScreen {
	ArenaSetup parent;

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
