package javelin.model.world.place.town.research;

import javelin.model.world.place.town.Town;
import javelin.view.screen.town.ResearchScreen;

/**
 * Grows {@link Town#size} by 1.
 * 
 * @see Town#work()
 * @author alex
 */
public class Grow extends Research {
	public Grow(Town t) {
		super("Grow", t.size);
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		t.size += 1;
	}

	@Override
	protected boolean isrepeated(Town t) {
		return false;
	}
}
