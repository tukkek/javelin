package javelin.model.world.place.town.research;

import javelin.model.world.place.town.Town;
import javelin.view.screen.town.ResearchScreen;

/**
 * For 2 labor allows player to redraw all options from
 * {@link Town#research.researchhand}.
 * 
 * @author alex
 */
public class Redraw extends SpecialResearchCard {

	public Redraw() {
		super("Redraw all choices", 2);
		immediate = true;
	}

	@Override
	public void apply(Town t, ResearchScreen s) {
		for (int i = 0; i < t.research.hand.length; i++) {
			if (t.research.hand[i] != this) {
				t.research.hand[i] = null;
			}
		}
	}
}
