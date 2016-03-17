package javelin.model.world.town.research;

import javelin.model.world.town.Town;
import javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen;

/**
 * For 2 labor allows player to redraw all options from
 * {@link Town#researchhand}.
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
		for (int i = 0; i < t.researchhand.length; i++) {
			if (t.researchhand[i] != this) {
				t.researchhand[i] = null;
			}
		}
	}
}
