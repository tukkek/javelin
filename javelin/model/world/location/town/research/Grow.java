package javelin.model.world.location.town.research;

import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

public class Grow extends Research {

	public Grow(int size) {
		super("Grow",
				"Makes this town produce more labor and possibly grow in size",
				size);
	}

	@Override
	public void play(Town t) {
		t.size += 1;
	}

	@Override
	public boolean validate(Town t, District d) {
		cost = t.size;
		return true;
	}
}
