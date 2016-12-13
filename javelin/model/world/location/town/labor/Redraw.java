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
		town.governor.gethand().clear();
		town.governor.redraw();
	}

	@Override
	public boolean validate(District d) {
		return true;
	}
}
