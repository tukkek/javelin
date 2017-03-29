package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.Town;

public class Redraw extends Labor {
	public Redraw() {
		super("Redraw", 0, Town.HAMLET);
		automatic = false;
	}

	@Override
	protected void define() {
		// nothing
	}

	@Override
	public void done() {
		for (Labor l : town.governor.gethand()) {
			l.discard();
		}
		town.governor.redraw();
	}
}
