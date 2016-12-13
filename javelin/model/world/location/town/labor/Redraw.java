package javelin.model.world.location.town.labor;

import javelin.model.world.location.town.District;

public class Redraw extends Labor {
	public Redraw() {
		super("Redraw", 0);
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

	@Override
	public boolean validate(District d) {
		return true;
	}
}
