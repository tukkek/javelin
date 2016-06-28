package javelin.model.world.location.town.research;

import javelin.model.world.location.town.Town;
import javelin.view.screen.town.ResearchScreen;

/**
 * For 2 labor allows player to redraw all options from {@link Town#research
 * .researchhand}.
 * 
 * @author alex
 */
public class Redraw extends SpecialResearchCard {

	private static final int COST = 2;

	public Redraw() {
		super("Redraw all choices", COST);
		immediate = true;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		if (!candiscard(t, s)) {
			t.labor += COST;
			return;
		}
		for (int i = 0; i < t.research.hand.length; i++) {
			if (t.research.hand[i] != this) {
				t.research.hand[i] = null;
			}
		}
	}
}
