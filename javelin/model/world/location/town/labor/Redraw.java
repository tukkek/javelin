package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.Rank;

public class Redraw extends Labor {
	public Redraw() {
		super("Redraw", 0, Rank.HAMLET);
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
